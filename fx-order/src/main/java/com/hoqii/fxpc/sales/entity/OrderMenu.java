package com.hoqii.fxpc.sales.entity;

import com.hoqii.fxpc.sales.core.DefaultPersistence;

public class OrderMenu extends DefaultPersistence {
	public enum OrderMenuStatus {
		ORDER, CANCELED, SHIPPED
	}
	public enum OrderType{
		REQUESITION,QUOTATION,PURCHASE_ORDER
	}

	private Order order = new Order();
	private int qty;
	private int qtyOrder;
	private double sellPrice;
	private Product product = new Product();
	private String description;
	private String imei;
	private OrderMenuStatus status = OrderMenuStatus.ORDER;
	private OrderType orderType;
	private String type;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

    public double getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(double sellPrice) {
        this.sellPrice = sellPrice;
    }

    public int getQty() {
		return qty;
	}

	public void setQty(int qty) {
		this.qty = qty;
	}

	public int getQtyOrder() {
		return qtyOrder;
	}

	public void setQtyOrder(int qtyOrder) {
		this.qtyOrder = qtyOrder;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public String getImei() {
		return imei;
	}

	public void setImei(String imei) {
		this.imei = imei;
	}

	public OrderMenuStatus getStatus() {
		return status;
	}

	public void setStatus(OrderMenuStatus status) {
		this.status = status;
	}

	public OrderType getOrderType() {
		return orderType;
	}

	public void setOrderType(OrderType orderType) {
		this.orderType = orderType;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}


}
