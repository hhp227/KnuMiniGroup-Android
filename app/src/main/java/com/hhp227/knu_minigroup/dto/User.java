package com.hhp227.knu_minigroup.dto;

import java.io.Serializable;

public class User implements Serializable {
    boolean anonymous, emailVerified;

    String email, uid, providerId;

    String userId, password, name, department, number, grade, userIp, campus, phoneNumber;

    public User() {
    }

    public User(String userId, String password, String name, String department, String number, String grade, String email, String uid, String userIp, String campus, String phoneNumber) {
        this.userId = userId;
        this.password = password;
        this.name = name;
        this.department = department;
        this.number = number;
        this.grade = grade;
        this.email = email;
        this.uid = uid;
        this.userIp = userIp;
        this.campus = campus;
        this.phoneNumber = phoneNumber;
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

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUserIp() {
        return userIp;
    }

    public void setUserIp(String userIp) {
        this.userIp = userIp;
    }

    public String getCampus() {
        return campus;
    }

    public void setCampus(String campus) {
        this.campus = campus;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
