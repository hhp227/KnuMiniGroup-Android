package com.hhp227.knu_minigroup.user;

import java.io.Serializable;

public class User implements Serializable {
    int id;
    String name, knuId, password;

    public User() {
    }

    public User(int id, String name, String knuId, String password) {
        this.id = id;
        this.name = name;
        this.knuId = knuId;
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKnuId() {
        return knuId;
    }

    public void setKnuId(String knuId) {
        this.knuId = knuId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
