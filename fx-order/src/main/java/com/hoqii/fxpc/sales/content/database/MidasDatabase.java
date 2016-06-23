package com.hoqii.fxpc.sales.content.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.hoqii.fxpc.sales.content.database.model.AssigmentDatabaseModel;
import com.hoqii.fxpc.sales.content.database.model.AssigmentDetailDatabaseModel;
import com.hoqii.fxpc.sales.content.database.model.AssigmentDetailItemDatabaseModel;
import com.hoqii.fxpc.sales.content.database.model.BusinessPartnerDatabaseModel;
import com.hoqii.fxpc.sales.content.database.model.CampaignDatabaseModel;
import com.hoqii.fxpc.sales.content.database.model.CampaignDetailDatabaseModel;
import com.hoqii.fxpc.sales.content.database.model.CartDatabaseModel;
import com.hoqii.fxpc.sales.content.database.model.CartMenuDatabaseModel;
import com.hoqii.fxpc.sales.content.database.model.CategoryDatabaseModel;
import com.hoqii.fxpc.sales.content.database.model.ContactDatabaseModel;
import com.hoqii.fxpc.sales.content.database.model.DefaultPersistenceModel;
import com.hoqii.fxpc.sales.content.database.model.OrderDatabaseModel;
import com.hoqii.fxpc.sales.content.database.model.OrderMenuDatabaseModel;
import com.hoqii.fxpc.sales.content.database.model.OrderMenuImeiDatabaseModel;
import com.hoqii.fxpc.sales.content.database.model.ProductDatabaseModel;
import com.hoqii.fxpc.sales.content.database.model.ProductStoreDatabaseModel;
import com.hoqii.fxpc.sales.content.database.model.ProductUomDatabaseModel;
import com.hoqii.fxpc.sales.content.database.model.SalesOrderDatabaseModel;
import com.hoqii.fxpc.sales.content.database.model.SalesOrderMenuDatabaseModel;
import com.hoqii.fxpc.sales.content.database.model.SalesOrderMenuSerialDatabaseModel;
import com.hoqii.fxpc.sales.content.database.model.SerialNumberDatabaseModel;
import com.hoqii.fxpc.sales.content.database.model.SettleDatabaseModel;
import com.hoqii.fxpc.sales.content.database.model.SiteDatabaseModel;
import com.hoqii.fxpc.sales.content.database.model.SupplierDatabaseModel;
import com.hoqii.fxpc.sales.content.database.model.UserDatabaseModel;

/**
 * Created by meruvian on 07/10/15.
 */
public class MidasDatabase extends SQLiteOpenHelper {
    public static final String DATABASE = "e_sales";
    private static final int VERSION = 1;

    public static final String CATEGORY_TABLE = "category";
    public static final String PRODUCT_TABLE = "product";
    public static final String CAMPAIGN_TABLE = "campaign";
    public static final String CAMPAIGN_DETAIL_TABLE = "campaign_detail";
    public static final String ORDER_TABLE = "sales_order";
    public static final String ORDER_MENU_TABLE = "order_menu";

    public static final String PRODUCT_UOM = "product_uom";
    public static final String CONTACT = "contact";

    public static final String CART_TABLE = "shopping_cart";
    public static final String CART_MENU_TABLE = "cart_menu";

    public static final String PRODUCT_STORE_TABLE = "product_store";
    public static final String USER_TABLE = "user_store";

    public static final String ORDER_MENU_IMEI_TABLE = "order_menu_imei";

    public static final String ASSIGMENT_TABLE = "assigment";
    public static final String ASSIGMENT_DETAIL_TABLE = "assigment_detail";
    public static final String ASSIGMENT_DETAIL_ITEM_TABLE = "assigment_detail_item";
    public static final String SETTLE_TABLE = "settle";

    public static final String SUPPLIER_TABLE = "supplier";

    public static final String BUSINESS_PARTNER = "business_partner";
    public static final String SERIAL_NUMBER = "serial_number";
    public static final String SITE = "site";
    public static final String SO_TABLE = "so_table";
    public static final String SO_MENU_TABLE = "so_menu_table";
    public static final String SO_MENU_SERIAL_TABLE = "so_menu_serial_table";

    private Context context;

