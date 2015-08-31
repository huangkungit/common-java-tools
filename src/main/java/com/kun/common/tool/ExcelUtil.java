package com.kun.common.tool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExcelUtil {

    private static final Logger logger = LoggerFactory.getLogger(ExcelUtil.class);


    public static <T> void exportExcelFile(String fileName, Collection<T> data,
                    Class<T> targetClass, String sheetName) throws Exception {

        if (data == null || targetClass == null) {
            return;
        }
        if (sheetName == null) {
            sheetName = "defaultName";
        }
        SXSSFWorkbook book = createExportFile(data, targetClass, sheetName);
        File excelFile = new File(fileName);
        OutputStream os = new FileOutputStream(excelFile);
        book.write(os);
        os.close();
    }

    public static <T> List<T> importExcelFile(String fileName, Class<T> targetClass,
                    String sheetName) throws Exception {
        if (fileName == null) {
            return null;
        }
        Workbook workbook = getWorkbook(fileName);
        Sheet sheet = null;
        if (sheetName == null) {
            sheet = workbook.getSheetAt(workbook.getActiveSheetIndex());
        } else {
            sheet = workbook.getSheet(sheetName);
        }
        List<T> data = importFile(targetClass, sheet);
        return data;
    }



    private static <T> SXSSFWorkbook createExportFile(Collection<T> data, Class<T> targetClass,
                    String sheetName) throws Exception {
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        logger.info("Export start");
        exportFile(data, targetClass, workbook.createSheet(sheetName), false);
        logger.info("Export end");
        return workbook;
    }

    private static <T> List<T> importFile(Class<T> targetObjectClass, Sheet sheet) throws Exception {
        List<T> objects = new ArrayList<T>();
        Iterator<Row> row = sheet.rowIterator();
        Map<Integer, String> headerMap = getHeaderMapFromExcel(row);
        Map<String, Method> setterMap = getSetterMap(targetObjectClass);
        Map<String, Class<?>> typeMap = getTypeMap(targetObjectClass);
        while (row.hasNext()) {
            Row rown = row.next();
            Iterator<Cell> cells = rown.cellIterator();
            T object = targetObjectClass.newInstance();
            int cellIndex = 0;
            while (cells.hasNext()) {
                Cell cell = cells.next();
                String cellName = headerMap.get(cellIndex);
                invokeSetterMethod(setterMap, typeMap, object, cell, cellName);
                cellIndex = cellIndex + 1;
            }
            objects.add(object);
        }

        return objects;
    }


    private static <T> void exportFile(Collection<T> objects, Class<T> targetObejctClass,
                    Sheet sheet, boolean ignore) throws NoSuchMethodException, SecurityException,
                    IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Map<Integer, String> headerMap = getHeaderMapFromObjectClass(targetObejctClass, ignore);
        Map<String, Method> getterMap = getGetterMap(targetObejctClass);
        Map<String, Class<?>> typeMap = getTypeMap(targetObejctClass);
        Row row = sheet.createRow((short) 0);
        for (int i = 0; i < headerMap.size(); i++) {
            Cell cell = row.createCell(i);
            cell.setCellType(HSSFCell.CELL_TYPE_STRING);
            cell.setCellValue(headerMap.get(i));
        }

        int rowIndex = 1;
        for (T object : objects) {
            if (rowIndex % 5000 == 0) {
                logger.info(" {} records have been processed", rowIndex);
            }
            row = sheet.createRow(rowIndex++);
            for (int i = 0; i < headerMap.size(); i++) {
                Cell cell = row.createCell(i);
                String cellName = headerMap.get(i);
                Method method = getterMap.get(cellName);

                if (typeMap.get(cellName).toString().equals(Integer.class.toString())) {
                    cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
                    cell.setCellValue((Integer) method.invoke(object));
                } else if (typeMap.get(cellName).toString().equals(Double.class.toString())) {
                    cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
                    cell.setCellValue((Double) method.invoke(object));
                } else if (typeMap.get(cellName).toString().equals(Long.class.toString())) {
                    cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
                    cell.setCellValue((Long) method.invoke(object));
                } else {
                    cell.setCellType(HSSFCell.CELL_TYPE_STRING);
                    cell.setCellValue((String) method.invoke(object));
                }
            }
            object = null;
        }

    }

    private static Map<String, Method> getGetterMap(Class<?> targetObjectClass)
                    throws NoSuchMethodException, SecurityException {
        Map<String, Method> result = new HashMap<String, Method>();
        Field fields[] = targetObjectClass.getDeclaredFields();
        for (Field field : fields) {
            ExcelCell excel = field.getAnnotation(ExcelCell.class);
            if (excel != null) {
                String fieldName = field.getName();
                String getMethodName =
                                "get" + fieldName.substring(0, 1).toUpperCase()
                                                + fieldName.substring(1);
                Method method = targetObjectClass.getMethod(getMethodName);
                result.put(excel.name(), method);
            }
        }
        return result;
    }


    private static Map<Integer, String> getHeaderMapFromObjectClass(Class<?> targetObjectClass,
                    boolean ignore) {
        Map<Integer, String> result = new HashMap<Integer, String>();
        Field fields[] = targetObjectClass.getDeclaredFields();
        int i = 0;
        for (Field field : fields) {
            ExcelCell excel = field.getAnnotation(ExcelCell.class);
            if (excel != null
                            && ((ignore == true && excel.ignorable() == false) || ignore == false)) {
                result.put(i++, excel.name());
            }
        }
        return result;
    }

    private static void invokeSetterMethod(Map<String, Method> setterMap,
                    Map<String, Class<?>> typeMap, Object object, Cell cell, String cellName)
                    throws IllegalAccessException, InvocationTargetException {
        if (setterMap.containsKey(cellName)) {
            Method setMethod = setterMap.get(cellName);
            if (typeMap.get(cellName).toString().equals(Integer.class.toString())) {
                setMethod.invoke(object, Double.valueOf(cell.getNumericCellValue()).intValue());
            } else if (typeMap.get(cellName).toString().equals(Double.class.toString())) {
                setMethod.invoke(object, cell.getNumericCellValue());
            } else if (typeMap.get(cellName).toString().equals(Long.class.toString())) {
                setMethod.invoke(object, Double.valueOf(cell.getNumericCellValue()).longValue());
            } else {
                setMethod.invoke(object, cell.getStringCellValue());
            }

        }
    }

    private static Map<Integer, String> getHeaderMapFromExcel(Iterator<Row> row) {
        Row title = row.next();
        Iterator<Cell> cellTitle = title.cellIterator();
        Map<Integer, String> titlemap = new HashMap<Integer, String>();
        int titleIndex = 0;
        while (cellTitle.hasNext()) {
            Cell cell = cellTitle.next();
            String value = cell.getStringCellValue();
            titlemap.put(titleIndex, value);
            titleIndex = titleIndex + 1;
        }
        return titlemap;
    }

    private static Map<String, Method> getSetterMap(Class<?> targetObjectClass)
                    throws NoSuchMethodException, SecurityException {
        Map<String, Method> result = new HashMap<String, Method>();
        Field fields[] = targetObjectClass.getDeclaredFields();
        for (Field field : fields) {
            ExcelCell excel = field.getAnnotation(ExcelCell.class);
            if (excel != null) {
                String fieldName = field.getName();
                String setMethodName =
                                "set" + fieldName.substring(0, 1).toUpperCase()
                                                + fieldName.substring(1);
                Method method = targetObjectClass.getMethod(setMethodName, field.getType());
                result.put(excel.name(), method);
            }
        }
        return result;
    }


    private static Map<String, Class<?>> getTypeMap(Class<?> targetObjectClass)
                    throws NoSuchMethodException, SecurityException {
        Map<String, Class<?>> result = new HashMap<String, Class<?>>();
        Field fields[] = targetObjectClass.getDeclaredFields();
        for (Field field : fields) {
            ExcelCell excel = field.getAnnotation(ExcelCell.class);
            if (excel != null) {
                result.put(excel.name(), field.getType());
            }
        }
        return result;
    }

    private static Workbook getWorkbook(String fileName) throws IOException, InvalidFormatException {
        Workbook workbook = null;
        try {
            File file = new File(fileName);
            workbook = WorkbookFactory.create(file);
        } catch(IOException e){
            e.printStackTrace();
        } catch(InvalidFormatException e){
            e.printStackTrace();
        }
        return workbook;
    }
}
