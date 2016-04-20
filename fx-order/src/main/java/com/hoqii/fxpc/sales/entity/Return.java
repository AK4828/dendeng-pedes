package com.hoqii.fxpc.sales.entity;

import com.hoqii.fxpc.sales.core.DefaultPersistence;

/**
 * Created by akm on 14/04/16.
 */
public class Return extends DefaultPersistence {

    public enum ReturnStatus {
        WAIT, RECEIVED, FAILED, RETURNED
    }
    private Order order;
    private ReturnStatus status = ReturnStatus.WAIT;
    private Shipment shipment;
    private String recipient;

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public ReturnStatus getStatus() {
        return status;
    }

    public void setStatus(ReturnStatus status) {
        this.status = status;
    }

    public Shipment getShipment() {
        return shipment;
    }

    public void setShipment(Shipment shipment) {
        this.shipment = shipment;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }
}
