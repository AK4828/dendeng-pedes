package com.hoqii.fxpc.sales.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.adapter.MainFragmentStateAdapter;

/**
 * Created by miftakhul on 11/20/15.
 */
public class MainMaterialFragment extends Fragment {

    private ViewPager viewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_material, container, false);

        viewPager = (ViewPager) view.findViewById(R.id.main_viewPager);

        MainFragmentStateAdapter viewPagerAdapter = new MainFragmentStateAdapter(getFragmentManager());
        viewPager.setAdapter(viewPagerAdapter);

//        ((MainActivityMaterial) getActivity()).tabLayout.setupWithViewPager(viewPager);


        return view;
    }


}
