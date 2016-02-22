package com.hoqii.fxpc.sales.core.commons;

import java.io.Serializable;

/**
 * Created by meruvian on 06/08/15.
 */
public class Bank implements Serializable {
    private String bankName;
    private String accountName;
    private String accountNumber;

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
}
