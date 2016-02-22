package org.meruvian.midas.wizard.model;

import android.support.v4.app.Fragment;
import android.text.TextUtils;

import org.meruvian.midas.wizard.ui.CampaignWizardFragment;
import org.meruvian.midas.wizard.ui.SiteWizardFragment;

import java.util.ArrayList;

/**
 * Created by meruvian on 21/05/15.
 */
public class SitePage extends Page {
    public static final String SITE_DATA_KEY = "site";

    public SitePage(ModelCallbacks callbacks, String title) {
        super(callbacks, title);
    }

    @Override
    public Fragment createFragment() {
        return SiteWizardFragment.create(getKey());
    }

    @Override
    public void getReviewItems(ArrayList<ReviewItem> dest) {
        dest.add(new ReviewItem("Nama Site", mData.getString(SITE_DATA_KEY), getKey(), -1));
    }

    @Override
    public boolean isCompleted() {
        return !TextUtils.isEmpty(mData.getString(SITE_DATA_KEY));
    }
}
