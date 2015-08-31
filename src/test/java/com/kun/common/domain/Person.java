package com.kun.common.domain;

import com.kun.common.tool.ExcelCell;



public class Person {

    @ExcelCell(name = "Id")
    private Long id;

    @ExcelCell(name = "Name")
    private String name;

    @ExcelCell(name = "Email")
    private String email;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

   
}
