package com.hoqii.fxpc.sales.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.transition.Transition;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.adapter.MainFragmentStateAdapter;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.EntypoIcons;
import com.joanzapata.iconify.fonts.EntypoModule;
import com.joanzapata.iconify.fonts.FontAwesomeModule;
import com.joanzapata.iconify.fonts.IoniconsModule;
import com.joanzapata.iconify.fonts.MaterialCommunityModule;
import com.joanzapata.iconify.fonts.MaterialModule;
import com.joanzapata.iconify.fonts.MeteoconsModule;
import com.joanzapata.iconify.fonts.SimpleLineIconsModule;
import com.joanzapata.iconify.fonts.TypiconsModule;
import com.joanzapata.iconify.fonts.WeathericonsModule;

/**
 * Created by miftakhul on 11/13/15.
 */
public class MainActivityMaterial extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private boolean isMinLoli = false;
    private static final int ORDER_REQUEST = 300;
    private Transition.TransitionListener transitionListener;
    int xCoordinate, yCoordinate;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_material);

        tabLayout = (TabLayout) findViewById(R.id.main_tab);

        if (getIntent() != null){
            xCoordinate = getIntent().getIntExtra("xCoordinate", 0);
            yCoordinate = getIntent().getIntExtra("yCoordinate", 0);
        }

        transitionListener = new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
                enterReveal(xCoordinate, yCoordinate);
            }

            @Override
            public void onTransitionEnd(Transition transition) {

            }

            @Override
            public void onTransitionCancel(Transition transition) {

            }

            @Override
            public void onTransitionPause(Transition transition) {

            }

            @Override
            public void onTransitionResume(Transition transition) {

            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            isMinLoli = true;
            getWindow().getEnterTransition().addListener(transitionListener);
        } else {
            isMinLoli = false;
        }



        Iconify
                .with(new FontAwesomeModule())
                .with(new EntypoModule())
                .with(new TypiconsModule())
                .with(new MaterialModule())
                .with(new MaterialCommunityModule())
                .with(new MeteoconsModule())
                .with(new WeathericonsModule())
                .with(new SimpleLineIconsModule())
                .with(new IoniconsModule());


        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setHomeAsUpIndicator(new IconDrawable(this, EntypoIcons.entypo_chevron_left).colorRes(R.color.white).actionBarSize());

        viewPager = (ViewPager) findViewById(R.id.main_viewPager);

        MainFragmentStateAdapter viewPagerAdapter = new MainFragmentStateAdapter(getSupportFragmentManager());
        viewPager.setAdapter(viewPagerAdapter);

        tabLayout.setupWithViewPager(viewPager);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (isMinLoli) {
            exitReveal(xCoordinate, yCoordinate);
        }
    }

    public void sendResult(Intent intent){
        if (intent != null){
            intent.getExtras().getString("type", null);

            setResult(RESULT_OK, intent);
            finish();
        }
    }

    public void order(Intent intent, View image, View title, View price){
        if (isMinLoli) {
//            String transitionName = getString(R.string.transition_string);
            Pair<View, String> pariImage = Pair.create(image, getString(R.string.transition_image));
            Pair<View, String> pairTitle = Pair.create(title, getString(R.string.transition_title));
            Pair<View, String> pariPrice = Pair.create(price, getString(R.string.transition_price));

            ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(this, pariImage, pairTitle);

            startActivityForResult(intent, ORDER_REQUEST, optionsCompat.toBundle());
            Log.d("order", " loli========================================");
        } else {
            startActivityForResult(intent, ORDER_REQUEST);
            Log.d("order", " not loli========================================");
        }
        Log.d("order", "========================================");
    }

    public void order(Intent intent){
        startActivityForResult(intent, ORDER_REQUEST);
        Log.d("order", "========================================");
    }



//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_main_activity_material, menu);
//
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home :
                super.onBackPressed();
                if (isMinLoli) {
                    exitReveal(xCoordinate, yCoordinate);
                }
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("result", "on=========================");
        if (requestCode == ORDER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Intent intent = new Intent();
                intent.putExtra("type", "orderList");

                setResult(RESULT_OK, intent);
                finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void enterReveal(int x, int y) {
        final View view = findViewById(R.id.coordianotorLayout);
        int finalRadius = Math.max(view.getWidth(), view.getHeight()) / 2;
        Animator animator = ViewAnimationUtils.createCircularReveal(view, x, y, 0, finalRadius);
        view.setVisibility(View.VISIBLE);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                getWindow().getEnterTransition().removeListener(transitionListener);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void exitReveal(int x, int y){
        final View view = findViewById(R.id.coordianotorLayout);
        int initialRadius = view.getWidth() / 2 ;
        Animator animator = ViewAnimationUtils.createCircularReveal(view, x, y, initialRadius, 0);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                view.setVisibility(View.INVISIBLE);
            }
        });
        animator.start();
    }

}
