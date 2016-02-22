package com.hoqii.fxpc.sales.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.adapter.WizardAdapter;
import com.hoqii.fxpc.sales.model.WizardModel;

import org.meruvian.midas.wizard.model.AbstractWizardModel;
import org.meruvian.midas.wizard.model.ModelCallbacks;
import org.meruvian.midas.wizard.model.Page;
import org.meruvian.midas.wizard.model.ReviewItem;
import org.meruvian.midas.wizard.ui.PageFragmentCallbacks;
import org.meruvian.midas.wizard.ui.ReviewFragment;
import org.meruvian.midas.wizard.ui.StepPagerStrip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by ludviantoovandi on 29/01/15.
 */
public class WizardActivity extends FragmentActivity implements
        PageFragmentCallbacks,
        ReviewFragment.Callbacks,
        ModelCallbacks {

    @InjectView(R.id.pager)
    ViewPager pager;
    @InjectView(R.id.strip) StepPagerStrip strip;
    @InjectView(R.id.next_button) Button nextButton;
    @InjectView(R.id.prev_button) Button prevButton;

    private WizardAdapter wizardAdapter;
    private AbstractWizardModel model;

    private boolean mConsumePageSelectedEvent;
    private boolean mEditingAfterReview;
    private List<Page> pages = new ArrayList<Page>();

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wizard);

        ButterKnife.inject(this);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (savedInstanceState != null) {
            model.load(savedInstanceState.getBundle("model"));
        }

        model = new WizardModel(this);
        model.registerListener(this);
        wizardAdapter = new WizardAdapter(getSupportFragmentManager(), model.getCurrentPageSequence());

        pager.setAdapter(wizardAdapter);

        strip.setOnPageSelectedListener(new StepPagerStrip.OnPageSelectedListener() {
            @Override
            public void onPageStripSelected(int position) {
                position = Math.min(wizardAdapter.getCount() - 1, position);
                if (pager.getCurrentItem() != position) {
                    pager.setCurrentItem(position);
                }
            }
        });

        pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                strip.setCurrentPage(position);

                if (mConsumePageSelectedEvent) {
                    mConsumePageSelectedEvent = false;
                    return;
                }

                mEditingAfterReview = false;
                updateBottomBar();
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pager.getCurrentItem() == pages.size()) {
                    final ProgressDialog progressDialog = new ProgressDialog(WizardActivity.this);
                    progressDialog.setMessage(getString(R.string.save_data));
                    progressDialog.show();

                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            List<String> timerEntries = Arrays.asList(getResources().getStringArray(R.array.slide_time_entries));
                            List<String> timerValues = Arrays.asList(getResources().getStringArray(R.array.slide_time_values));
                            List<String> idleEntries = Arrays.asList(getResources().getStringArray(R.array.slide_idle_entries));
                            List<String> idleValues = Arrays.asList(getResources().getStringArray(R.array.slide_idle_values));
                            List<String> periodicEntries = Arrays.asList(getResources().getStringArray(R.array.sync_periodic_entries));
                            List<String> periodicValues = Arrays.asList(getResources().getStringArray(R.array.sync_periodic_values));

                            ArrayList<ReviewItem> reviewItems = new ArrayList<ReviewItem>();
                            for (Page page : model.getCurrentPageSequence()) {
                                page.getReviewItems(reviewItems);
                            }

                            SharedPreferences.Editor editor = preferences.edit();
                            for (ReviewItem item : reviewItems) {
                                if (timerEntries.contains(item.getDisplayValue())) {
                                    int index = timerEntries.indexOf(item.getDisplayValue());
                                    editor.putString("slide_timer", timerValues.get(index));
                                } else if (idleEntries.contains(item.getDisplayValue())) {
                                    int index = idleEntries.indexOf(item.getDisplayValue());
                                    editor.putString("slide_idle", idleValues.get(index));
                                } else if (periodicEntries.contains(item.getDisplayValue())) {
                                    int index = periodicEntries.indexOf(item.getDisplayValue());
                                    editor.putString("sync_periodic", periodicValues.get(index));
                                } else if (item.getDisplayValue().equals(getString(R.string.offline))) {
                                    editor.putBoolean("sync", true);
                                } else if (item.getDisplayValue().equals(getString(R.string.online))) {
                                    editor.putBoolean("sync", false);
                                } else if (item.getDisplayValue().equals(getString(R.string.slide_auto))) {
                                    editor.putBoolean("slide_show", true);
                                } else if (item.getDisplayValue().equals(getString(R.string.slide_manual))) {
                                    editor.putBoolean("slide_show", false);
                                } else if (item.getTitle().equals("Nama Campaign")) {
                                    editor.putString("campaign_name", item.getDisplayValue());
                                } else if (item.getTitle().equals("Nama Site")) {
                                    editor.putString("site_name", item.getDisplayValue());
                                }
                            }

                            editor.putBoolean("has_wizard", true);
                            editor.commit();

                            Log.d(getClass().getSimpleName(), "Site Name: " + preferences.getString("site_name", "none"));

                            progressDialog.dismiss();

                            if (preferences.getBoolean("sync", false)) {
                                startActivity(new Intent(WizardActivity.this, SyncActivity.class));
                            } else {
                                startActivity(new Intent(WizardActivity.this, MainActivityMaterial.class));
                            }
                            WizardActivity.this.finish();
                        }
                    }, 2000);
                } else {
                    if (mEditingAfterReview) {
                        pager.setCurrentItem(wizardAdapter.getCount() - 1);
                    } else {
                        pager.setCurrentItem(pager.getCurrentItem() + 1);
                    }
                }
            }
        });

        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pager.setCurrentItem(pager.getCurrentItem() - 1);
            }
        });

        onPageTreeChanged();
        updateBottomBar();
    }

    @Override
    public void onPageTreeChanged() {
        pages = model.getCurrentPageSequence();
        wizardAdapter.setPages(pages);
        recalculateCutOffPage();
        strip.setPageCount(pages.size() + 1); // + 1 = review step
        wizardAdapter.notifyDataSetChanged();
        updateBottomBar();
    }

    private void updateBottomBar() {
        int position = pager.getCurrentItem();
        if (position == pages.size()) {
            nextButton.setText(org.meruvian.midas.wizard.R.string.finish);
            nextButton.setBackgroundResource(org.meruvian.midas.wizard.R.drawable.finish_background);
            nextButton.setTextAppearance(this, org.meruvian.midas.wizard.R.style.TextAppearanceFinish);
        } else {
            nextButton.setText(mEditingAfterReview
                    ? org.meruvian.midas.wizard.R.string.review
                    : org.meruvian.midas.wizard.R.string.next);
            nextButton.setBackgroundResource(org.meruvian.midas.wizard.R.drawable.selectable_item_background);
            TypedValue v = new TypedValue();
            getTheme().resolveAttribute(android.R.attr.textAppearanceMedium, v, true);
            nextButton.setTextAppearance(this, v.resourceId);
            nextButton.setEnabled(position != wizardAdapter.getCutOffPage());
        }

        prevButton.setVisibility(position <= 0 ? View.INVISIBLE : View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        model.unregisterListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle("model", model.save());
    }

    @Override
    public AbstractWizardModel onGetModel() {
        return model;
    }

    @Override
    public void onEditScreenAfterReview(String key) {
        for (int i = pages.size() - 1; i >= 0; i--) {
            if (pages.get(i).getKey().equals(key)) {
                mConsumePageSelectedEvent = true;
                mEditingAfterReview = true;
                pager.setCurrentItem(i);
                updateBottomBar();
                break;
            }
        }
    }

    @Override
    public void onPageDataChanged(Page page) {
        if (page.isRequired()) {
            if (recalculateCutOffPage()) {
                wizardAdapter.notifyDataSetChanged();
                updateBottomBar();
            }
        }
    }

    @Override
    public Page onGetPage(String key) {
        return model.findByKey(key);
    }

    private boolean recalculateCutOffPage() {
        // Cut off the pager adapter at first required page that isn't completed
        int cutOffPage = pages.size() + 1;
        for (int i = 0; i < pages.size(); i++) {
            Page page = pages.get(i);
            if (page.isRequired() && !page.isCompleted()) {
                cutOffPage = i;
                break;
            }
        }

        if (wizardAdapter.getCutOffPage() != cutOffPage) {
            wizardAdapter.setCutOffPage(cutOffPage);
            return true;
        }

        return false;
    }
}