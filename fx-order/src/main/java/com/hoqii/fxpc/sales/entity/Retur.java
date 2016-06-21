package com.hoqii.fxpc.sales.entity;

import com.hoqii.fxpc.sales.core.DefaultPersistence;
import com.hoqii.fxpc.sales.core.commons.Site;

/**
 * Created by akm on 14/04/16.
 */
public class Retur extends DefaultPersistence {

    public enum ReturnStatus {
        WAIT, RETURNED, FAILED
    }
    private ReturnStatus status = ReturnStatus.RETURNED;
    private Site siteTo;
    private Site siteFrom;
    private String description;
    private OrderMenuSerial orderMenuSerial;

    public ReturnStatus getStatus() {
        return status;
    }

    public void setStatus(ReturnStatus status) {
        this.status = status;
    }

    public Site getSiteTo() {
        return siteTo;
    }

    public void setSiteTo(Site siteTo) {
        this.siteTo = siteTo;
    }

    public Site getSiteFrom() {
        return siteFrom;
    }

    public void setSiteFrom(Site siteFrom) {
        this.siteFrom = siteFrom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public OrderMenuSerial getOrderMenuSerial() {
        return orderMenuSerial;
    }

    public void setOrderMenuSerial(OrderMenuSerial orderMenuSerial) {
        this.orderMenuSerial = orderMenuSerial;
    }

}
