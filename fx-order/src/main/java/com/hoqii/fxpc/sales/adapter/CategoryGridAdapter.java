package com.hoqii.fxpc.sales.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.content.database.adapter.ProductDatabaseAdapter;
import com.hoqii.fxpc.sales.entity.Category;
import com.hoqii.fxpc.sales.entity.Product;
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
    private List<Category> categories;
    private List<String> color = new ArrayList<String>();

    String[] baseColor = {"#DD7627","#F59D27","#2C8EC9","#6CAA44","#6A51A1","#93287D"};


    private static LayoutInflater infalter = null;

    public CategoryGridAdapter(Context c, List<Category> categories) {
        mcontext = c;
        infalter = (LayoutInflater) mcontext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.categories = categories;

        if (categories.size() > 0){
            for (int x = 0; x < categories.size(); x++){
                if (color.size() < categories.size()){
                    for (String a : baseColor){
                        color.add(a);
                    }
                }
            }
        }
    }

    @Override
    public int getCount() {
        return categories.size();
    }

    @Override
    public Object getItem(int position) {
        return categories.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final Holder holder = new Holder();
        View itemView = infalter.inflate(R.layout.adapter_category_grid, null);

        holder.imageView = (ImageView) itemView.findViewById(R.id.category_image);
        holder.title = (TextView) itemView.findViewById(R.id.category_title);
        holder.layout = (RelativeLayout) itemView.findViewById(R.id.detail_layout);


        List<Product> products = new ArrayList<Product>();

        products = new ProductDatabaseAdapter(mcontext).getMenuByParentCategory(categories.get(position).getId());
        Log.d("prduk size", Integer.toString(products.size()));

        if (products.size() == 0) {
            holder.imageView.setImageResource(R.drawable.no_image);
        } else {
            Glide.with(mcontext).load("file://" + ImageUtil.getImagePath(mcontext, products.get(products.size() - 1).getId())).into(holder.imageView);
        }

        holder.title.setText(categories.get(position).getName());
        holder.layout.setBackgroundColor(Color.parseColor(color.get(position)));
        return itemView;

    }

    public class Holder {
        public ImageView imageView;
        public TextView title;
        public RelativeLayout layout;
    }
}
