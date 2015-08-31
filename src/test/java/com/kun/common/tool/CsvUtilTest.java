package com.kun.common.tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class CsvUtilTest {
    
   @Test
   public void writeCsv(){
       List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
       for(int i=0; i<10; i++){
           Map<String, Object> dataRow =new HashMap<String, Object>();
           dataRow.put("name", "yy" + i);
           dataRow.put("Id", i);
           dataRow.put("Email", "em@ail" + i);
           list.add(dataRow);
       }
       
       CsvUtil.writeCsv(list, "test", false);
       
   }
   
   @Test
   public void getList(){
       List<Map<String, Object>> list = CsvUtil.getList("test");
       for(Map<String, Object> row : list){
           for(String key : row.keySet()){
               System.out.println(row.get(key));
           }
       }
   }

}
