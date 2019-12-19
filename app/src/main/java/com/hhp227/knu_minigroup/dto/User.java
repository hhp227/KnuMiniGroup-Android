package com.hhp227.knu_minigroup.dto;

import java.io.Serializable;

public class User implements Serializable {
    String userId, password, name, department, number, grade, email, imageId;

    public User() {
    }

    public User(String userId, String password, String name, String department, String number, String grade, String email, String imageId) {
        this.userId = userId;
        this.password = password;
        this.name = name;
        this.department = department;
        this.number = number;
        this.grade = grade;
        this.email = email;
        this.imageId = imageId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }
}
