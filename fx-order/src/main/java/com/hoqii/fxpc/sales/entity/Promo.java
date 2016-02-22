package com.hoqii.fxpc.sales.entity;

import org.meruvian.midas.core.entity.DefaultEntity;

/**
 * Created by ludviantoovandi on 05/02/15.
 */
public class Promo extends DefaultEntity {
    private String description;
    private int image;

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
