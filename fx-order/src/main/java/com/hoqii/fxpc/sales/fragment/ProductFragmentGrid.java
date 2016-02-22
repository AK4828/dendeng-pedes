package com.hoqii.fxpc.sales.fragment;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.adapter.ProductAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.OrderDatabaseAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.OrderMenuDatabaseAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.ProductDatabaseAdapter;
import com.hoqii.fxpc.sales.entity.OrderMenu;
import com.hoqii.fxpc.sales.entity.Product;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            isMinLoli = true;
        } else {
            isMinLoli = false;
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

        productAdapter = new ProductAdapter(getActivity(), dataProduct());
//        productAdapter = new ProductAdapter(getActivity(), dataProduct(), isMainActivfity);

        gridView = (GridView) view.findViewById(R.id.grid_image);
        gridView.setAdapter(productAdapter);
//        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                if (tx == 0) {
//                    Intent intentOrder = new Intent(getActivity(), OrderActivity.class);
//
//                    intentOrder.putExtra("tx", 0);
//                    intentOrder.putExtra("productId", dataProduct().get(position).getId());
//                    View startView = gridView.getChildAt(position).findViewById(R.id.image);
//
//                    ((MainActivity) getActivity()).order(intentOrder, startView);
//
//                } else if (tx == 1) {
//                    Intent intentOrder = new Intent(getActivity(), OrderActivity.class);
//
//                    intentOrder.putExtra("tx", 1);
//                    intentOrder.putExtra("productId", dataProduct().get(position).getId());
//                    View startView = gridView.getChildAt(position).findViewById(R.id.image);
//
//                    ((MainActivity) getActivity()).order(intentOrder, startView);
//
//                } else if (tx == 2) {
//                    Intent intentOrder = new Intent(getActivity(), OrderActivity.class);
//
//                    intentOrder.putExtra("tx", 2);
//                    intentOrder.putExtra("productId", dataProduct().get(position).getId());
////                    View startView = gridView.getChildAt(position).findViewById(R.id.image);
////                    View startView = productAdapter.getView(position, view, parent).findViewById(R.id.image);
//                    View startView = gridView.getAdapter().getView(position, view, parent).findViewById(R.id.image);
//
//                    if (!getArguments().getBoolean("mainActivityMaterial", false)) {
//                        ((MainActivity) getActivity()).order(intentOrder, startView);
////                        ((MainActivity) getActivity()).order(intentOrder);
//
//                        Log.d("status","MAIN ACTIVITY");
//                    } else {
//                        Log.d("status","MAIN ACTIVITY material");
//
//                        ((MainActivityMaterial) getActivity()).order(intentOrder, startView);
////                        ((MainActivityMaterial) getActivity()).order(intentOrder);
//                    }
//                }
//            }
//        });


        return view;
    }


    private List<Product> dataProduct() {
        List<Product> products = new ArrayList<Product>();

        String parentCategory = null;
        String category = null;
        String name = null;

        if (getArguments() != null) {
            parentCategory = getArguments().getString("parent_category", null);
            category = getArguments().getString("category", null);
            name = getArguments().getString("name", null);
        }


        if (parentCategory != null) {
            products = productDatabaseAdapter.getMenuByParentCategory(parentCategory);
        } else if (category != null) {
            products = productDatabaseAdapter.getMenuByCategory(category);
        } else if (name != null) {
            products = productDatabaseAdapter.getMenuByName(name);
        } else {
            products = productDatabaseAdapter.getMenu();
        }

        Log.d("jumlah total", Integer.toString(products.size()));
        return products;
    }


}
