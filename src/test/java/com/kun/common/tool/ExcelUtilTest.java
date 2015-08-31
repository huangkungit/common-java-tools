package com.kun.common.tool;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.kun.common.domain.Person;

public class ExcelUtilTest {

    @Test
    public void exportExcelFile(){
        List<Person> list = new ArrayList<Person>();
        for(int i=0; i<5; i++){
            Person person = new Person();
            person.setEmail("email" + i);
            person.setId(i + 0l);
            person.setName("name" + i);
            list.add(person);
        }
        try {
            ExcelUtil.exportExcelFile("test", list, Person.class, "person");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    @Test
    public void importExcelFile(){
        
        try {
            List<Person> data = ExcelUtil.importExcelFile("test", Person.class, null);
            for(Person person : data){
                System.out.println(person.getId());
                System.out.println(person.getName());
                System.out.println(person.getEmail());
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
}
