package com.hoqii.fxpc.sales.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.activity.MainActivityMaterialNew;
import com.hoqii.fxpc.sales.activity.OrderActivity;
import com.hoqii.fxpc.sales.content.database.adapter.CategoryDatabaseAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.ProductDatabaseAdapter;
import com.hoqii.fxpc.sales.entity.Category;
import com.hoqii.fxpc.sales.entity.Product;
import com.hoqii.fxpc.sales.util.ImageUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by miftakhul on 12/6/15.
 */
public class ProductRecycleAdapter extends RecyclerView.Adapter<ProductRecycleAdapter.ViewHolder> {

    private Context context;
    private ProductDatabaseAdapter productDatabaseAdapter;
    private CategoryDatabaseAdapter categoryDatabaseAdapter;
    private List<Category> categories = new ArrayList<Category>();


//    public ProductRecycleAdapter(Context context) {
//        this.context = context;
//        productDatabaseAdapter = new ProductDatabaseAdapter(context);
//        categoryDatabaseAdapter = new CategoryDatabaseAdapter(context);
//
//        List<Category> tempCategorys = new ArrayList<Category>();
//        tempCategorys = categoryDatabaseAdapter.getParentCategoryMenu();
//
//        for (Category category : tempCategorys) {
//            if (productDatabaseAdapter.getMenuByParentCategory(category.getId()).size() > 0) {
//                categories.add(category);
//            }
//        }
//
//    }

    public ProductRecycleAdapter(Context context, List<Category> categories){
        this.context = context;
        this.categories = categories;
        productDatabaseAdapter = new ProductDatabaseAdapter(context);
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_product, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        List<Product> products = new ArrayList<Product>();
        products = productDatabaseAdapter.getMenuByParentCategory(categories.get(position).getId());
        ProductAdapterNew productAdapter = new ProductAdapterNew(context, products);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(context, 2);
        holder.recyclerView.setLayoutManager(layoutManager);

        ViewGroup.LayoutParams layoutParams = holder.recyclerView.getLayoutParams();

        int height;
        int displayDensisty = (int) context.getResources().getDisplayMetrics().density;
        Log.d(getClass().getSimpleName(), "android density : " + displayDensisty);
        int defaultHeight = 228 * displayDensisty ;//layout height

        if (products.size() * 2 == 0) {
            Log.d(getClass().getSimpleName(), "pas");
            height = (products.size() * defaultHeight) / 2;
        } else {
            Log.d(getClass().getSimpleName(), "lebih satu");

            height = ((products.size() * defaultHeight) / 2) + defaultHeight;
        }


        layoutParams.height = height;
        holder.recyclerView.setLayoutParams(layoutParams);

        holder.productCategory.setText(categories.get(position).getName());
        holder.recyclerView.setAdapter(productAdapter);


    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView productCategory;
        private RecyclerView recyclerView;

        public ViewHolder(View itemView) {
            super(itemView);

            productCategory = (TextView) itemView.findViewById(R.id.product_category);
            recyclerView = (RecyclerView) itemView.findViewById(R.id.recycle_view_product);

        }
    }


    class ProductAdapterNew extends RecyclerView.Adapter<ProductAdapterNew.ViewHolder> {
        private Context mcontext;
        private ImageLoader imageLoader = ImageLoader.getInstance();
        private List<Product> products;
        private DecimalFormat decimalFormat = new DecimalFormat("#,###");
        private int mutedColor;

        public ProductAdapterNew(Context c, List<Product> products) {
            mcontext = c;
            this.products = products;

            if (!imageLoader.isInited()) {
                imageLoader.init(ImageLoaderConfiguration.createDefault(mcontext));
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_product_grid_new, parent, false);
            ViewHolder viewHolder = new ViewHolder(view);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            holder.title.setText(products.get(position).getName());
            holder.price.setText("Rp. " + decimalFormat.format(products.get(position).getSellPrice()));

            imageLoader.displayImage("file://" + ImageUtil.getImagePath(mcontext, products.get(position).getId()), holder.imageView, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    holder.progressImage.setVisibility(View.VISIBLE);
                    holder.imageView.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    holder.progressImage.setVisibility(View.GONE);
                    holder.imageView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    holder.progressImage.setVisibility(View.GONE);
                    holder.imageView.setImageResource(R.drawable.no_image);
                }
            });

            if (ImageUtil.getImage(mcontext, products.get(position).getId()) != null) {
//            Bitmap bitmap = BitmapFactory.decodeFile(ImageUtil.getImagePath(mcontext, products.get(position).getId()));

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 8;
                Bitmap bitmap = BitmapFactory.decodeFile(ImageUtil.getImagePath(mcontext, products.get(position).getId()), options);

                try {
                    Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                        @Override
                        public void onGenerated(Palette palette) {
                            mutedColor = palette.getMutedColor(R.attr.colorPrimary);
                            holder.detailLayout.setBackgroundColor(mutedColor);
                        }
                    });
                } catch (IllegalArgumentException e) {
                    Log.e("Bitmat status", e.getMessage());
                }


            } else {
                holder.detailLayout.setBackgroundColor(mcontext.getResources().getColor(R.color.colorPrimary));
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intentOrder = new Intent(mcontext, OrderActivity.class);

                    intentOrder.putExtra("productId", products.get(position).getId());

                    View image = v.findViewById(R.id.image);
                    View title = v.findViewById(R.id.text_name);
                    View price = v.findViewById(R.id.text_price);
//                    View shadow = v.findViewById(R.id.shadow);

                            ((MainActivityMaterialNew) mcontext).order(intentOrder, image, title, price);
                }
            });
        }

        @Override
        public int getItemCount() {
            return products.size();
        }


        class ViewHolder extends RecyclerView.ViewHolder {

            TextView title, price;
            ProgressBar progressImage;
            ImageView imageView;
            RelativeLayout detailLayout;
            View view;

            public ViewHolder(View itemView) {
                super(itemView);

                view = itemView;

                title = (TextView) itemView.findViewById(R.id.text_name);
                price = (TextView) itemView.findViewById(R.id.text_price);
                progressImage = (ProgressBar) itemView.findViewById(R.id.progressbar);
                imageView = (ImageView) itemView.findViewById(R.id.image);
                detailLayout = (RelativeLayout) itemView.findViewById(R.id.detail_layout);
            }

        }

    }

}
