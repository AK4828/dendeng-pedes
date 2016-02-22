package com.hoqii.fxpc.sales.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.hoqii.fxpc.sales.fragment.CategoryFragment;
import com.hoqii.fxpc.sales.fragment.ProductFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by miftakhul on 11/20/15.
 */
public class MainFragmentStateAdapterMaterial extends FragmentStatePagerAdapter {
    private List<Fragment> fragmentList = new ArrayList<Fragment>();

    public MainFragmentStateAdapterMaterial(FragmentManager fm) {
        super(fm);
        CategoryFragment categoryFragment = new CategoryFragment();
        ProductFragment productFragment = new ProductFragment();

        fragmentList.add(categoryFragment);
        fragmentList.add(productFragment);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
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
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return "KATEGORI";
        } else if (position == 1) {
            return "PRODUK";
        } else {
            return null;
        }
    }
}
