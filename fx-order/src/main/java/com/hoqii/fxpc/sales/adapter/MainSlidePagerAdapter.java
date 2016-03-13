package com.hoqii.fxpc.sales.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.fragment.ReceiveListFragment;
import com.hoqii.fxpc.sales.fragment.SellerOrderListFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by miftakhul on 2/25/16.
 */
public class MainSlidePagerAdapter extends FragmentPagerAdapter{
    private List<Fragment> fragmentList = new ArrayList<Fragment>();
    private Context context;

    public MainSlidePagerAdapter(FragmentManager fm, Context context) {
        super(fm);

        this.context = context;
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
                return context.getString(R.string.text_order_list);
            case 1:
                return context.getString(R.string.text_receive_list);
            default:
                return null;
        }
    }
}
