package com.hoqii.fxpc.sales.holder;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hoqii.fxpc.sales.R;

import org.meruvian.midas.core.defaults.DefaultHolder;

import butterknife.InjectView;

/**
 * Created by meruvian on 14/07/15.
 */
public class OrderHolder extends DefaultHolder {
    @InjectView(R.id.text_menu_name)
    public TextView menuName;
    @InjectView(R.id.text_menu_quantity)
    public TextView menuQuantity;
    @InjectView(R.id.text_total_price)
    public TextView totalPrice;
//    @InjectView(R.id.image_button_delete)
//    public ImageButton buttonDelete;
//    @InjectView(R.id.image_button_edit)
//    public ImageButton buttonEdit;
    @InjectView(R.id.img_preview)
    public ImageView preview;
    @InjectView(R.id.layout)
    public RelativeLayout layout;

    public OrderHolder(View view) {
        super(view);
    }
}