    public MidasDatabase(Context context) {
        super(context, DATABASE, null, VERSION);

        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + CATEGORY_TABLE + "("
                + DefaultPersistenceModel.ID + " TEXT PRIMARY KEY, "
                + DefaultPersistenceModel.CREATE_BY + " TEXT, "
                + DefaultPersistenceModel.CREATE_DATE + " INTEGER, "
                + DefaultPersistenceModel.UPDATE_BY + " TEXT, "
                + DefaultPersistenceModel.UPDATE_DATE + " INTEGER, "
                + DefaultPersistenceModel.REF_ID + " TEXT, "
                + DefaultPersistenceModel.STATUS_FLAG + " INTEGER, "
                + DefaultPersistenceModel.SITE_ID + " TEXT, "
                + DefaultPersistenceModel.SYNC_STATUS + " INTEGER, "
                + CategoryDatabaseModel.PARENT_CATEGORY_ID + " TEXT, "
                + CategoryDatabaseModel.CATEGORY_NAME + " TEXT)");

        db.execSQL("CREATE TABLE " + PRODUCT_TABLE + "("
                + DefaultPersistenceModel.ID + " TEXT PRIMARY KEY, "
                + DefaultPersistenceModel.CREATE_BY + " TEXT, "
                + DefaultPersistenceModel.CREATE_DATE + " INTEGER, "
                + DefaultPersistenceModel.UPDATE_BY + " TEXT, "
                + DefaultPersistenceModel.UPDATE_DATE + " INTEGER, "
                + DefaultPersistenceModel.STATUS_FLAG + " INTEGER, "
                + DefaultPersistenceModel.REF_ID + " TEXT, "
                + DefaultPersistenceModel.SITE_ID + " TEXT, "
                + DefaultPersistenceModel.SYNC_STATUS + " INTEGER, "
                + ProductDatabaseModel.NAME + " TEXT, "
                + ProductDatabaseModel.DESCRIPTION + " TEXT, "
                + ProductDatabaseModel.PRICE + " TEXT, "
                + ProductDatabaseModel.IMAGE + " INTEGER, "
                + ProductDatabaseModel.PARENT_CATEGORY_ID + " TEXT,"
                + ProductDatabaseModel.CATEGORY_ID + " TEXT,"
                + ProductDatabaseModel.PRODUCT_VALUE + " TEXT,"
                + ProductDatabaseModel.MIN_QUANTITY + " TEXT,"
                + ProductDatabaseModel.MAX_QUANTITY + " TEXT,"
                + ProductDatabaseModel.UOM_ID + " TEXT, "
                + ProductDatabaseModel.CODE + " TEXT, "
                + ProductDatabaseModel.FG + " INTEGER, "
                + ProductDatabaseModel.SELL_ABLE + " INTEGER, "
                + ProductDatabaseModel.REWARD + " DOUBLE, "
                + ProductDatabaseModel.COMPOSITION_STATUS + " INTEGER)");

        db.execSQL("CREATE TABLE " + CAMPAIGN_TABLE + "("
                + DefaultPersistenceModel.ID + " TEXT PRIMARY KEY, "
                + DefaultPersistenceModel.CREATE_BY + " TEXT, "
                + DefaultPersistenceModel.CREATE_DATE + " INTEGER, "
                + DefaultPersistenceModel.UPDATE_BY + " TEXT, "
                + DefaultPersistenceModel.UPDATE_DATE + " INTEGER, "
                + DefaultPersistenceModel.REF_ID + " TEXT, "
                + DefaultPersistenceModel.STATUS_FLAG + " INTEGER, "
                + DefaultPersistenceModel.SITE_ID + " TEXT, "
                + DefaultPersistenceModel.SYNC_STATUS + " INTEGER, "
                + CampaignDatabaseModel.NAME + " TEXT, "
                + CampaignDatabaseModel.DESCRIPTION + " TEXT, "
                + CampaignDatabaseModel.SHOW_ON_ANDROID + " INTEGER)");

        db.execSQL("CREATE TABLE " + CAMPAIGN_DETAIL_TABLE + "("
                + DefaultPersistenceModel.ID + " TEXT PRIMARY KEY, "
                + DefaultPersistenceModel.CREATE_BY + " TEXT, "
                + DefaultPersistenceModel.CREATE_DATE + " INTEGER, "
                + DefaultPersistenceModel.UPDATE_BY + " TEXT, "
                + DefaultPersistenceModel.UPDATE_DATE + " INTEGER, "
                + DefaultPersistenceModel.REF_ID + " TEXT, "
                + DefaultPersistenceModel.STATUS_FLAG + " INTEGER, "
                + DefaultPersistenceModel.SITE_ID + " TEXT, "
                + DefaultPersistenceModel.SYNC_STATUS + " INTEGER, "
                + CampaignDetailDatabaseModel.CAMPAIGN_ID + " TEXT, "
                + CampaignDetailDatabaseModel.DESCRIPTION + " TEXT, "
                + CampaignDetailDatabaseModel.PATH + " TEXT)");

