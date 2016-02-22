package com.hoqii.fxpc.sales.entity;

import com.hoqii.fxpc.sales.core.DefaultPersistence;

/**
 * Created by meruvian on 12/09/15.
 */
public class Payment extends DefaultPersistence {
    public enum PaymentStatus {
        UNPAID, PAID, CANCELED
    }

    public enum PaymentType {
        COD, TRANSFER
    }

    private Order order;
    private PaymentStatus status = PaymentStatus.UNPAID;
    private PaymentType type;
    private SiteBank transferTo;
    private String bankName;
    private String bankNumber;
    private String bankAccount;
    private String reference;

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public PaymentType getType() {
        return type;
    }

    public void setType(PaymentType type) {
        this.type = type;
    }

    public SiteBank getTransferTo() {
        return transferTo;
    }

    public void setTransferTo(SiteBank transferTo) {
        this.transferTo = transferTo;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankNumber() {
        return bankNumber;
    }

    public void setBankNumber(String bankNumber) {
        this.bankNumber = bankNumber;
    }

    public String getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
}
