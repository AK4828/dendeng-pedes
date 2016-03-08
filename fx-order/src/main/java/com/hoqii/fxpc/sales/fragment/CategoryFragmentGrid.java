package com.hoqii.fxpc.sales.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageApplication;
import com.hoqii.fxpc.sales.adapter.CategoryGridAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.CategoryDatabaseAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.ProductDatabaseAdapter;
import com.hoqii.fxpc.sales.entity.Category;
import com.hoqii.fxpc.sales.entity.Stock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by miftakhul on 11/16/15.
 */
public class CategoryFragmentGrid extends Fragment {

    private CategoryDatabaseAdapter categoryDatabaseAdapter;
    private ProductDatabaseAdapter productDatabaseAdapter;
    private CategoryGridAdapter categoryGridAdapter;
    private List<Stock> stocks = new ArrayList<Stock>();
    private String jsonStock = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null){
            jsonStock = getArguments().getString("jsonStock");
            ObjectMapper mapper = SignageApplication.getObjectMapper();
            try {
                stocks = mapper.readValue(jsonStock, new TypeReference<List<Stock>>(){});
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater infater, ViewGroup container, Bundle savedInstanceState) {
        View view = infater.inflate(R.layout.fragment_category_grid, container, false);

        categoryDatabaseAdapter = new CategoryDatabaseAdapter(getActivity());
        productDatabaseAdapter = new ProductDatabaseAdapter(getActivity());

        GridView gridView = (GridView) view.findViewById(R.id.gridCategory);

        categoryGridAdapter = new CategoryGridAdapter(getActivity(), stockCategories());
        gridView.setAdapter(categoryGridAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle bundle = new Bundle();
                bundle.putString("parent_category", stockCategories().get(position).getProduct().getParentCategory().getId());
                bundle.putString("jsonStock", jsonStock);
                ProductFragmentGrid productFragment = new ProductFragmentGrid();
                productFragment.setArguments(bundle);
                getFragmentManager().beginTransaction().replace(R.id.category_frame, productFragment).addToBackStack(null).commit();
            }
        });

        return view;
    }

    private List<Stock>stockCategories(){
        List<Stock> cies = new ArrayList<Stock>();
        List<Category> tempCies = new ArrayList<Category>();
        for (Stock s : stocks){
            if (!tempCies.contains(s.getProduct().getParentCategory())){
                cies.add(s);
                tempCies.add(s.getProduct().getParentCategory());
            }
        }
        return cies;
    }

}