        db.execSQL("CREATE TABLE " + ORDER_TABLE + " ("
                + DefaultPersistenceModel.ID + " TEXT, "
                + DefaultPersistenceModel.CREATE_BY + " TEXT, "
                + DefaultPersistenceModel.CREATE_DATE + " INTEGER, "
                + DefaultPersistenceModel.UPDATE_BY + " TEXT, "
                + DefaultPersistenceModel.UPDATE_DATE + " INTEGER, "
                + DefaultPersistenceModel.STATUS_FLAG + " INTEGER, "
                + DefaultPersistenceModel.SITE_ID + " TEXT, "
                + DefaultPersistenceModel.REF_ID + " TEXT, "
                + DefaultPersistenceModel.SYNC_STATUS + " INTEGER, "
                + OrderDatabaseModel.ORDER_TYPE + " TEXT, "
                + OrderDatabaseModel.SITE_ORDER_ID + " TEXT, "
                + OrderDatabaseModel.RECIEPT_NUMBER + " TEXT, "
                + OrderDatabaseModel.CONTACTID + " TEXT, "
                + OrderDatabaseModel.STATUS + " TEXT)");

        db.execSQL("CREATE TABLE " + ORDER_MENU_TABLE + " ("
                + DefaultPersistenceModel.ID + " TEXT, "
                + DefaultPersistenceModel.CREATE_BY + " TEXT, "
                + DefaultPersistenceModel.CREATE_DATE + " INTEGER, "
                + DefaultPersistenceModel.UPDATE_BY + " TEXT, "
                + DefaultPersistenceModel.UPDATE_DATE + " INTEGER, "
                + DefaultPersistenceModel.SITE_ID + " TEXT, "
                + DefaultPersistenceModel.STATUS_FLAG + " INTEGER, "
                + DefaultPersistenceModel.SYNC_STATUS + " INTEGER, "
                + DefaultPersistenceModel.REF_ID + " TEXT, "
                + OrderMenuDatabaseModel.QUANTITY + " INTEGER, "
                + OrderMenuDatabaseModel.QUANTITY_ORDER + " INTEGER, "
                + OrderMenuDatabaseModel.DELIVERY_STATUS + " INTEGER, "
                + OrderMenuDatabaseModel.PRODUCT_ID + " TEXT, "
                + OrderMenuDatabaseModel.ORDER_ID + " TEXT, "
                + OrderMenuDatabaseModel.DISCOUNT_NOMINAL + " TEXT, "
                + OrderMenuDatabaseModel.DISCOUNT_PERCENT + " TEXT, "
                + OrderMenuDatabaseModel.DISCOUNT_NAME + " TEXT, "
                + OrderMenuDatabaseModel.DESC + " TEXT, "
                + OrderMenuDatabaseModel.PRICE + " TEXT, "
                + OrderMenuDatabaseModel.STATUS + " TEXT, "
                + OrderMenuDatabaseModel.TYPE + " TEXT )");

        db.execSQL("CREATE TABLE " + CART_TABLE + " ("
                + DefaultPersistenceModel.ID + " TEXT, "
                + DefaultPersistenceModel.CREATE_BY + " TEXT, "
                + DefaultPersistenceModel.CREATE_DATE + " INTEGER, "
                + DefaultPersistenceModel.UPDATE_BY + " TEXT, "
                + DefaultPersistenceModel.UPDATE_DATE + " INTEGER, "
                + DefaultPersistenceModel.STATUS_FLAG + " INTEGER, "
                + DefaultPersistenceModel.SITE_ID + " TEXT, "
                + DefaultPersistenceModel.REF_ID + " TEXT, "
                + DefaultPersistenceModel.SYNC_STATUS + " INTEGER, "
                + CartDatabaseModel.ORDER_TYPE + " TEXT, "
                + CartDatabaseModel.RECIEPT_NUMBER + " TEXT)");

