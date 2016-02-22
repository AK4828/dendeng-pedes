package com.hoqii.fxpc.sales.fragment;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.adapter.ProductAdapter;
import com.hoqii.fxpc.sales.adapter.ProductRecycleAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.CategoryDatabaseAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.OrderDatabaseAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.OrderMenuDatabaseAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.ProductDatabaseAdapter;
import com.hoqii.fxpc.sales.entity.Category;
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
public class ProductFragment extends Fragment {

    private RecyclerView recyclerView;
    private CategoryDatabaseAdapter categoryDatabaseAdapter;
    private ProductDatabaseAdapter productDatabaseAdapter;
    private List<Category> categories = new ArrayList<Category>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        categoryDatabaseAdapter = new CategoryDatabaseAdapter(getActivity());
        productDatabaseAdapter = new ProductDatabaseAdapter(getActivity());

        if (getArguments() == null) {
            List<Category> tempCategorys = new ArrayList<Category>();
            tempCategorys = categoryDatabaseAdapter.getParentCategoryMenu();

            for (Category category : tempCategorys) {
                if (productDatabaseAdapter.getMenuByParentCategory(category.getId()).size() > 0) {
                    categories.add(category);
                }
            }
        }else {
            String categoryId = getArguments().getString("parent_categoryId");
            Category category = categoryDatabaseAdapter.findCategoryById(categoryId);
            categories.add(category);
        }

    }


    @Override
    public View onCreateView(final LayoutInflater infater, final ViewGroup container, Bundle savedInstanceState) {
        View view = infater.inflate(R.layout.fragment_product, container, false);

        recyclerView = (RecyclerView)view.findViewById(R.id.recycle_view);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(new ProductRecycleAdapter(getActivity(), categories));

        return view;
    }



}
