package com.hoqii.fxpc.sales.entity;

import org.meruvian.midas.core.entity.DefaultEntity;

import java.util.Date;

public class Product extends DefaultEntity {
	private Category category;
	private Category parentCategory;
	private String stockId;
	private String barcodeId;
	private String name;
	private long buyPrice;
	private long sellPrice;
	private int quantityFirst;
	private int minQuantity;
	private int maxQuantity;
//	private UnitOfMeasure unitOfMeasure;
	private Date expiredDate;
//	private Vendor vendor;
	private int image;
	private int productValue;
	private int cStats;
	private int bom;
	private int wip;
	private int fg;
	private int sellAble;
	private int delivery;
	private String description;
    private ProductUom uom;
    private String code;
	private String serialNumber;
	private double reward;

	public int getProductValue() {
		return productValue;
	}

	public void setProductValue(int productValue) {
		this.productValue = productValue;
	}

    public ProductUom getUom() {
        return uom;
    }

    public void setUom(ProductUom uom) {
        this.uom = uom;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public String getStockId() {
		return stockId;
	}

	public void setStockId(String stockId) {
		this.stockId = stockId;
	}

//	public UnitOfMeasure getUnitOfMeasure() {
//		return unitOfMeasure;
//	}
//
//	public void setUnitOfMeasure(UnitOfMeasure unitOfMeasure) {
//		this.unitOfMeasure = unitOfMeasure;
//	}

	public Date getExpiredDate() {
		return expiredDate;
	}

	public void setExpiredDate(Date expiredDate) {
		this.expiredDate = expiredDate;
	}

	public Category getParentCategory() {
		return parentCategory;
	}

	public void setParentCategory(Category parentCategory) {
		this.parentCategory = parentCategory;
	}

	public String getBarcodeId() {
		return barcodeId;
	}

	public void setBarcodeId(String barcodeId) {
		this.barcodeId = barcodeId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getBuyPrice() {
		return buyPrice;
	}

	public void setBuyPrice(long buyPrice) {
		this.buyPrice = buyPrice;
	}

	public long getSellPrice() {
		return sellPrice;
	}

	public void setSellPrice(long sellPrice) {
		this.sellPrice = sellPrice;
	}

	public int getQuantityFirst() {
		return quantityFirst;
	}

	public void setQuantityFirst(int quantityFirst) {
		this.quantityFirst = quantityFirst;
	}

	public int getMinQuantity() {
		return minQuantity;
	}

	public void setMinQuantity(int minQuantity) {
		this.minQuantity = minQuantity;
	}

	public int getMaxQuantity() {
		return maxQuantity;
	}

	public void setMaxQuantity(int maxQuantity) {
		this.maxQuantity = maxQuantity;
	}

	public int getcStats() {
		return cStats;
	}

	public void setcStats(int cStats) {
		this.cStats = cStats;
	}

	public int getBom() {
		return bom;
	}

	public void setBom(int bom) {
		this.bom = bom;
	}

	public int getWip() {
		return wip;
	}

	public void setWip(int wip) {
		this.wip = wip;
	}

	public int getFg() {
		return fg;
	}

	public void setFg(int fg) {
		this.fg = fg;
	}

	public int getImage() {
		return image;
	}

	public void setImage(int image) {
		this.image = image;
	}

	public int getSellAble() {
		return sellAble;
	}

	public void setSellAble(int sellAble) {
		this.sellAble = sellAble;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getDelivery() {
		return delivery;
	}

	public void setDelivery(int delivery) {
		this.delivery = delivery;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public double getReward() {
		return reward;
	}

	public void setReward(double reward) {
		this.reward = reward;
	}

	@Override
	public String toString() {
		return name;
	}
}
