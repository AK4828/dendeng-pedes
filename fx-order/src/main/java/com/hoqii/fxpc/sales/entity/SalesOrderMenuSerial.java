package com.hoqii.fxpc.sales.entity;

import com.hoqii.fxpc.sales.core.DefaultPersistence;

/**
 * Created by miftakhul on 23/06/16.
 */
public class SalesOrderMenuSerial extends DefaultPersistence{
    private SalesOrderMenu salesOrderMenu = new SalesOrderMenu();
    private String serialNumber;

    public SalesOrderMenu getSalesOrderMenu() {
        return salesOrderMenu;
    }

    public void setSalesOrderMenu(SalesOrderMenu salesOrderMenu) {
        this.salesOrderMenu = salesOrderMenu;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }
}
