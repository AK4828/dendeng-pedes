package com.hoqii.fxpc.sales.entity;

import org.meruvian.midas.core.entity.DefaultEntity;

/**
 * Created by miftakhul on 11/23/15.
 */
public class Supplier extends DefaultEntity{

    private String name;
    private String email;
    private String phone;
    private String comphany;

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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getComphany() {
        return comphany;
    }

    public void setComphany(String comphany) {
        this.comphany = comphany;
    }
}
