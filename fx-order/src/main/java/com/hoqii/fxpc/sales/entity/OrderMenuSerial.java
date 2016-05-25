package com.hoqii.fxpc.sales.entity;

import com.hoqii.fxpc.sales.core.DefaultPersistence;

/**
 * Created by miftakhul on 1/5/16.
 */
public class OrderMenuSerial extends DefaultPersistence{

    private OrderMenu orderMenu = new OrderMenu();
    private Shipment shipment = new Shipment();
    private String serialNumber;

    public OrderMenu getOrderMenu() {
        return orderMenu;
    }

    public void setOrderMenu(OrderMenu orderMenu) {
        this.orderMenu = orderMenu;
    }

    public Shipment getShipment() {
        return shipment;
    }

    public void setShipment(Shipment shipment) {
        this.shipment = shipment;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

}
