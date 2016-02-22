package com.hoqii.fxpc.sales.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.activity.MainActivityMaterial;
import com.hoqii.fxpc.sales.adapter.CategoryGridAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.CategoryDatabaseAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.ProductDatabaseAdapter;
import com.hoqii.fxpc.sales.entity.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by miftakhul on 11/16/15.
 */
public class CategoryFragmentGrid extends Fragment {

    private CategoryDatabaseAdapter categoryDatabaseAdapter;
    private ProductDatabaseAdapter productDatabaseAdapter;
    private CategoryGridAdapter categoryGridAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater infater, ViewGroup container, Bundle savedInstanceState) {
        View view = infater.inflate(R.layout.fragment_category_grid, container, false);

        categoryDatabaseAdapter = new CategoryDatabaseAdapter(getActivity());
        productDatabaseAdapter = new ProductDatabaseAdapter(getActivity());

        GridView gridView = (GridView) view.findViewById(R.id.gridCategory);

        categoryGridAdapter = new CategoryGridAdapter(getActivity(), dataCategory());
        gridView.setAdapter(categoryGridAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle bundle = new Bundle();
                bundle.putString("parent_category", dataCategory().get(position).getId());

                ProductFragmentGrid productFragment = new ProductFragmentGrid();
                productFragment.setArguments(bundle);

                getFragmentManager().beginTransaction().replace(R.id.category_frame, productFragment).addToBackStack(null).commit();

            }
        });

        return view;
    }


    private List<Category> dataCategory() {
        List<Category> categories = new ArrayList<Category>();

        if (getArguments() != null && getArguments().containsKey("parent_category")) {
            categories.addAll(categoryDatabaseAdapter.getCategoryMenuByIdParent(getArguments().getString("parent_category", null)));
        } else {
            categories.addAll(categoryDatabaseAdapter.getParentCategoryMenu());
        }
        Log.d("jumlah total", Integer.toString(categories.size()));
        return categories;
    }

}
