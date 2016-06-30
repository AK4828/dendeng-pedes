package com.hoqii.fxpc.sales.activity;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageApplication;
import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.content.database.adapter.OrderDatabaseAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.OrderMenuDatabaseAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.ProductDatabaseAdapter;
import com.hoqii.fxpc.sales.entity.Order;
import com.hoqii.fxpc.sales.entity.OrderMenu;
import com.hoqii.fxpc.sales.entity.Product;
import com.hoqii.fxpc.sales.entity.Stock;
import com.hoqii.fxpc.sales.task.StockSync;
import com.hoqii.fxpc.sales.util.AuthenticationUtils;
import com.hoqii.fxpc.sales.util.ImageUtil;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.TypiconsIcons;
import com.joanzapata.iconify.widget.IconTextView;

import org.meruvian.midas.core.service.TaskService;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by miftakhul on 12/2/15.
 */
public class OrderActivity extends AppCompatActivity implements TaskService {

    private Toolbar toolbar;
    private ImageView prodcutThumb;
    private TextView productName, productPrice, productDesc, productStock, orderDesc, orderCount;
    private IconTextView reward;
    private Product product;
    private OrderMenu orderMenu;
    private Button orderButton;
    private ImageButton btnMin, btnPlus;

    private ProductDatabaseAdapter productDatabaseAdapter;
    private OrderDatabaseAdapter orderDatabaseAdapter;
    private OrderMenuDatabaseAdapter orderMenuDatabaseAdapter;

    private boolean isMinLoli = false;
    private long orderMenuPrice;
    private List<Integer> orderCountList = new ArrayList<Integer>();
    private DecimalFormat decimalFormat = new DecimalFormat("#,###");
    private OrderMenu.OrderType orderMenuType;
    private int qty = 0, stockProduct = 0;
    private SharedPreferences preferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        preferences = getSharedPreferences(SignageVariables.PREFS_SERVER, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            isMinLoli = true;
        } else {
            isMinLoli = false;
        }

