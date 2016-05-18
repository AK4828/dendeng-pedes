package com.hoqii.fxpc.sales.entity;

/**
 * Created by meruvian on 20/04/16.
 */
public class SerialEvent {
    boolean status = false;
    String serial ;

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }
}
