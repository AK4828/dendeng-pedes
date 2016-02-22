package com.hoqii.fxpc.sales.entity;

import org.meruvian.midas.core.entity.DefaultEntity;

/**
 * Created by meruvian on 24/03/15.
 */
public class Campaign extends DefaultEntity {
    private String name;
    private String description;
    private boolean showOnAndroid;
    private String refId;

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

    public boolean isShowOnAndroid() {
        return showOnAndroid;
    }

    public void setShowOnAndroid(boolean showOnAndroid) {
        this.showOnAndroid = showOnAndroid;
    }

    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }
}