        db.execSQL("CREATE TABLE " + CART_MENU_TABLE + " ("
                + DefaultPersistenceModel.ID + " TEXT, "
                + DefaultPersistenceModel.CREATE_BY + " TEXT, "
                + DefaultPersistenceModel.CREATE_DATE + " INTEGER, "
                + DefaultPersistenceModel.UPDATE_BY + " TEXT, "
                + DefaultPersistenceModel.UPDATE_DATE + " INTEGER, "
                + DefaultPersistenceModel.SITE_ID + " TEXT, "
                + DefaultPersistenceModel.STATUS_FLAG + " INTEGER, "
                + DefaultPersistenceModel.SYNC_STATUS + " INTEGER, "
                + DefaultPersistenceModel.REF_ID + " TEXT, "
                + CartMenuDatabaseModel.QUANTITY + " INTEGER, "
                + CartMenuDatabaseModel.DELIVERY_STATUS + " INTEGER, "
                + CartMenuDatabaseModel.PRODUCT_ID + " TEXT, "
                + CartMenuDatabaseModel.CART_ID + " TEXT, "
                + CartMenuDatabaseModel.DISCOUNT_NOMINAL + " TEXT, "
                + CartMenuDatabaseModel.DISCOUNT_PERCENT + " TEXT, "
                + CartMenuDatabaseModel.DISCOUNT_NAME + " TEXT, "
                + CartMenuDatabaseModel.DESC + " TEXT, "
                + CartMenuDatabaseModel.PRICE + " TEXT)");

        db.execSQL("CREATE TABLE " + PRODUCT_UOM + "("
                + DefaultPersistenceModel.ID + " TEXT PRIMARY KEY, "
                + DefaultPersistenceModel.CREATE_BY + " TEXT, "
                + DefaultPersistenceModel.CREATE_DATE + " INTEGER, "
                + DefaultPersistenceModel.UPDATE_BY + " TEXT, "
                + DefaultPersistenceModel.UPDATE_DATE + " INTEGER, "
                + DefaultPersistenceModel.REF_ID + " TEXT, "
                + DefaultPersistenceModel.STATUS_FLAG + " INTEGER, "
                + DefaultPersistenceModel.SITE_ID + " TEXT, "
                + DefaultPersistenceModel.SYNC_STATUS + " INTEGER, "
                + ProductUomDatabaseModel.NAME + " TEXT, "
                + ProductUomDatabaseModel.DESCRIPTION + " TEXT)");

        db.execSQL("CREATE TABLE " + CONTACT + "("
                + DefaultPersistenceModel.ID + " TEXT PRIMARY KEY, "
                + DefaultPersistenceModel.CREATE_BY + " TEXT, "
                + DefaultPersistenceModel.CREATE_DATE + " INTEGER, "
                + DefaultPersistenceModel.UPDATE_BY + " TEXT, "
                + DefaultPersistenceModel.UPDATE_DATE + " INTEGER, "
                + DefaultPersistenceModel.REF_ID + " TEXT, "
                + DefaultPersistenceModel.STATUS_FLAG + " INTEGER, "
                + DefaultPersistenceModel.SITE_ID + " TEXT, "
                + DefaultPersistenceModel.SYNC_STATUS + " INTEGER, "
                + ContactDatabaseModel.FIRSTNAME + " TEXT, "
                + ContactDatabaseModel.LASTNAME + " TEXT, "
                + ContactDatabaseModel.OFFICE_PHONE + " TEXT, "
                + ContactDatabaseModel.MOBILE + " TEXT, "
                + ContactDatabaseModel.HOME_PHONE + " TEXT, "
                + ContactDatabaseModel.OTHER_PHONE + " TEXT, "
                + ContactDatabaseModel.FAX + " TEXT, "
                + ContactDatabaseModel.EMAIL + " TEXT, "
                + ContactDatabaseModel.OTHER_EMAIL + " TEXT, "
                + ContactDatabaseModel.ASSISTANT + " TEXT, "
                + ContactDatabaseModel.ASSISTANT_PHONE + " TEXT, "
                + ContactDatabaseModel.ADDRESS + " TEXT, "
                + ContactDatabaseModel.CITY + " TEXT, "
                + ContactDatabaseModel.ZIPCODE + " TEXT, "
                + ContactDatabaseModel.COUNTRY + " TEXT, "
                + ContactDatabaseModel.DESCRIPTION + " TEXT, "
                + ContactDatabaseModel.BUSINESS_PARTNER_ID + " TEXT )");

        db.execSQL("CREATE TABLE " + PRODUCT_STORE_TABLE + "("
                + DefaultPersistenceModel.ID + " TEXT PRIMARY KEY, "
                + DefaultPersistenceModel.CREATE_BY + " TEXT, "
                + DefaultPersistenceModel.CREATE_DATE + " INTEGER, "
                + DefaultPersistenceModel.UPDATE_BY + " TEXT, "
                + DefaultPersistenceModel.UPDATE_DATE + " INTEGER, "
                + DefaultPersistenceModel.REF_ID + " TEXT, "
                + DefaultPersistenceModel.STATUS_FLAG + " INTEGER, "
                + DefaultPersistenceModel.SITE_ID + " TEXT, "
                + DefaultPersistenceModel.SYNC_STATUS + " INTEGER, "
                + ProductStoreDatabaseModel.PRODUCT_ID + " TEXT, "
                + ProductStoreDatabaseModel.SELL_PRICE + " TEXT, "
                + ProductStoreDatabaseModel.INCENTIVE + " TEXT, "
                + ProductStoreDatabaseModel.STOCK + " TEXT)");

