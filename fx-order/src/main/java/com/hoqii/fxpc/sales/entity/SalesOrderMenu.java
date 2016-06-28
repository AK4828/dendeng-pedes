package com.hoqii.fxpc.sales.entity;

import com.hoqii.fxpc.sales.core.DefaultPersistence;

/**
 * Created by miftakhul on 23/06/16.
 */
public class SalesOrderMenu extends DefaultPersistence{

    public enum SalesOrderMenuStatus {
        ORDER, CANCELED, SHIPPED
    }

    private SalesOrder salesOrder = new SalesOrder();
    private Product product= new Product();
    private int qty;
    private int qtySalesOrder;
    private double sellPrice;
    private String description;
    private SalesOrderMenuStatus status = SalesOrderMenuStatus.ORDER;

    public SalesOrder getSalesOrder() {
        return salesOrder;
    }

    public void setSalesOrder(SalesOrder salesOrder) {
        this.salesOrder = salesOrder;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public int getQtySalesOrder() {
        return qtySalesOrder;
    }

    public void setQtySalesOrder(int qtySalesOrder) {
        this.qtySalesOrder = qtySalesOrder;
    }

    public double getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(double sellPrice) {
        this.sellPrice = sellPrice;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SalesOrderMenuStatus getStatus() {
        return status;
    }

    public void setStatus(SalesOrderMenuStatus status) {
        this.status = status;
    }
}
