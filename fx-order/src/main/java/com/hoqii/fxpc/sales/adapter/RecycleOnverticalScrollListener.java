package com.hoqii.fxpc.sales.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by miftakhul on 1/27/16.
 */
public abstract class RecycleOnverticalScrollListener extends RecyclerView.OnScrollListener{
    private RecyclerView recyclerView;

    public RecycleOnverticalScrollListener(RecyclerView recyclerView){
        this.recyclerView = recyclerView;
    }


    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//        super.onScrolled(recyclerView, dx, dy);
        if (!recyclerView.canScrollVertically(-1)){
            onScrolledToTop();
        }else if (!recyclerView.canScrollVertically(1)){
            onScrolledToBottom();
        }else if (dy < 0){
            onScrolledUp();
        }else if (dy > 0){
            onScrolledDown();
        }
        Log.d(getClass().getSimpleName(), "recycle scrolling");
    }

    public void onScrolledUp(){
        Log.d(getClass().getSimpleName(), "recycle scrolledUp");
    }

    public void onScrolledDown(){
        Log.d(getClass().getSimpleName(), "recycle scrolledDown");
    }

    public void onScrolledToTop(){
        Log.d(getClass().getSimpleName(), "recycle scrolled to top");
    }

    public void onScrolledToBottom(){
        Log.d(getClass().getSimpleName(), "recycle scrolled to bottom");
    }

}