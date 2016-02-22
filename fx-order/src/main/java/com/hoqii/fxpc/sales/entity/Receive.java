package com.hoqii.fxpc.sales.entity;

import com.hoqii.fxpc.sales.core.DefaultPersistence;

/**
 * Created by miftakhul on 1/10/16.
 */
public class Receive extends DefaultPersistence{

    public enum ReceiveStatus{
        WAIT, RECEIVED, FAILED
    }

    private Order order;
    private ReceiveStatus status = ReceiveStatus.WAIT;
    private Shipment shipment;
    private String recipient;

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public ReceiveStatus getStatus() {
        return status;
    }

    public void setStatus(ReceiveStatus status) {
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
