package com.hoqii.fxpc.sales.entity;


import com.hoqii.fxpc.sales.core.DefaultPersistence;

public class CartMenu extends DefaultPersistence {
	private Cart cart = new Cart();
	private int qty;
	private double sellPrice;
	private Product product = new Product();
	private String description;

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

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

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

}
