package com.hoqii.fxpc.sales.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.task.BusinessPartnerTask;
import com.hoqii.fxpc.sales.task.CategorySyncTask;
import com.hoqii.fxpc.sales.task.CategoryTotalElementsTask;
import com.hoqii.fxpc.sales.task.ContactSyncTask;
import com.hoqii.fxpc.sales.task.ImageProductTask;
import com.hoqii.fxpc.sales.task.ProductSyncTask;
import com.hoqii.fxpc.sales.task.ProductTotalElementsTask;
import com.hoqii.fxpc.sales.task.ProductUomsSyncTask;
import com.hoqii.fxpc.sales.task.RequestSiteSyncTask;

import org.meruvian.midas.core.defaults.DefaultActivity;
import org.meruvian.midas.core.service.TaskService;
import org.meruvian.midas.core.util.ConnectionUtil;

import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by ludviantoovandi on 30/01/15.
 */
public class SyncActivity extends DefaultActivity implements TaskService {
    @InjectView(R.id.text_sync)
    TextView textSync;
    @InjectView(R.id.button_sync)
    Button buttonSync;
    @InjectView(R.id.progressbar)
    ProgressBar progressBar;

    private CategoryTotalElementsTask categoryTotalElementsTask;
    private ProductTotalElementsTask productTotalElementsTask;

    private CategorySyncTask categorySyncTask;
    private ProductUomsSyncTask productUomsSyncTask;
    private ProductSyncTask productSyncTask;
    private ImageProductTask imageProductTask;
    private ContactSyncTask contactSyncTask;
    private RequestSiteSyncTask siteSyncTask;


    private BusinessPartnerTask businessPartnerTask;
    private int a = 1, i = 0;

    @Override
    protected int layout() {
        return R.layout.activity_sync;
    }

    @Override
    public void onViewCreated(Bundle bundle) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (ConnectionUtil.isInternetAvailable(this)) {
            new Handler().postDelayed(new Runnable() {
                public void run() {
//                    categorySyncTask = new CategorySyncTask(SyncActivity.this, SyncActivity.this);
//                    categorySyncTask.execute("0");
                    categoryTotalElementsTask = new CategoryTotalElementsTask(SyncActivity.this, SyncActivity.this);
                    categoryTotalElementsTask.execute();

                }
            }, 2000);
        } else {
            Toast.makeText(this, getString(R.string.check_internet), Toast.LENGTH_LONG).show();

            buttonSync.setVisibility(View.VISIBLE);
            textSync.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.button_sync)
    public void onClick(Button button) {
        if (button.getId() == R.id.button_sync) {
            categoryTotalElementsTask = new CategoryTotalElementsTask(SyncActivity.this, SyncActivity.this);
            categoryTotalElementsTask.execute();

        }
    }

    @Override
    public void onExecute(int code) {
        if (buttonSync.getVisibility() == View.VISIBLE) {
            buttonSync.setVisibility(View.GONE);
        }

        if (code == SignageVariables.PRODUCT_GET_TASK) {
            progressBar.setVisibility(View.VISIBLE);
            textSync.setVisibility(View.VISIBLE);
            textSync.setText(R.string.sync_product);
        } else if (code == SignageVariables.CATEGORY_GET_TASK) {
            progressBar.setVisibility(View.VISIBLE);
            textSync.setVisibility(View.VISIBLE);
            textSync.setText(R.string.sync_category);
        } else if (code == SignageVariables.IMAGE_PRODUCT_TASK) {
            progressBar.setVisibility(View.VISIBLE);
            textSync.setVisibility(View.VISIBLE);
            textSync.setText(R.string.sync_image);
        } else if (code == SignageVariables.PRODUCT_UOM_GET_TASK) {
            progressBar.setVisibility(View.VISIBLE);
            textSync.setVisibility(View.VISIBLE);
            textSync.setText(R.string.sync_product_uom);
        } else if (code == SignageVariables.CONTACT_GET_TASK) {
            progressBar.setVisibility(View.VISIBLE);
            textSync.setVisibility(View.VISIBLE);
            textSync.setText(R.string.sync_contact);
        } else if (code == SignageVariables.BUSINESS_PARTNER_GET_TASK) {
            progressBar.setVisibility(View.VISIBLE);
            textSync.setVisibility(View.VISIBLE);
            textSync.setText(R.string.sync_business_partner);
        } else if (code == SignageVariables.SITE_GET_TASK) {
            progressBar.setVisibility(View.VISIBLE);
            textSync.setVisibility(View.VISIBLE);
            textSync.setText(R.string.sync_site);
        }
    }

    @Override
    public void onSuccess(int code, Object result) {
        if (result != null) {

            Log.d("==============", result.toString());

            if (code == SignageVariables.CATEGORY_ELEMENTS_TASK) {
                categorySyncTask = new CategorySyncTask(this, this);
                categorySyncTask.execute(result.toString());

            } else if (code == SignageVariables.CATEGORY_GET_TASK) {
                productUomsSyncTask = new ProductUomsSyncTask(this, this);
                productUomsSyncTask.execute();
            } else if (code == SignageVariables.PRODUCT_UOM_GET_TASK) {
                contactSyncTask = new ContactSyncTask(this, this);
                contactSyncTask.execute();

            }
            else if (code == SignageVariables.CONTACT_GET_TASK) {
                productTotalElementsTask = new ProductTotalElementsTask(this, this);
                productTotalElementsTask.execute();
            }else if (code == SignageVariables.PRODUCT_ELEMENTS_TASK) {
                Log.e(getClass().getSimpleName(), "PRODUCT_ELEMENTS_TASK: Finish");
                productSyncTask = new ProductSyncTask(this, this);
                productSyncTask.execute(result.toString());

            } else if (code == SignageVariables.PRODUCT_GET_TASK) {
                Log.e(getClass().getSimpleName(), "PRODUCT_GET_TASK: Finish");
                imageProductTask = new ImageProductTask(this, this);
                imageProductTask.execute();
            } else if (code == SignageVariables.IMAGE_PRODUCT_TASK) {

                businessPartnerTask = new BusinessPartnerTask(this, this);
                businessPartnerTask.execute();


            } else if (code == SignageVariables.BUSINESS_PARTNER_GET_TASK) {
                siteSyncTask = new RequestSiteSyncTask(this, this);
                siteSyncTask.execute();
            }else if (code == SignageVariables.SITE_GET_TASK){
                progressBar.setVisibility(View.GONE);
                textSync.setText(R.string.finish_sync);

                Log.d(getClass().getSimpleName(), "SITE_TASK: Finish");

                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(SyncActivity.this).edit();

                        editor.putBoolean("has_sync", true);
                        editor.commit();

                        if (getIntent().getBooleanExtra("just_sync", false)) {
                            finish();
                        } else {
                            startActivity(new Intent(SyncActivity.this, MainActivity.class));
                            finish();
                        }
                    }
                }, 2000);
            }
        }
    }

    @Override
    public void onCancel(int code, String message) {
        if (buttonSync.getVisibility() == View.GONE) {
            buttonSync.setVisibility(View.VISIBLE);
            textSync.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        }

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onError(int code, String message) {
        if (buttonSync.getVisibility() == View.GONE) {
            buttonSync.setVisibility(View.VISIBLE);
            textSync.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        }

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
