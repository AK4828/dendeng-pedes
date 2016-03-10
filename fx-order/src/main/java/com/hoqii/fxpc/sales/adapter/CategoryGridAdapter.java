package com.hoqii.fxpc.sales.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageApplication;
import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.content.database.adapter.ProductDatabaseAdapter;
import com.hoqii.fxpc.sales.entity.Category;
import com.hoqii.fxpc.sales.entity.Product;
import com.hoqii.fxpc.sales.entity.Stock;
import com.hoqii.fxpc.sales.fragment.ProductFragmentGrid;
import com.hoqii.fxpc.sales.util.AuthenticationUtils;
import com.hoqii.fxpc.sales.util.ImageUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by miftakhul on 8/19/15.
 */
public class CategoryGridAdapter extends BaseAdapter {
    private Context mcontext;
    private List<Stock> stocks = new ArrayList<Stock>();
    private List<String> color = new ArrayList<String>();
    private SharedPreferences preferences;

    String[] baseColor = {"#DD7627","#F59D27","#2C8EC9","#6CAA44","#6A51A1","#93287D"};


    private static LayoutInflater infalter = null;

    public CategoryGridAdapter(Context c, List<Stock> stocksList) {
        mcontext = c;
        infalter = (LayoutInflater) mcontext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        preferences = c.getSharedPreferences(SignageVariables.PREFS_SERVER, 0);
        this.stocks = stocksList;

//        List<String> categoriesId = new ArrayList<String>();
//        for (Stock s : stocksList){
//            if (s.getProduct().getParentCategory().getId() != null) {
//                if (!categoriesId.contains(s.getProduct().getParentCategory().getId())) {
//                    categoriesId.add(s.getProduct().getParentCategory().getId());
//                    stocks.add(s);
//                    Log.d(getClass().getSimpleName(), "category id " + s.getProduct().getParentCategory().getId());
//                    Log.d(getClass().getSimpleName(), "category name "+ s.getProduct().getParentCategory().getName());
//                    Log.d(getClass().getSimpleName(), "==============================");
//
//                }
//            }else {
//                Category cUncateg = new Category();
//                cUncateg.setId("uncategorized");
//                cUncateg.setName("Uncategorized");
//                if (!categoriesId.contains(cUncateg.getId())) {
//                    categoriesId.add(cUncateg.getId());
//                    s.getProduct().setParentCategory(cUncateg);
//                    stocks.add(s);
//                }
//            }
//        }

        if (stocks.size() > 0){
            for (int x = 0; x < stocks.size(); x++){
                if (color.size() < stocks.size()){
                    for (String a : baseColor){
                        color.add(a);
                    }
                }
            }
        }
        Log.d(getClass().getSimpleName(), "stock size"+stocks.size());
    }

    @Override
    public int getCount() {
        return stocks.size();
    }

    @Override
    public Object getItem(int position) {
        return stocks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final Holder holder = new Holder();
        View itemView = infalter.inflate(R.layout.adapter_category_grid, null);

        holder.imageView = (ImageView) itemView.findViewById(R.id.category_image);
        holder.title = (TextView) itemView.findViewById(R.id.category_title);
        holder.layout = (RelativeLayout) itemView.findViewById(R.id.detail_layout);

//        List<Product> products = new ArrayList<Product>();
//
//        products = new ProductDatabaseAdapter(mcontext).getMenuByParentCategory(categories.get(position).getId());
//        Log.d("prduk size", Integer.toString(products.size()));

//        if (products.size() == 0) {
//            holder.imageView.setImageResource(R.drawable.no_image);
//        } else {
//            Glide.with(mcontext).load("file://" + ImageUtil.getImagePath(mcontext, products.get(products.size() - 1).getId())).into(holder.imageView);
//        }

        String imageUrl = preferences.getString("server_url", "")+"/api/products/"+stocks.get(position).getProduct().getId() + "/image?access_token="+ AuthenticationUtils.getCurrentAuthentication().getAccessToken();
        Glide.with(mcontext).load(imageUrl).error(R.drawable.no_image).into(holder.imageView);
        holder.title.setText(stocks.get(position).getProduct().getParentCategory().getName());
        holder.layout.setBackgroundColor(Color.parseColor(color.get(position)));
//
//        itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                Bundle bundle = new Bundle();
//                bundle.putString("parent_category", stocks.get(position).getProduct().getParentCategory().getId());
//                bundle.putString("jsonStock", jsonStock);
//                ProductFragmentGrid productFragment = new ProductFragmentGrid();
//                productFragment.setArguments(bundle);
//                getFragmentManager().beginTransaction().replace(R.id.category_frame, productFragment).addToBackStack(null).commit();
//            }
//        });

        return itemView;

    }

    public class Holder {
        public ImageView imageView;
        public TextView title;
        public RelativeLayout layout;
    }
}
