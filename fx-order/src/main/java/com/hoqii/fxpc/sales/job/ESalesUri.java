package com.hoqii.fxpc.sales.job;

/**
 * Created by meruvian on 30/07/15.
 */
public class ESalesUri {
    public static final String ORDER = "/api/orders";
    public static final String UPDATE_ORDER = "/api/orders/%s";
    public static final String ORDER_MENU = "/api/orders/%s/menu";
    public static final String SHIPMENT = "/api/order/shipments";
    public static final String SHIPMENT_RECEIPT = "/api/order/shipments/shipmentnumber";
    public static final String SERIAL = "/api/order/menu/serialnumbers";
    public static final String SITE = "/api/sites";
    public static final String CONTACT = "/api/contacts";
    public static final String UPDATE_CONTACT = "/api/contacts/%s";

    public static final String GET_ASSIGMENT_DETAIL = "/api/assigments/details/agents/%s";
    public static final String POST_SETTLE = "/api/settle";
    public static final String PUT_ASSIGMENT_DETAIL = "/api/assigments/detail/%s";

}
