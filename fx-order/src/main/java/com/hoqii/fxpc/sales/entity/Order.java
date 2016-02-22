package com.hoqii.fxpc.sales.entity;

import com.hoqii.fxpc.sales.core.DefaultPersistence;
import com.hoqii.fxpc.sales.core.commons.Site;

public class Order extends DefaultPersistence {
	public enum OrderStatus {
		PROCESSED, SENDING, RECEIVED, DONE, CANCELED
	}

	private String receiptNumber;
	private String name;
	private String handphone;
	private String phoneNumber;
	private String address;
	private String email;
	private boolean sameAddress;
	private String nameReceiver;
	private String handphoneReceiver;
	private String phoneNumberReceiver;
	private String addressReceiver;
	private String emailReceiver;
    private String orderType;
    private String siteId;
//	private Contact contact = new Contact();
	private Site site = new Site();
	private Site siteFrom = new Site();

	private OrderStatus status = OrderStatus.PROCESSED;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHandphone() {
		return handphone;
	}

	public void setHandphone(String handphone) {
		this.handphone = handphone;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean isSameAddress() {
		return sameAddress;
	}

	public void setSameAddress(boolean sameAddress) {
		this.sameAddress = sameAddress;
	}

	public String getNameReceiver() {
		return nameReceiver;
	}

	public void setNameReceiver(String nameReceiver) {
		this.nameReceiver = nameReceiver;
	}

	public String getHandphoneReceiver() {
		return handphoneReceiver;
	}

	public void setHandphoneReceiver(String handphoneReceiver) {
		this.handphoneReceiver = handphoneReceiver;
	}

	public String getPhoneNumberReceiver() {
		return phoneNumberReceiver;
	}

	public void setPhoneNumberReceiver(String phoneNumberReceiver) {
		this.phoneNumberReceiver = phoneNumberReceiver;
	}

	public String getAddressReceiver() {
		return addressReceiver;
	}

	public void setAddressReceiver(String addressReceiver) {
		this.addressReceiver = addressReceiver;
	}

	public String getEmailReceiver() {
		return emailReceiver;
	}

	public void setEmailReceiver(String emailReceiver) {
		this.emailReceiver = emailReceiver;
	}

	public String getReceiptNumber() {
		return receiptNumber;
	}

	public void setReceiptNumber(String receiptNumber) {
		this.receiptNumber = receiptNumber;
	}

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

//	public Contact getContact() {
//		return contact;
//	}
//
//	public void setContact(Contact contact) {
//		this.contact = contact;
//	}

	public OrderStatus getStatus() {
		return status;
	}

	public void setStatus(OrderStatus status) {
		this.status = status;
	}

	public Site getSite() {
		return site;
	}

	public void setSite(Site site) {
		this.site = site;
	}

	public Site getSiteFrom() {
		return siteFrom;
	}

	public void setSiteFrom(Site siteFrom) {
		this.siteFrom = siteFrom;
	}
}