        db.execSQL("CREATE TABLE " + USER_TABLE + "("
                + DefaultPersistenceModel.ID + " TEXT PRIMARY KEY, "
                + DefaultPersistenceModel.CREATE_BY + " TEXT, "
                + DefaultPersistenceModel.CREATE_DATE + " INTEGER, "
                + DefaultPersistenceModel.UPDATE_BY + " TEXT, "
                + DefaultPersistenceModel.UPDATE_DATE + " INTEGER, "
                + DefaultPersistenceModel.REF_ID + " TEXT, "
                + DefaultPersistenceModel.STATUS_FLAG + " INTEGER, "
                + DefaultPersistenceModel.SITE_ID + " TEXT, "
                + DefaultPersistenceModel.SYNC_STATUS + " INTEGER, "
                + UserDatabaseModel.USERNAME + " TEXT, "
                + UserDatabaseModel.PASSWORD + " TEXT, "
                + UserDatabaseModel.EMAIL + " TEXT, "
                + UserDatabaseModel.NAME_PREFIX + " TEXT, "
                + UserDatabaseModel.NAME_FIRST + " TEXT, "
                + UserDatabaseModel.NAME_MIDDLE + " TEXT, "
                + UserDatabaseModel.NAME_LAST + " TEXT, "
                + UserDatabaseModel.ADDRESS_STREET1 + " TEXT, "
                + UserDatabaseModel.ADDRESS_STREET2 + " TEXT, "
                + UserDatabaseModel.ADDRESS_CITY + " TEXT, "
                + UserDatabaseModel.ADDRESS_STATE + " TEXT, "
                + UserDatabaseModel.ADDRESS_ZIP + " TEXT, "
                + UserDatabaseModel.BANK_NAME + " TEXT, "
                + UserDatabaseModel.ACCOUNT_NUMBER + " TEXT, "
                + UserDatabaseModel.ACCOUNT_NAME + " TEXT, "
                + UserDatabaseModel.PHONE + " TEXT, "
                + UserDatabaseModel.UPLINE + " TEXT, "
                + UserDatabaseModel.REFERENCE + " TEXT, "
                + UserDatabaseModel.AGENT_CODE + " TEXT)");

        db.execSQL("CREATE TABLE " + ORDER_MENU_IMEI_TABLE + "("
                + DefaultPersistenceModel.ID + " TEXT PRIMARY KEY, "
                + DefaultPersistenceModel.CREATE_BY + " TEXT, "
                + DefaultPersistenceModel.CREATE_DATE + " INTEGER, "
                + DefaultPersistenceModel.UPDATE_BY + " TEXT, "
                + DefaultPersistenceModel.UPDATE_DATE + " INTEGER, "
                + DefaultPersistenceModel.REF_ID + " TEXT, "
                + DefaultPersistenceModel.STATUS_FLAG + " INTEGER, "
                + DefaultPersistenceModel.SITE_ID + " TEXT, "
                + DefaultPersistenceModel.SYNC_STATUS + " INTEGER, "
                + OrderMenuImeiDatabaseModel.ORDER_MENU_ID + " TEXT, "
                + OrderMenuImeiDatabaseModel.IMEI + " TEXT)");

        db.execSQL("CREATE TABLE " + ASSIGMENT_TABLE + "("
                + DefaultPersistenceModel.ID + " TEXT PRIMARY KEY, "
                + DefaultPersistenceModel.CREATE_BY + " TEXT, "
                + DefaultPersistenceModel.CREATE_DATE + " INTEGER, "
                + DefaultPersistenceModel.UPDATE_BY + " TEXT, "
                + DefaultPersistenceModel.UPDATE_DATE + " INTEGER, "
                + DefaultPersistenceModel.REF_ID + " TEXT, "
                + DefaultPersistenceModel.STATUS_FLAG + " INTEGER, "
                + DefaultPersistenceModel.SITE_ID + " TEXT, "
                + DefaultPersistenceModel.SYNC_STATUS + " INTEGER, "
                + AssigmentDatabaseModel.COLLECTOR_ID + " TEXT, "
                + AssigmentDatabaseModel.STATUS + " TEXT)");

