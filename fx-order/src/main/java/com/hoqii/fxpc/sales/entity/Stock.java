package com.hoqii.fxpc.sales.entity;

import com.hoqii.fxpc.sales.core.DefaultPersistence;

/**
 * Created by miftakhul on 3/5/16.
 */
public class Stock extends DefaultPersistence{

    private Product product = new Product();
    private int qty = 0;

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
}
