package com.hoqii.fxpc.sales.core.commons;

import com.hoqii.fxpc.sales.core.DefaultPersistence;

/**
 * Created by root on 19/11/14.
 */
public class Role extends DefaultPersistence {
    private String name;
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
