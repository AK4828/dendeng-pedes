package com.hoqii.fxpc.sales.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.hoqii.fxpc.sales.fragment.ShipmentDetailFragment;
import com.hoqii.fxpc.sales.fragment.ShipmentMenuListFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by miftakhul on 2/10/16.
 */
public class ShipmentDetailPagerAdapter extends FragmentPagerAdapter {
    private List<Fragment> fragments = new ArrayList<Fragment>();

    public ShipmentDetailPagerAdapter(FragmentManager fm, String shipmentJson) {
        super(fm);

        ShipmentMenuListFragment shipmentMenuList = new ShipmentMenuListFragment();
        Bundle b = new Bundle();
        b.putString("shipmentJson", shipmentJson);
        shipmentMenuList.setArguments(b);

        ShipmentDetailFragment shipmentDetailFragment = new ShipmentDetailFragment();
        Bundle s = new Bundle();
        s.putString("shipmentJson", shipmentJson);
        shipmentDetailFragment.setArguments(s);

        fragments.add(shipmentMenuList);
        fragments.add(shipmentDetailFragment);
    }


    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return fragments.get(0);
            case 1:
                return fragments.get(1);
        }

        return null;
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return "Item";
        } else if (position == 1) {
            return "Detail";
        } else {
            return null;
        }
    }
}
