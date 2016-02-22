package org.meruvian.midas.wizard.model;

import android.support.v4.app.Fragment;
import android.text.TextUtils;

import org.meruvian.midas.wizard.R;
import org.meruvian.midas.wizard.ui.CampaignWizardFragment;
import org.meruvian.midas.wizard.ui.CustomerInfoFragment;

import java.util.ArrayList;

/**
 * Created by meruvian on 01/04/15.
 */
public class CampaignPage extends Page {
    public static final String CAMPAIGN_DATA_KEY = "campaign";

    public CampaignPage(ModelCallbacks callbacks, String title) {
        super(callbacks, title);
    }

    @Override
    public Fragment createFragment() {
        return CampaignWizardFragment.create(getKey());
    }

    @Override
    public void getReviewItems(ArrayList<ReviewItem> dest) {
        dest.add(new ReviewItem("Nama Campaign", mData.getString(CAMPAIGN_DATA_KEY), getKey(), -1));
    }

    @Override
    public boolean isCompleted() {
        return !TextUtils.isEmpty(mData.getString(CAMPAIGN_DATA_KEY));
    }
}
