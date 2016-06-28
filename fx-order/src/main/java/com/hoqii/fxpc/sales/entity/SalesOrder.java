package com.hoqii.fxpc.sales.entity;

import com.hoqii.fxpc.sales.core.DefaultPersistence;
import com.hoqii.fxpc.sales.core.commons.Site;

/**
 * Created by miftakhul on 23/06/16.
 */
public class SalesOrder extends DefaultPersistence{
    public enum SalesOrderStatus {
        PROCESSED, SENDING, RECEIVED, DONE, CANCELED
    }

    private String receiptNumber;
    private Site siteFrom = new Site();
    private SalesOrderStatus status = SalesOrderStatus.PROCESSED;
    private String name;
    private String address;
    private String email;
    private String telephone;

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    public Site getSiteFrom() {
        return siteFrom;
    }

    public void setSiteFrom(Site siteFrom) {
        this.siteFrom = siteFrom;
    }

    public SalesOrderStatus getStatus() {
        return status;
    }

    public void setStatus(SalesOrderStatus status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }
}
