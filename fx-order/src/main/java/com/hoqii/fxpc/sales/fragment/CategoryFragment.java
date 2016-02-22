package com.hoqii.fxpc.sales.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hoqii.fxpc.sales.R;

/**
 * Created by miftakhul on 11/16/15.
 */
public class CategoryFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater infater, ViewGroup container, Bundle savedInstanceState) {
        View view = infater.inflate(R.layout.fragment_category, container, false);
        getFragmentManager().beginTransaction().replace(R.id.category_frame, new CategoryFragmentGrid()).commit();
        return view;
    }



}
