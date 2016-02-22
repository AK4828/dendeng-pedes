package com.hoqii.fxpc.sales.entity;

import com.hoqii.fxpc.sales.core.DefaultPersistence;

/**
 * Created by meruvian on 06/10/15.
 */
public class AssigmentDetailItem extends DefaultPersistence {
    private AssigmentDetail assigmentDetail = new AssigmentDetail();
    private ProductStore product = new ProductStore();
    private int qty;

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
}
