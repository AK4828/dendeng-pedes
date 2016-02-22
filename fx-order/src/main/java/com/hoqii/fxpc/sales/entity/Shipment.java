package com.hoqii.fxpc.sales.entity;

import com.hoqii.fxpc.sales.core.DefaultPersistence;

/**
 * Created by miftakhul on 12/18/15.
 */
public class Shipment extends DefaultPersistence{

    public enum ShipmentStatus{
        WAIT, DELIVERED, FAILED
    }

    private Order order = new Order();
    private ShipmentStatus status = ShipmentStatus.WAIT;
    private String deliveryServiceName;
    private String receiptNumber;

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public ShipmentStatus getStatus() {
        return status;
    }

    public void setStatus(ShipmentStatus status) {
        this.status = status;
    }

    public String getDeliveryServiceName() {
        return deliveryServiceName;
    }

    public void setDeliveryServiceName(String deliveryServiceName) {
        this.deliveryServiceName = deliveryServiceName;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }
}
