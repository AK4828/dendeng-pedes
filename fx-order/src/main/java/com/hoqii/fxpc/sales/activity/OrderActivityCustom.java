package com.hoqii.fxpc.sales.activity;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.adapter.ProductAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.CategoryDatabaseAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.OrderDatabaseAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.OrderMenuDatabaseAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.ProductDatabaseAdapter;
import com.hoqii.fxpc.sales.entity.Category;
import com.hoqii.fxpc.sales.entity.Order;
import com.hoqii.fxpc.sales.entity.OrderMenu;
import com.hoqii.fxpc.sales.entity.Product;
import com.hoqii.fxpc.sales.util.AuthenticationUtils;
import com.hoqii.fxpc.sales.util.ImageUtil;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.TypiconsIcons;
import com.joanzapata.iconify.widget.IconTextView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by miftakhul on 12/2/15.
 */
public class OrderActivityCustom extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageView prodcutThumb, smallProductImage;
    private TextView productName, productCategory, productNameSub, productDesc, orderType, orderPrice, orderDesc, smallProductCategory;
    private IconTextView productPrice, reward;
    private Product product;
    private Category category;
    private ImageLoader imagePreview;
    private Spinner orderSpin;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private LinearLayout linearLayoutDesc;
    private CardView categoryLayout;
    private Button orderButton;


    private ProductDatabaseAdapter productDatabaseAdapter;
    private CategoryDatabaseAdapter categoryDatabaseAdapter;
    private OrderDatabaseAdapter orderDatabaseAdapter;
    private OrderMenuDatabaseAdapter orderMenuDatabaseAdapter;

    private ProductAdapter productAdapter;

    private int mutedColor, lightMutedColor, vibrantColor;
    private String productId, categoryId;
    private long orderMenuPrice;
    private List<Integer> orderCountList = new ArrayList<Integer>();
    private DecimalFormat decimalFormat = new DecimalFormat("#,###");
    private OrderMenu.OrderType orderMenuType;
    private int qty = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_custom);

        init();
        if (getIntent().getExtras() != null) {
            productId = getIntent().getExtras().getString("productId", null);
            qty = getIntent().getIntExtra("qtyUpdate", 0);

            product = productDatabaseAdapter.findAllProductById(productId);
            categoryId = product.getParentCategory().getId();
            category = categoryDatabaseAdapter.findCategoryById(categoryId);
        }

        initSet();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.cart, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home :
                super.onBackPressed();
                break;
            case R.id.menu_cart :
//                setResult(RESULT_OK);
//                finish();
                dialogOrder();
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    private void init() {
        toolbar = (Toolbar) findViewById(R.id.toolbar_order);
        prodcutThumb = (ImageView) findViewById(R.id.order_product_preview);
        smallProductImage = (ImageView) findViewById(R.id.small_product_pic);
        productName = (TextView) findViewById(R.id.order_product_name);
//        productNameSub = (TextView) findViewById(R.id.order_product_name_sub);
        productNameSub = (TextView) findViewById(R.id.small_product_name);
        productPrice = (IconTextView) findViewById(R.id.order_product_price);
        reward = (IconTextView) findViewById(R.id.product_reward);
        productDesc = (TextView) findViewById(R.id.order_product_desc);
        productCategory = (TextView) findViewById(R.id.order_product_category);
        smallProductCategory = (TextView) findViewById(R.id.small_product_category);
        orderType = (TextView) findViewById(R.id.order_type);
        orderPrice = (TextView) findViewById(R.id.order_price);
        orderDesc = (TextView) findViewById(R.id.order_desc);
        orderSpin = (Spinner) findViewById(R.id.order_spin);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapse_toolbar);
        linearLayoutDesc = (LinearLayout) findViewById(R.id.linearLayout_desc);
        categoryLayout = (CardView) findViewById(R.id.layout_category);
        orderButton = (Button) findViewById(R.id.order_button);

        productDatabaseAdapter = new ProductDatabaseAdapter(this);
        categoryDatabaseAdapter = new CategoryDatabaseAdapter(this);
        orderDatabaseAdapter = new OrderDatabaseAdapter(this);
        orderMenuDatabaseAdapter = new OrderMenuDatabaseAdapter(this);
        imagePreview = ImageLoader.getInstance();
    }

    private void initSet() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(new IconDrawable(this, TypiconsIcons.typcn_chevron_left).colorRes(R.color.white).actionBarSize());

        collapsingToolbarLayout.setTitle("Order " + product.getName());
        imagePreview.displayImage("file://" + ImageUtil.getImagePath(this, productId), prodcutThumb);
        imagePreview.displayImage("file://" + ImageUtil.getImagePath(this, productId), smallProductImage);
        productName.setText(product.getName());
        productNameSub.setText(product.getName());
        Log.d(getClass().getSimpleName(), "====================== description : " + product.getDescription());
        if (product.getDescription() != null || product.getDescription().equalsIgnoreCase(null) || product.getDescription().equalsIgnoreCase("") || product.getDescription().equalsIgnoreCase("null")){
            productDesc.setText(product.getDescription());
        }else {
            productDesc.setText("Tidak ada deskripsi");
        }
        productPrice.setText("{typcn-tags}  Rp." + decimalFormat.format(product.getSellPrice()));
        reward.setText("{typcn-star-full-outline}  Reward : " + Double.toString(product.getReward()) + " point");
        Log.d(getClass().getSimpleName(), "====================== reward : " + product.getReward());
        productCategory.setText(category.getName());
        smallProductCategory.setText(category.getName());

        orderMenuType = OrderMenu.OrderType.PURCHASE_ORDER;
