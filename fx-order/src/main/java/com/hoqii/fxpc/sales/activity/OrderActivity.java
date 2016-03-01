package com.hoqii.fxpc.sales.activity;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.content.database.adapter.OrderDatabaseAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.OrderMenuDatabaseAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.ProductDatabaseAdapter;
import com.hoqii.fxpc.sales.entity.Order;
import com.hoqii.fxpc.sales.entity.OrderMenu;
import com.hoqii.fxpc.sales.entity.Product;
import com.hoqii.fxpc.sales.util.AuthenticationUtils;
import com.hoqii.fxpc.sales.util.ImageUtil;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.TypiconsIcons;
import com.joanzapata.iconify.widget.IconTextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by miftakhul on 12/2/15.
 */
public class OrderActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageView prodcutThumb;
    private TextView productName, productPrice, productDesc, orderPrice, orderDesc, totalReward;
    private IconTextView reward;
    private Product product;
    private Spinner orderSpin;
    private Button orderButton;
    private View appShadow;


    private ProductDatabaseAdapter productDatabaseAdapter;
    private OrderDatabaseAdapter orderDatabaseAdapter;
    private OrderMenuDatabaseAdapter orderMenuDatabaseAdapter;

    private boolean isMinLoli = false;
    private String productId;
    private long orderMenuPrice;
    private List<Integer> orderCountList = new ArrayList<Integer>();
    private DecimalFormat decimalFormat = new DecimalFormat("#,###");
    private OrderMenu.OrderType orderMenuType;
    private int qty = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            isMinLoli = true;
        } else {
            isMinLoli = false;
        }

        init();
        if (getIntent().getExtras() != null) {
            productId = getIntent().getExtras().getString("productId", null);
            qty = getIntent().getIntExtra("qtyUpdate", 0);

            product = productDatabaseAdapter.findAllProductById(productId);
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
                dialogOrder();
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    private void init() {
        toolbar = (Toolbar) findViewById(R.id.toolbar_order);
        prodcutThumb = (ImageView) findViewById(R.id.product_image);
        productName = (TextView) findViewById(R.id.order_product_name);
        productPrice = (TextView) findViewById(R.id.order_product_price);
        reward = (IconTextView) findViewById(R.id.product_reward);
        productDesc = (TextView) findViewById(R.id.order_product_desc);
        orderPrice = (TextView) findViewById(R.id.order_price);
        orderDesc = (TextView) findViewById(R.id.order_desc);
        totalReward = (TextView) findViewById(R.id.order_reward);
        orderSpin = (Spinner) findViewById(R.id.order_spin);
        orderButton = (Button) findViewById(R.id.addCart);
//        appShadow = (View) findViewById(R.id.app_shadow);

        productDatabaseAdapter = new ProductDatabaseAdapter(this);
        orderDatabaseAdapter = new OrderDatabaseAdapter(this);
        orderMenuDatabaseAdapter = new OrderMenuDatabaseAdapter(this);
    }

    private void initSet() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(new IconDrawable(this, TypiconsIcons.typcn_chevron_left).colorRes(R.color.white).actionBarSize());

//        if (isMinLoli){
//            appShadow.setVisibility(View.GONE);
//        }else {
//            appShadow.setVisibility(View.VISIBLE);
//        }

        Glide.with(this).load("file://" + ImageUtil.getImagePath(this, productId)).error(R.drawable.no_image).into(prodcutThumb);
        productName.setText(product.getName());

        productPrice.setText("Rp." + decimalFormat.format(product.getSellPrice()));
        reward.setText("Reward : " + Double.toString(product.getReward()) + " Points");
        orderPrice.setText("Total : Rp." + decimalFormat.format(product.getSellPrice()));
        totalReward.setText("Total Reward : "+ product.getReward());
        orderMenuType = OrderMenu.OrderType.PURCHASE_ORDER;

        if (product.getDescription() != null || product.getDescription() != "null" || product.getDescription() != ""){
            productDesc.setText(product.getDescription());
        }else {
            productDesc.setText("No description");
        }

        for (int x = 1; x <= 100; x++) {
            orderCountList.add(x);
        }

        ArrayAdapter<Integer> orderSpinAdapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_dropdown_item, orderCountList);
        orderSpin.setAdapter(orderSpinAdapter);

        // for update order
        if (qty != 0) {
            orderSpin.setSelection(qty - 1);
            orderPrice.setText("Total : Rp." + decimalFormat.format(product.getSellPrice() * qty));
            totalReward.setText("Total Reward : " + product.getReward() * qty);
        }else {
            orderSpin.setSelection(0);
        }

//        orderSpin.setShowNumberPickerDialog(false);
//        orderSpin.setOnValueChangeListener(new OnValueChangeListener() {
//            @Override
//            public boolean onValueChange(SwipeNumberPicker view, int oldValue, int newValue) {
//                boolean isValueOk = (newValue & 1) == 0;
//
//                long productPrice = product.getSellPrice();
//                long price = productPrice * newValue;
//                orderMenuPrice = price;
//                orderPrice.setText("Total : Rp." + decimalFormat.format(price));
//
//                totalReward.setText("Total Reward : " + product.getReward() + newValue);
//
//                return isValueOk;
//            }
//        });

        orderSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                long productPrice = product.getSellPrice();
                long price = productPrice * (int)orderSpin.getSelectedItem();

                orderMenuPrice = price;

                orderPrice.setText("Total price : Rp." + decimalFormat.format(price));
                totalReward.setText("Total Reward : " + (int)product.getReward() * (int)orderSpin.getSelectedItem());

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogOrder();
            }
        });

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
        alert.setTitle("Order");
        alert.setMessage("Order " + product.getName() + " ?");
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String orderId = orderDatabaseAdapter.getOrderId();
                int q = (int)orderSpin.getSelectedItem();

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

        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alert.show();
    }


}