        db.execSQL("CREATE TABLE " + ASSIGMENT_DETAIL_TABLE + "("
                + DefaultPersistenceModel.ID + " TEXT PRIMARY KEY, "
                + DefaultPersistenceModel.CREATE_BY + " TEXT, "
                + DefaultPersistenceModel.CREATE_DATE + " INTEGER, "
                + DefaultPersistenceModel.UPDATE_BY + " TEXT, "
                + DefaultPersistenceModel.UPDATE_DATE + " INTEGER, "
                + DefaultPersistenceModel.REF_ID + " TEXT, "
                + DefaultPersistenceModel.STATUS_FLAG + " INTEGER, "
                + DefaultPersistenceModel.SITE_ID + " TEXT, "
                + DefaultPersistenceModel.SYNC_STATUS + " INTEGER, "
                + AssigmentDetailDatabaseModel.ASSIGMENT_ID + " TEXT, "
                + AssigmentDetailDatabaseModel.AGENT_ID + " TEXT)");

        db.execSQL("CREATE TABLE " + ASSIGMENT_DETAIL_ITEM_TABLE + "("
                + DefaultPersistenceModel.ID + " TEXT PRIMARY KEY, "
                + DefaultPersistenceModel.CREATE_BY + " TEXT, "
                + DefaultPersistenceModel.CREATE_DATE + " INTEGER, "
                + DefaultPersistenceModel.UPDATE_BY + " TEXT, "
                + DefaultPersistenceModel.UPDATE_DATE + " INTEGER, "
                + DefaultPersistenceModel.REF_ID + " TEXT, "
                + DefaultPersistenceModel.STATUS_FLAG + " INTEGER, "
                + DefaultPersistenceModel.SITE_ID + " TEXT, "
                + DefaultPersistenceModel.SYNC_STATUS + " INTEGER, "
                + AssigmentDetailItemDatabaseModel.ASSIGMENT_DETAIL_ID + " TEXT, "
                + AssigmentDetailItemDatabaseModel.PRODUCT_ID + " TEXT, "
                + AssigmentDetailItemDatabaseModel.QUANTITY + " TEXT)");

        db.execSQL("CREATE TABLE " + SETTLE_TABLE + "("
                + DefaultPersistenceModel.ID + " TEXT PRIMARY KEY, "
                + DefaultPersistenceModel.CREATE_BY + " TEXT, "
                + DefaultPersistenceModel.CREATE_DATE + " INTEGER, "
                + DefaultPersistenceModel.UPDATE_BY + " TEXT, "
                + DefaultPersistenceModel.UPDATE_DATE + " INTEGER, "
                + DefaultPersistenceModel.REF_ID + " TEXT, "
                + DefaultPersistenceModel.STATUS_FLAG + " INTEGER, "
                + DefaultPersistenceModel.SITE_ID + " TEXT, "
                + DefaultPersistenceModel.SYNC_STATUS + " INTEGER, "
                + SettleDatabaseModel.ASSIGMENT_DETAIL_ID + " TEXT, "
                + SettleDatabaseModel.PRODUCT_ID + " TEXT, "
                + SettleDatabaseModel.SELL_PRICE + " TEXT, "
                + SettleDatabaseModel.QUANTITY + " TEXT)");

        db.execSQL("CREATE TABLE " + SUPPLIER_TABLE + "("
                + DefaultPersistenceModel.ID + " TEXT PRIMARY KEY, "
                + DefaultPersistenceModel.CREATE_BY + " TEXT, "
                + DefaultPersistenceModel.CREATE_DATE + " INTEGER, "
                + DefaultPersistenceModel.UPDATE_BY + " TEXT, "
                + DefaultPersistenceModel.UPDATE_DATE + " INTEGER, "
                + DefaultPersistenceModel.REF_ID + " TEXT, "
                + DefaultPersistenceModel.STATUS_FLAG + " INTEGER, "
                + DefaultPersistenceModel.SITE_ID + " TEXT, "
                + DefaultPersistenceModel.SYNC_STATUS + " INTEGER, "
                + SupplierDatabaseModel.NAME + " TEXT, "
                + SupplierDatabaseModel.PHONE + " TEXT, "
                + SupplierDatabaseModel.EMAIL + " TEXT, "
                + SupplierDatabaseModel.COMPHANY + " TEXT )");

