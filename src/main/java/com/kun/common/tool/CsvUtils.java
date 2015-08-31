package com.kun.common.tool;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class CsvUtils {

    private static final Logger logger = LoggerFactory.getLogger(CsvUtils.class);

    public static List<Map<String, Object>> getList(String fileName) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        try {
            Reader reader = new FileReader(fileName);
            CSVReader csvReader = new CSVReader(reader);
            List<String[]> rows = csvReader.readAll();
            Map<Integer, String> headsMap = new HashMap<Integer, String>();
            for (int i = 0; i < rows.size(); i++) {
                String[] oneRowColumns = rows.get(i);
                if (i == 0) {
                    for (int j = 0; j < oneRowColumns.length; j++) {
                        headsMap.put(j, oneRowColumns[j]);
                    }
                    continue;
                }
                if (oneRowColumns.length != headsMap.size()) {
                    continue;
                }
                Map<String, Object> dataRow = new HashMap<String, Object>();
                for (int j = 0; j < oneRowColumns.length; j++) {
                    dataRow.put(headsMap.get(j), oneRowColumns[j]);
                }
                list.add(dataRow);
            }
            csvReader.close();
        } catch (FileNotFoundException e) {
            logger.error("{}", e);
        } catch (IOException e) {
            logger.error("Exception ", e);
        }
        return list;
    }


    public static void appendCsv(List<Map<String, Object>> list, String fileName, boolean ignoreHeader) {
        try {
            FileWriter writer;
            writer = new FileWriter(fileName, true);
            CSVWriter cSVWriter = new CSVWriter(writer);
            List<String[]> data = toStringArray(list, ignoreHeader);
            cSVWriter.writeAll(data);
            try {
                writer.close();
                cSVWriter.close();
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        } catch (IOException e1) {
            logger.error(e1.getMessage());
        }
    }

    public static void writeCsv(List<Map<String, Object>> list, String fileName, boolean ignoreHeader) {
        try {
            FileWriter writer;
            writer = new FileWriter(fileName);
            CSVWriter cSVWriter = new CSVWriter(writer);
            List<String[]> data = toStringArray(list, ignoreHeader);
            cSVWriter.writeAll(data);
            try {
                writer.close();
                cSVWriter.close();
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        } catch (IOException e1) {
            logger.error(e1.getMessage());
        }
    }

    private static List<String[]> toStringArray(List<Map<String, Object>> data, boolean ignoreHeader) {
        List<String[]> records = new ArrayList<String[]>();
        List<String> headers = new ArrayList<String>();
        Set<String> headSet = data.get(0).keySet();
        Iterator<String> headIt = headSet.iterator();
        while (headIt.hasNext()) {
            String head = headIt.next();
            headers.add(head);
        }

        String[] headArr = new String[headers.size()];
        headArr = headers.toArray(headArr);
        if (!ignoreHeader) {
            records.add(headArr);
        }

        for(Map<String, Object> dataRecord : data){
            List<String> oneRecord = new ArrayList<String>();
            for (int j = 0; j < headers.size(); j++) {
                oneRecord.add(dataRecord.get(headers.get(j)).toString());
            }
            String[] dateItem = new String[oneRecord.size()];
            dateItem = oneRecord.toArray(dateItem);
            records.add(dateItem);
        }
        return records;
    }



}
