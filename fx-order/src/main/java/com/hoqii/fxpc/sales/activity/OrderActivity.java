package com.hoqii.fxpc.sales.activity;

import android.content.DialogInterface;
import android.graphics.Color;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import org.w3c.dom.Text;

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
    private EditText orderCount;
    private IconTextView reward;
    private Product product;
    private OrderMenu orderMenu;
    private Spinner orderSpin;
    private Button orderButton;
    private ImageButton btnMin, btnPlus;
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
            orderMenu = orderMenuDatabaseAdapter.findOrderMenuByProductId(productId);
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
//        orderPrice = (TextView) findViewById(R.id.order_price);
//        orderDesc = (TextView) findViewById(R.id.order_desc);
//        totalReward = (TextView) findViewById(R.id.order_reward);
//        orderSpin = (Spinner) findViewById(R.id.order_spin);
        orderButton = (Button) findViewById(R.id.addCart);
        orderCount = (EditText) findViewById(R.id.order_count);
        btnMin = (ImageButton) findViewById(R.id.btn_min);
        btnPlus = (ImageButton) findViewById(R.id.btn_plus);
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
        Log.d(getClass().getSimpleName(), product.getDescription());

        Glide.with(this).load("file://" + ImageUtil.getImagePath(this, productId)).error(R.drawable.no_image).into(prodcutThumb);
        productName.setText(product.getName());

        productPrice.setText("Rp." + decimalFormat.format(product.getSellPrice()));
        reward.setText("Reward : " + Double.toString(product.getReward()) + " Points");
//        orderPrice.setText("Total : Rp." + decimalFormat.format(product.getSellPrice()));
//        totalReward.setText("Total Reward : "+ product.getReward());
        orderMenuType = OrderMenu.OrderType.PURCHASE_ORDER;

        if (product.getDescription() != null || product.getDescription() != "null" || product.getDescription() != ""){
            String temp = product.getDescription();
            final String desc = temp.replace("\n"," ");

            int length = desc.length();
            if (length > 100){
                final boolean[] clicked = {false};
                int cut = length / 2;
                final String newDesc = desc.substring(0, cut);
                productDesc.setText(newDesc + "\n\nooo");

                productDesc.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (clicked[0] == false) {
                            clicked[0] = true;
                            productDesc.setText(desc);
                        }else {
                            clicked[0] = false;
                            productDesc.setText(newDesc + "\n\nooo");
                        }
                    }
                });
            }else {
                productDesc.setText(desc);
            }

        } else {
            productDesc.setText("No description");
        }

        for (int x = 1; x <= 100; x++) {
            orderCountList.add(x);
        }

        // for update order
        if (qty != 0) {
            orderCount.setText(Integer.toString(qty));
            orderButton.setText("Update order");
        }else {
            orderCount.setText(Integer.toString(1));
        }

        btnMin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addOrderCount(-1);
            }
        });
        btnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addOrderCount(1);
            }
        });
        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogOrder();
            }
        });

    }
    private void addOrderCount(int count){
        int c = Integer.parseInt(orderCount.getText().toString());
        if (count > 0){
            c += count;
            orderCount.setText(Integer.toString(c));
        }
        if (count < 0){
            if (c != 1) {
                c += count;
                orderCount.setText(Integer.toString(c));
            }
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
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        View view = getLayoutInflater().from(OrderActivity.this).inflate(R.layout.view_order_desc, null);
        orderDesc = (TextView) view.findViewById(R.id.order_desc);
        final LinearLayout layoutDesc = (LinearLayout) view.findViewById(R.id.layout_desc);
        final Button addOrderDesc = (Button) view.findViewById(R.id.btn_addOrderDesc);

        if (!isMinLoli){
            addOrderDesc.setTextColor(getResources().getColor(R.color.colorAccent));
        }
        if (qty != 0){
            layoutDesc.setVisibility(View.VISIBLE);
            addOrderDesc.setText("Cancel");
            orderDesc.setText(orderMenu.getDescription());
        }

        addOrderDesc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (layoutDesc.getVisibility() == View.VISIBLE) {
                    layoutDesc.setVisibility(View.GONE);
                    addOrderDesc.setText("Add description");
                    orderDesc.setText("");
                } else if (layoutDesc.getVisibility() == View.GONE) {
                    layoutDesc.setVisibility(View.VISIBLE);
                    addOrderDesc.setText("Cancel");
                }
            }
        });


        alert.setView(view);
        alert.setTitle("Order " + product.getName());
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String orderId = orderDatabaseAdapter.getOrderId();
                int q = Integer.parseInt(orderCount.getText().toString());

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
