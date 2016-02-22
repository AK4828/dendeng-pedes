package com.hoqii.fxpc.sales.entity;

import com.hoqii.fxpc.sales.core.DefaultPersistence;

/**
 * Created by meruvian on 05/10/15.
 */
public class Settle extends DefaultPersistence {
    private AssigmentDetail assigmentDetail = new AssigmentDetail();
    private ProductStore product = new ProductStore();
    private int qty;
    private double sellPrice;

    public AssigmentDetail getAssigmentDetail() {
        return assigmentDetail;
    }

    public void setAssigmentDetail(AssigmentDetail assigmentDetail) {
        this.assigmentDetail = assigmentDetail;
    }

    public ProductStore getProduct() {
        return product;
    }

    public void setProduct(ProductStore product) {
        this.product = product;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public double getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(double sellPrice) {
        this.sellPrice = sellPrice;
    }

}