        db.execSQL("CREATE TABLE " + BUSINESS_PARTNER + "("
                + DefaultPersistenceModel.ID + " TEXT PRIMARY KEY, "
                + DefaultPersistenceModel.CREATE_BY + " TEXT, "
                + DefaultPersistenceModel.CREATE_DATE + " INTEGER, "
                + DefaultPersistenceModel.UPDATE_BY + " TEXT, "
                + DefaultPersistenceModel.UPDATE_DATE + " INTEGER, "
                + DefaultPersistenceModel.REF_ID + " TEXT, "
                + DefaultPersistenceModel.STATUS_FLAG + " INTEGER, "
                + DefaultPersistenceModel.SITE_ID + " TEXT, "
                + DefaultPersistenceModel.SYNC_STATUS + " INTEGER, "
                + BusinessPartnerDatabaseModel.NAME + " TEXT, "
                + BusinessPartnerDatabaseModel.OFFICEPHONE + " TEXT, "
                + BusinessPartnerDatabaseModel.FAX + " TEXT, "
                + BusinessPartnerDatabaseModel.EMAIL + " TEXT, "
                + BusinessPartnerDatabaseModel.OTHEREMAIL + " TEXT, "
                + BusinessPartnerDatabaseModel.ADDRESS + " TEXT, "
                + BusinessPartnerDatabaseModel.CITY + " TEXT, "
                + BusinessPartnerDatabaseModel.ZIPCODE + " TEXT, "
                + BusinessPartnerDatabaseModel.COUNTRY + " TEXT, "
                + BusinessPartnerDatabaseModel.DESCRIPTION + " TEXT )");

        db.execSQL("CREATE TABLE " + SERIAL_NUMBER + "("
                + DefaultPersistenceModel.ID + " TEXT PRIMARY KEY, "
                + DefaultPersistenceModel.CREATE_BY + " TEXT, "
                + DefaultPersistenceModel.CREATE_DATE + " INTEGER, "
                + DefaultPersistenceModel.UPDATE_BY + " TEXT, "
                + DefaultPersistenceModel.UPDATE_DATE + " INTEGER, "
                + DefaultPersistenceModel.REF_ID + " TEXT, "
                + DefaultPersistenceModel.STATUS_FLAG + " INTEGER, "
                + DefaultPersistenceModel.SITE_ID + " TEXT, "
                + DefaultPersistenceModel.SYNC_STATUS + " INTEGER, "
                + SerialNumberDatabaseModel.OrderId + " TEXT, "
                + SerialNumberDatabaseModel.OrderMenuId + " TEXT, "
                + SerialNumberDatabaseModel.ShipmentId + " TEXT, "
                + SerialNumberDatabaseModel.SerialNumber + " TEXT )");

        db.execSQL("CREATE TABLE " + SITE + "("
                + DefaultPersistenceModel.ID + " TEXT PRIMARY KEY, "
                + DefaultPersistenceModel.CREATE_BY + " TEXT, "
                + DefaultPersistenceModel.CREATE_DATE + " INTEGER, "
                + DefaultPersistenceModel.UPDATE_BY + " TEXT, "
                + DefaultPersistenceModel.UPDATE_DATE + " INTEGER, "
                + DefaultPersistenceModel.REF_ID + " TEXT, "
                + DefaultPersistenceModel.STATUS_FLAG + " INTEGER, "
                + DefaultPersistenceModel.SITE_ID + " TEXT, "
                + DefaultPersistenceModel.SYNC_STATUS + " INTEGER, "
                + SiteDatabaseModel.NAME + " TEXT, "
                + SiteDatabaseModel.DESCRIPTION + " TEXT, "
                + SiteDatabaseModel.TITLE + " TEXT, "
                + SiteDatabaseModel.URL_BRANDING + " TEXT, "
                + SiteDatabaseModel.SITE_URL + " TEXT, "
                + SiteDatabaseModel.ADMIN_EMAIL + " TEXT, "
                + SiteDatabaseModel.NOTIFY_FLAG + " TEXT, "
                + SiteDatabaseModel.NOTIFY_EMAIL + " TEXT, "
                + SiteDatabaseModel.NOTIFY_FROM + " TEXT, "
                + SiteDatabaseModel.NOTIFY_MESSAGE + " TEXT, "
                + SiteDatabaseModel.WORKSPACE_TYPE + " TEXT, "
                + SiteDatabaseModel.VIRTUALHOST + " TEXT, "
                + SiteDatabaseModel.PATH + " TEXT, "
                + SiteDatabaseModel.LEVEL + " TEXT, "
                + SiteDatabaseModel.THEME + " TEXT, "
                + SiteDatabaseModel.VERY_TRANS_ID + " TEXT, "
                + SiteDatabaseModel.ADDRESS + " TEXT, "
                + SiteDatabaseModel.PHONE + " TEXT, "
                + SiteDatabaseModel.FAX + " TEXT, "
                + SiteDatabaseModel.EMAIL + " TEXT, "
                + SiteDatabaseModel.NPWP + " TEXT, "
                + SiteDatabaseModel.POSTAL_CODE + " TEXT, "
                + SiteDatabaseModel.CITY + " TEXT, "
                + SiteDatabaseModel.TYPE + " TEXT )");

