package com.hoqii.fxpc.sales.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.hoqii.fxpc.sales.fragment.ReceiveListFragment;
import com.hoqii.fxpc.sales.fragment.SellerOrderListFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by miftakhul on 2/25/16.
 */
public class MainSlidePagerAdapter extends FragmentPagerAdapter{
    private List<Fragment> fragmentList = new ArrayList<Fragment>();

    public MainSlidePagerAdapter(FragmentManager fm) {
        super(fm);

        fragmentList.add(new SellerOrderListFragment());
        fragmentList.add(new ReceiveListFragment());
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
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return "Order List";
            case 1:
                return "Receive List";
            default:
                return null;
        }
    }
}
