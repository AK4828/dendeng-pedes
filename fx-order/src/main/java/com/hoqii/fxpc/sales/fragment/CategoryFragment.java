package com.hoqii.fxpc.sales.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageApplication;
import com.hoqii.fxpc.sales.entity.Stock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by miftakhul on 11/16/15.
 */
public class CategoryFragment extends Fragment {
    private List<Stock> stocks = new ArrayList<Stock>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null){
            String jsonStock = getArguments().getString("jsonStock");
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
        View view = infater.inflate(R.layout.fragment_category, container, false);
        CategoryFragmentGrid c = new CategoryFragmentGrid();
        String jsonStock = null;
        ObjectMapper mapper = SignageApplication.getObjectMapper();
        try {
            jsonStock = mapper.writeValueAsString(stocks);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        Bundle bundle = new Bundle();
        bundle.putString("jsonStock", jsonStock);
        c.setArguments(bundle);

        getFragmentManager().beginTransaction().replace(R.id.category_frame, c).commit();
        return view;
    }

}