        db.execSQL("CREATE TABLE " + SO_TABLE + " ("
                + DefaultPersistenceModel.ID + " TEXT, "
                + DefaultPersistenceModel.CREATE_BY + " TEXT, "
                + DefaultPersistenceModel.CREATE_DATE + " INTEGER, "
                + DefaultPersistenceModel.UPDATE_BY + " TEXT, "
                + DefaultPersistenceModel.UPDATE_DATE + " INTEGER, "
                + DefaultPersistenceModel.STATUS_FLAG + " INTEGER, "
                + DefaultPersistenceModel.SITE_ID + " TEXT, "
                + DefaultPersistenceModel.REF_ID + " TEXT, "
                + DefaultPersistenceModel.SYNC_STATUS + " INTEGER, "
                + SalesOrderDatabaseModel.NAME + " TEXT, "
                + SalesOrderDatabaseModel.EMAIL + " TEXT, "
                + SalesOrderDatabaseModel.ADDRESS + " TEXT, "
                + SalesOrderDatabaseModel.TELEPHONE + " TEXT, "
                + SalesOrderDatabaseModel.SITE_FROM_ORDER_ID + " TEXT, "
                + SalesOrderDatabaseModel.RECIEPT_NUMBER + " TEXT, "
                + SalesOrderDatabaseModel.STATUS + " TEXT)");

        db.execSQL("CREATE TABLE " + SO_MENU_TABLE + " ("
                + DefaultPersistenceModel.ID + " TEXT, "
                + DefaultPersistenceModel.CREATE_BY + " TEXT, "
                + DefaultPersistenceModel.CREATE_DATE + " INTEGER, "
                + DefaultPersistenceModel.UPDATE_BY + " TEXT, "
                + DefaultPersistenceModel.UPDATE_DATE + " INTEGER, "
                + DefaultPersistenceModel.SITE_ID + " TEXT, "
                + DefaultPersistenceModel.STATUS_FLAG + " INTEGER, "
                + DefaultPersistenceModel.SYNC_STATUS + " INTEGER, "
                + DefaultPersistenceModel.REF_ID + " TEXT, "
                + SalesOrderMenuDatabaseModel.QUANTITY + " INTEGER, "
                + SalesOrderMenuDatabaseModel.QUANTITY_SALES_ORDER + " INTEGER, "
                + SalesOrderMenuDatabaseModel.DELIVERY_STATUS + " INTEGER, "
                + SalesOrderMenuDatabaseModel.PRODUCT_ID + " TEXT, "
                + SalesOrderMenuDatabaseModel.SALES_ORDER_ID + " TEXT, "
                + SalesOrderMenuDatabaseModel.DISCOUNT_NOMINAL + " TEXT, "
                + SalesOrderMenuDatabaseModel.DISCOUNT_PERCENT + " TEXT, "
                + SalesOrderMenuDatabaseModel.DISCOUNT_NAME + " TEXT, "
                + SalesOrderMenuDatabaseModel.DESC + " TEXT, "
                + SalesOrderMenuDatabaseModel.PRICE + " TEXT, "
                + SalesOrderMenuDatabaseModel.STATUS + " TEXT, "
                + SalesOrderMenuDatabaseModel.TYPE + " TEXT )");

        db.execSQL("CREATE TABLE " + SO_MENU_SERIAL_TABLE + " ("
                + DefaultPersistenceModel.ID + " TEXT, "
                + DefaultPersistenceModel.CREATE_BY + " TEXT, "
                + DefaultPersistenceModel.CREATE_DATE + " INTEGER, "
                + DefaultPersistenceModel.UPDATE_BY + " TEXT, "
                + DefaultPersistenceModel.UPDATE_DATE + " INTEGER, "
                + DefaultPersistenceModel.SITE_ID + " TEXT, "
                + DefaultPersistenceModel.STATUS_FLAG + " INTEGER, "
                + DefaultPersistenceModel.SYNC_STATUS + " INTEGER, "
                + DefaultPersistenceModel.REF_ID + " TEXT, "
                + SalesOrderMenuSerialDatabaseModel.SALES_ORDER_MENU_ID + " TEXT, "
                + SalesOrderMenuSerialDatabaseModel.SERIAL + " TEXT )");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {

        }
    }
}
