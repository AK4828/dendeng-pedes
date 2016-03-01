package com.hoqii.fxpc.sales.adapter;

import android.content.Context;
import android.content.Intent;
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
import com.hoqii.fxpc.sales.activity.MainActivityMaterial;
import com.hoqii.fxpc.sales.activity.OrderActivity;
import com.hoqii.fxpc.sales.entity.Product;
import com.hoqii.fxpc.sales.util.ImageUtil;
import com.joanzapata.iconify.widget.IconTextView;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by miftakhul on 8/19/15.
 */
public class ProductAdapter extends BaseAdapter {
    private Context mcontext;
    private List<Product> products;
    private DecimalFormat decimalFormat = new DecimalFormat("#,###");
    private int mutedColor;
    private boolean isMainActivity = false;


    private static LayoutInflater infalter = null;

    public ProductAdapter(Context c, List<Product> products) {
        mcontext = c;
        infalter = (LayoutInflater) mcontext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        this.products = products;

    }

    public ProductAdapter(Context c, List<Product> products, boolean isMainActivity) {
        mcontext = c;
        infalter = (LayoutInflater) mcontext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        this.products = products;
        this.isMainActivity = isMainActivity;

    }

    @Override
    public int getCount() {
        return products.size();
    }

    @Override
    public Object getItem(int position) {
        return products.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final Holder holder = new Holder();
        View itemView = infalter.inflate(R.layout.adapter_product_grid, null);


        holder.title = (TextView) itemView.findViewById(R.id.text_name);
        holder.price = (TextView) itemView.findViewById(R.id.text_price);
        holder.imageView = (ImageView) itemView.findViewById(R.id.image);
        holder.detailLayout = (RelativeLayout) itemView.findViewById(R.id.detail_layout);
        holder.point = (IconTextView) itemView.findViewById(R.id.text_point);

        holder.title.setText(products.get(position).getName());

        holder.price.setText("Rp. " + decimalFormat.format(products.get(position).getSellPrice()));
        holder.point.setText(products.get(position).getReward()+" point {typcn-star-outline}");

        Glide.with(mcontext).load("file://" + ImageUtil.getImagePath(mcontext, products.get(position).getId())).into(holder.imageView);


        Log.d("path image", ImageUtil.getImagePath(mcontext, products.get(position).getId()));
        Log.d("image file", String.valueOf(ImageUtil.getImage(mcontext, products.get(position).getId())));
//        if (ImageUtil.getImage(mcontext, products.get(position).getId()) != null) {
////            Bitmap bitmap = BitmapFactory.decodeFile(ImageUtil.getImagePath(mcontext, products.get(position).getId()));
//
//            BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inSampleSize = 8;
//            Bitmap bitmap = BitmapFactory.decodeFile(ImageUtil.getImagePath(mcontext, products.get(position).getId()), options);
//
//            try {
//                Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
//                    @Override
//                    public void onGenerated(Palette palette) {
//                        mutedColor = palette.getMutedColor(R.attr.colorPrimary);
//                        holder.detailLayout.setBackgroundColor(mutedColor);
//                    }
//                });
//            } catch (IllegalArgumentException e) {
//                Log.e("Bitmat status", e.getMessage());
//            }
//
//
//        } else {
//            holder.detailLayout.setBackgroundColor(mcontext.getResources().getColor(R.color.grey));
//        }

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intentOrder = new Intent(mcontext, OrderActivity.class);

                intentOrder.putExtra("productId", products.get(position).getId());

                View image = v.findViewById(R.id.image);
                View title = v.findViewById(R.id.text_name);
                View price = v.findViewById(R.id.text_price);

                ((MainActivityMaterial) mcontext).order(intentOrder, image, title, price);

            }
        });

        return itemView;
    }

    public class Holder {

        TextView title, price;
        ImageView imageView;
        RelativeLayout detailLayout;
        IconTextView point;
    }


}
