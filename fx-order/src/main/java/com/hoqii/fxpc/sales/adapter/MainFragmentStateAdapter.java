package com.hoqii.fxpc.sales.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageApplication;
import com.hoqii.fxpc.sales.entity.Stock;
import com.hoqii.fxpc.sales.fragment.CategoryFragment;
import com.hoqii.fxpc.sales.fragment.ProductFragmentGrid;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by miftakhul on 11/20/15.
 */
public class MainFragmentStateAdapter extends FragmentStatePagerAdapter{
    private List<Fragment> fragmentList = new ArrayList<Fragment>();
    private Context context;

    public MainFragmentStateAdapter(FragmentManager fm, Context context, List<Stock> stocks) {
        super(fm);
        String jsonStock = null;
        this.context = context;
        ObjectMapper mapper = SignageApplication.getObjectMapper();
        try {
            jsonStock = mapper.writeValueAsString(stocks);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        Bundle bundle = new Bundle();
        bundle.putString("jsonStock", jsonStock);
        CategoryFragment categoryFragment = new CategoryFragment();
        ProductFragmentGrid productFragmentGrid = new ProductFragmentGrid();

        categoryFragment.setArguments(bundle);
        productFragmentGrid.setArguments(bundle);

        fragmentList.add(categoryFragment);
        fragmentList.add(productFragmentGrid);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return fragmentList.get(0);

            case 1:
                return fragmentList.get(1);
        }

        return null;
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }


    @Override
    public CharSequence getPageTitle(int position){
        if (position == 0){
            return context.getString(R.string.text_category);
        }else if (position == 1){
            return context.getString(R.string.text_product);
        }else {
            return null;
        }
    }
}