//        orderType.setText(orderMenuType.name());

        for (int x = 1; x <= 100; x++) {
            orderCountList.add(x);
        }

        ArrayAdapter<Integer> orderSpinAdapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_dropdown_item, orderCountList);
        orderSpin.setAdapter(orderSpinAdapter);

        // for update order
        if (qty != 0) {
            orderSpin.setSelection(qty - 1);
        }

        orderSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                long productPrice = product.getSellPrice();
                long price = productPrice * (position + 1);

                orderMenuPrice = price;

                orderPrice.setText("Total : Rp." + decimalFormat.format(price));

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

//
        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogOrder();
            }
        });

        Bitmap linearBitmap = BitmapFactory.decodeFile(ImageUtil.getImagePath(this, productId));

        try {
            Palette.from(linearBitmap).generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(Palette palette) {
                    mutedColor = palette.getMutedColor(R.color.colorPrimaryDark);
                    lightMutedColor = palette.getLightMutedColor(R.color.colorPrimaryDark);
                    vibrantColor = palette.getVibrantColor(R.color.colorPrimaryDark);

                    linearLayoutDesc.setBackgroundColor(mutedColor);
                    categoryLayout.setCardBackgroundColor(vibrantColor);
                }
            });
        }catch (IllegalArgumentException e){
            Log.e("Bitmap status",e.getMessage());
        }


    }

    public String saveOrder() {
        Order order = new Order();

        order.setSiteId("");
        order.setOrderType("1");
        order.setReceiptNumber("");
        order.getLogInformation().setCreateBy(AuthenticationUtils.getCurrentAuthentication().getUser().getId());
        order.getLogInformation().setCreateDate(new Date());
        order.getLogInformation().setLastUpdateBy(AuthenticationUtils.getCurrentAuthentication().getUser().getId());
        order.getLogInformation().setSite(AuthenticationUtils.getCurrentAuthentication().getSite().getId());

        order.setStatus(Order.OrderStatus.PROCESSED);

        return orderDatabaseAdapter.saveOrder(order);
    }

    private void dialogOrder(){
        Log.d(getClass().getSimpleName(), " current site id adalah  " + AuthenticationUtils.getCurrentAuthentication().getSite().getId());

        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(getResources().getString(R.string.order));
        alert.setMessage(getResources().getString(R.string.order) + " " + product.getName() + " ?");
        alert.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String orderId = orderDatabaseAdapter.getOrderId();
                int q = Integer.parseInt(orderSpin.getSelectedItem().toString());

                if (orderId == null) {
                    orderId = saveOrder();

                    if (orderMenuDatabaseAdapter.findOrderMenuByProductId(productId) == null) {
                        OrderMenu orderMenu = new OrderMenu();

                        orderMenu.getLogInformation().setCreateBy(AuthenticationUtils.getCurrentAuthentication().getUser().getId());
                        orderMenu.getLogInformation().setLastUpdateBy(AuthenticationUtils.getCurrentAuthentication().getUser().getId());
                        orderMenu.getLogInformation().setSite(AuthenticationUtils.getCurrentAuthentication().getSite().getId());

                        orderMenu.getOrder().setId(orderId);
                        orderMenu.setQty(q);
                        orderMenu.setQtyOrder(q);
                        orderMenu.setProduct(product);
                        orderMenu.setSellPrice(orderMenuPrice);
                        orderMenu.setDescription(orderDesc.getText().toString());
                        orderMenu.setType(orderMenuType.name());

                        orderMenuDatabaseAdapter.saveOrderMenu(orderMenu);
                    } else {
                        OrderMenu tempOrdermenu = orderMenuDatabaseAdapter.findOrderMenuByProductId(productId);

                        OrderMenu orderMenu = new OrderMenu();
                        orderMenu.setId(tempOrdermenu.getId());
                        orderMenu.getLogInformation().setCreateBy(AuthenticationUtils.getCurrentAuthentication().getUser().getId());
                        orderMenu.getLogInformation().setLastUpdateBy(AuthenticationUtils.getCurrentAuthentication().getUser().getId());
                        orderMenu.getLogInformation().setSite(AuthenticationUtils.getCurrentAuthentication().getSite().getId());

                        orderMenu.getOrder().setId(orderId);
                        orderMenu.setQty(q + tempOrdermenu.getQty());
                        orderMenu.setQtyOrder(q + tempOrdermenu.getQtyOrder());
                        orderMenu.setProduct(product);
                        orderMenu.setSellPrice(orderMenuPrice);
                        orderMenu.setDescription(orderDesc.getText().toString());
                        orderMenu.setType(orderMenuType.name());

                        orderMenuDatabaseAdapter.saveOrderMenu(orderMenu);
                    }

                } else if (orderId != null && qty == 0) {
                    if (orderMenuDatabaseAdapter.findOrderMenuByProductId(productId) == null) {
                        OrderMenu orderMenu = new OrderMenu();

                        orderMenu.getLogInformation().setCreateBy(AuthenticationUtils.getCurrentAuthentication().getUser().getId());
                        orderMenu.getLogInformation().setLastUpdateBy(AuthenticationUtils.getCurrentAuthentication().getUser().getId());
                        orderMenu.getLogInformation().setSite(AuthenticationUtils.getCurrentAuthentication().getSite().getId());

                        orderMenu.getOrder().setId(orderId);
                        orderMenu.setQty(q);
                        orderMenu.setQtyOrder(q);
                        orderMenu.setProduct(product);
                        orderMenu.setSellPrice(orderMenuPrice);
                        orderMenu.setDescription(orderDesc.getText().toString());
                        orderMenu.setType(orderMenuType.name());

                        orderMenuDatabaseAdapter.saveOrderMenu(orderMenu);
                    } else {

                        OrderMenu tempOrdermenu = orderMenuDatabaseAdapter.findOrderMenuByProductId(productId);

                        OrderMenu orderMenu = new OrderMenu();
                        orderMenu.setId(tempOrdermenu.getId());
                        orderMenu.getLogInformation().setCreateBy(AuthenticationUtils.getCurrentAuthentication().getUser().getId());
                        orderMenu.getLogInformation().setLastUpdateBy(AuthenticationUtils.getCurrentAuthentication().getUser().getId());
                        orderMenu.getLogInformation().setSite(AuthenticationUtils.getCurrentAuthentication().getSite().getId());

                        orderMenu.getOrder().setId(orderId);
                        orderMenu.setQty(q + tempOrdermenu.getQty());
                        orderMenu.setQtyOrder(q + tempOrdermenu.getQtyOrder());
                        orderMenu.setProduct(product);
                        orderMenu.setSellPrice(orderMenuPrice);
                        orderMenu.setDescription(orderDesc.getText().toString());
                        orderMenu.setType(orderMenuType.name());

                        orderMenuDatabaseAdapter.saveOrderMenu(orderMenu);
                    }
                } else if (orderId != null && qty > 0) {
                    OrderMenu tempOrdermenu = orderMenuDatabaseAdapter.findOrderMenuByProductId(productId);

                    OrderMenu orderMenu = new OrderMenu();
                    orderMenu.setId(tempOrdermenu.getId());
                    orderMenu.getLogInformation().setCreateBy(AuthenticationUtils.getCurrentAuthentication().getUser().getId());
                    orderMenu.getLogInformation().setLastUpdateBy(AuthenticationUtils.getCurrentAuthentication().getUser().getId());
                    orderMenu.getLogInformation().setSite(AuthenticationUtils.getCurrentAuthentication().getSite().getId());

                    orderMenu.getOrder().setId(orderId);
                    orderMenu.setQty(q);
                    orderMenu.setQtyOrder(q);
                    orderMenu.setProduct(product);
                    orderMenu.setSellPrice(orderMenuPrice);
                    orderMenu.setDescription(orderDesc.getText().toString());
                    orderMenu.setType(orderMenuType.name());

                    orderMenuDatabaseAdapter.saveOrderMenu(orderMenu);
                }

                setResult(RESULT_OK);
                finish();
            }
        });

        alert.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        alert.show();
    }


}