        init();
        if (getIntent().getExtras() != null) {
            qty = getIntent().getIntExtra("qtyUpdate", 0);
            stockProduct = getIntent().getIntExtra("stockProduct", 0);

            String jsonProduct = getIntent().getStringExtra("jsonProduct");
            ObjectMapper mapper = SignageApplication.getObjectMapper();

            try {
                product = mapper.readValue(jsonProduct, Product.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
            orderMenu = orderMenuDatabaseAdapter.findOrderMenuByProductId(product.getId());
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
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                break;
            case R.id.menu_cart:
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
        productStock = (TextView) findViewById(R.id.order_product_stock);
        orderButton = (Button) findViewById(R.id.addCart);
        orderCount = (TextView) findViewById(R.id.order_count);
        btnMin = (ImageButton) findViewById(R.id.btn_min);
        btnPlus = (ImageButton) findViewById(R.id.btn_plus);

        productDatabaseAdapter = new ProductDatabaseAdapter(this);
        orderDatabaseAdapter = new OrderDatabaseAdapter(this);
        orderMenuDatabaseAdapter = new OrderMenuDatabaseAdapter(this);
    }

    private void initSet() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(new IconDrawable(this, TypiconsIcons.typcn_chevron_left).colorRes(R.color.white).actionBarSize());

//        Glide.with(this).load(preview).error(R.drawable.no_image).into(prodcutThumb);
        String imageUrl = preferences.getString("server_url", "") + "/api/products/" + product.getId() + "/image?access_token=" + AuthenticationUtils.getCurrentAuthentication().getAccessToken();
        Glide.with(this).load(imageUrl).error(R.drawable.ic_description_24dp).into(prodcutThumb);
        productName.setText(product.getName());

        productPrice.setText(getString(R.string.text_currency) + decimalFormat.format(product.getSellPrice()));
        reward.setText(getString(R.string.text_reward) + Double.toString(product.getReward()) + getString(R.string.text_point_end));
        productStock.setText(Integer.toString(stockProduct) + getString(R.string.text_item_end));
        orderMenuType = OrderMenu.OrderType.PURCHASE_ORDER;

        if (product.getDescription().toString().equalsIgnoreCase("null")) {
            Log.d(getClass().getSimpleName(), "desc null");
            productDesc.setText(R.string.text_no_description);
        } else {
            Log.d(getClass().getSimpleName(), "desc not null");
            String temp = product.getDescription();
            final String desc = temp.replace("\n", " ");

            int length = desc.length();
            if (length > 100) {
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
                        } else {
                            clicked[0] = false;
                            productDesc.setText(newDesc + "\n\nooo");
                        }
                    }
                });
            } else {
                productDesc.setText(desc);
            }
        }

        for (int x = 1; x <= 100; x++) {
            orderCountList.add(x);
        }

        // for update order
        if (qty != 0) {
            orderCount.setText(Integer.toString(qty));
            orderButton.setText(R.string.text_button_update_order);
            String orderId = orderDatabaseAdapter.getOrderId();
            String siteId = orderDatabaseAdapter.findOrderById(orderId).getSite().getId();
            StockSync stockSync = new StockSync(this, this, StockSync.StockUri.byProductIdUri.name());
            stockSync.execute(product.getId(), siteId);
        } else {
            orderCount.setText(Integer.toString(1));
        }
        if (stockProduct == 0) {
            orderButton.setEnabled(false);
        }
        if (product.getSellPrice() == 0) {
            orderButton.setEnabled(false);
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

    private void addOrderCount(int count) {
        int c = Integer.parseInt(orderCount.getText().toString());
        int totalCount = c + count;
        if (totalCount > stockProduct) {

        } else {
            if (count > 0) {
                c += count;
                orderCount.setText(Integer.toString(c));
            }
            if (count < 0) {
                if (c > 1) {
                    Log.d(getClass().getSimpleName(), "min data " + Integer.toString(c));
                    c += count;
                    orderCount.setText(Integer.toString(c));
                }
            }
            orderMenuPrice = product.getSellPrice() * Integer.parseInt(orderCount.getText().toString());
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

    private void dialogOrder() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        View view = getLayoutInflater().from(OrderActivity.this).inflate(R.layout.view_order_desc, null);
        orderDesc = (TextView) view.findViewById(R.id.order_desc);
        final LinearLayout layoutDesc = (LinearLayout) view.findViewById(R.id.layout_desc);
        final Button addOrderDesc = (Button) view.findViewById(R.id.btn_addOrderDesc);

        if (!isMinLoli) {
            addOrderDesc.setTextColor(getResources().getColor(R.color.colorAccent));
        }
        if (qty != 0) {
            layoutDesc.setVisibility(View.VISIBLE);
            addOrderDesc.setText(getResources().getString(R.string.cancel));
            orderDesc.setText(orderMenu.getDescription());
        }

        addOrderDesc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (layoutDesc.getVisibility() == View.VISIBLE) {
                    layoutDesc.setVisibility(View.GONE);
                    addOrderDesc.setText(R.string.button_add_description);
                    orderDesc.setText("");
                } else if (layoutDesc.getVisibility() == View.GONE) {
                    layoutDesc.setVisibility(View.VISIBLE);
                    addOrderDesc.setText(getResources().getString(R.string.cancel));
                }
            }
        });

        alert.setView(view);
        alert.setTitle(getString(R.string.message_order) + product.getName());
        alert.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                productDatabaseAdapter.saveProduct(product);
                String orderId = orderDatabaseAdapter.getOrderId();
                int q = Integer.parseInt(orderCount.getText().toString());

                if (orderId == null) {
                    Log.d("1", "Pos1");
                    orderId = saveOrder();

                    if (orderMenuDatabaseAdapter.findOrderMenuByProductId(product.getId()) == null) {
                        Log.d("2", "Pos2");
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
                        Log.d("3", "Pos3");
                        OrderMenu tempOrdermenu = orderMenuDatabaseAdapter.findOrderMenuByProductId(product.getId());

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
                    Log.d("4", "Pos4");
                    if (orderMenuDatabaseAdapter.findOrderMenuByProductId(product.getId()) == null) {
                        OrderMenu orderMenu = new OrderMenu();

                        orderMenu.getLogInformation().setCreateBy(AuthenticationUtils.getCurrentAuthentication().getUser().getId());
                        orderMenu.getLogInformation().setLastUpdateBy(AuthenticationUtils.getCurrentAuthentication().getUser().getId());
                        orderMenu.getLogInformation().setSite(AuthenticationUtils.getCurrentAuthentication().getSite().getId());

                        orderMenu.getOrder().setId(orderId);
                        orderMenu.setQty(q);
                        orderMenu.setQtyOrder(q);
                        orderMenu.setProduct(product);
                        Log.d("PRICe", String.valueOf(orderMenuPrice));
                        orderMenu.setSellPrice(orderMenuPrice);
                        orderMenu.setDescription(orderDesc.getText().toString());
                        orderMenu.setType(orderMenuType.name());

                        orderMenuDatabaseAdapter.saveOrderMenu(orderMenu);
                    } else {
                        Log.d("5", "Pos5");

                        OrderMenu tempOrdermenu = orderMenuDatabaseAdapter.findOrderMenuByProductId(product.getId());

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
                    Log.d("6", "Pos6");
                    OrderMenu tempOrdermenu = orderMenuDatabaseAdapter.findOrderMenuByProductId(product.getId());

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
                dialog.dismiss();
            }
        });
        alert.show();
    }


    @Override
    public void onExecute(int code) {

    }

    @Override
    public void onSuccess(int code, Object result) {
        Stock stock = (Stock) result;
        productStock.setText(Integer.toString(stock.getQty()) + getResources().getString(R.string.text_item_end));
        stockProduct = stock.getQty();
        orderButton.setEnabled(true);
    }

    @Override
    public void onCancel(int code, String message) {

    }

    @Override
    public void onError(int code, String message) {

    }
}
