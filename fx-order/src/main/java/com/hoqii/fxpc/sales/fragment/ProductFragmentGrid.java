package com.hoqii.fxpc.sales.fragment;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageApplication;
import com.hoqii.fxpc.sales.adapter.ProductAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.OrderDatabaseAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.OrderMenuDatabaseAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.ProductDatabaseAdapter;
import com.hoqii.fxpc.sales.entity.OrderMenu;
import com.hoqii.fxpc.sales.entity.Stock;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by miftakhul on 11/16/15.
 */
public class ProductFragmentGrid extends Fragment {

    private ImageLoader imageLoader = ImageLoader.getInstance();
    private GridView gridView;
    private ProductAdapter productAdapter;
    private DecimalFormat decimalFormat = new DecimalFormat("#,###");
    private ProductDatabaseAdapter productDatabaseAdapter;
    private OrderDatabaseAdapter orderDbAdapter;
    private OrderMenuDatabaseAdapter orderMenuDbAdapter;
    private List<OrderMenu> orderMenus = new ArrayList<OrderMenu>();
    private boolean isMinLoli = false, isMainActivfity = false;
    private List<Stock> stocks = new ArrayList<Stock>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            isMinLoli = true;
        } else {
            isMinLoli = false;
        }

        if (getArguments().getString("jsonStock") != null){
            String jsonStock = getArguments().getString("jsonStock");
            ObjectMapper mapper = SignageApplication.getObjectMapper();
            try {
                stocks = mapper.readValue(jsonStock, new TypeReference<List<Stock>>(){});
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public View onCreateView(final LayoutInflater infater, final ViewGroup container, Bundle savedInstanceState) {
        View view = infater.inflate(R.layout.fragment_product_grid, container, false);

        if (!imageLoader.isInited()) {
            imageLoader.init(ImageLoaderConfiguration.createDefault(getActivity()));
        }

        productDatabaseAdapter = new ProductDatabaseAdapter(getActivity());
        orderDbAdapter = new OrderDatabaseAdapter(getActivity());
        orderMenuDbAdapter = new OrderMenuDatabaseAdapter(getActivity());
        productAdapter = new ProductAdapter(getActivity(), dataStock());

        gridView = (GridView) view.findViewById(R.id.grid_image);
        gridView.setAdapter(productAdapter);

        return view;
    }

//    private List<Product> dataProduct() {
//        List<Product> products = new ArrayList<Product>();
//
//        String parentCategory = null;
//        String category = null;
//        String name = null;
//
//        if (getArguments() != null) {
//            parentCategory = getArguments().getString("parent_category", null);
//            category = getArguments().getString("category", null);
//            name = getArguments().getString("name", null);
//        }
//
//        if (parentCategory != null) {
//            products = productDatabaseAdapter.getMenuByParentCategory(parentCategory);
//        } else if (category != null) {
//            products = productDatabaseAdapter.getMenuByCategory(category);
//        } else if (name != null) {
//            products = productDatabaseAdapter.getMenuByName(name);
//        } else {
//            products = productDatabaseAdapter.getMenu();
//        }
//
//        Log.d("jumlah total", Integer.toString(products.size()));
//        return products;
//    }

    private List<Stock> dataStock(){
        List<Stock> data = new ArrayList<Stock>();
        String parentCID = getArguments().getString("parent_category");
        Log.d(getClass().getSimpleName(), "parent category id : "+parentCID);
        if (parentCID != null){
            if (parentCID.equalsIgnoreCase("uncategorized")){
                Log.d(getClass().getSimpleName(), "uncategorized");
                for (Stock s : stocks){
                    if (s.getProduct().getParentCategory().getId() == null){
                        data.add(s);
                        Log.d(getClass().getSimpleName(), "uncategorized added");
                    }
                }
            }else {
                Log.d(getClass().getSimpleName(), "not uncategorized");
                for (Stock s : stocks) {
                    Log.d(getClass().getSimpleName(), "id cat : "+s.getProduct().getParentCategory().getId());
                    if (s.getProduct().getParentCategory().getId() != null && s.getProduct().getParentCategory().getId().equals(parentCID)) {
                        data.add(s);
                    }
                }
            }
            return data;
        }else {
            return stocks;
        }
    }

}
