package com.hoqii.fxpc.sales.model;

import android.content.Context;
import android.support.v4.app.Fragment;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.fragment.v4.WelcomeWizardFragment;

import org.meruvian.midas.wizard.model.AbstractWizardModel;
import org.meruvian.midas.wizard.model.BranchPage;
import org.meruvian.midas.wizard.model.CampaignPage;
import org.meruvian.midas.wizard.model.Page;
import org.meruvian.midas.wizard.model.PageList;
import org.meruvian.midas.wizard.model.ReviewItem;
import org.meruvian.midas.wizard.model.SingleFixedChoicePage;
import org.meruvian.midas.wizard.model.SitePage;

import java.util.ArrayList;

/**
 * Created by ludviantoovandi on 10/02/15.
 */
public class WizardModel extends AbstractWizardModel {

    public WizardModel(Context context) {
        super(context);
    }

    @Override
    protected PageList onNewRootPageList() {
        PageList pages = new PageList();

        Page welcome = new Page(this, "Welcome") {
            @Override
            public Fragment createFragment() {
                return new WelcomeWizardFragment();
            }

            @Override
            public void getReviewItems(ArrayList<ReviewItem> dest) {

            }
        };
        pages.add(welcome);

        SingleFixedChoicePage periodicPage = new SingleFixedChoicePage(this, "Periode Sinkronisasi");
        periodicPage.setParentKey("periodic");
        periodicPage.setChoices(mContext.getResources().getStringArray(R.array.sync_periodic_entries));
        periodicPage.setValue("1 Hari");
        periodicPage.setRequired(true);

        BranchPage dataPage = new BranchPage(this, "Penggunaan Data");
        dataPage.setParentKey("sync");
        dataPage.addBranch(mContext.getString(R.string.online));
        dataPage.setRequired(true);
        dataPage.addBranch(mContext.getString(R.string.offline), periodicPage);

        SingleFixedChoicePage slideTimerPage = new SingleFixedChoicePage(this, mContext.getString(R.string.slide_timer));
        slideTimerPage.setParentKey("slide_timer");
        slideTimerPage.setChoices(mContext.getResources().getStringArray(R.array.slide_time_entries));
        slideTimerPage.setRequired(true);

        SingleFixedChoicePage slideIdlePage = new SingleFixedChoicePage(this, mContext.getString(R.string.slide_idle));
        slideIdlePage.setParentKey("slide_idle");
        slideIdlePage.setChoices(mContext.getResources().getStringArray(R.array.slide_idle_entries));
        slideIdlePage.setRequired(true);

        BranchPage slidePage = new BranchPage(this, "Slide Show");
        slidePage.setParentKey("slide_show");
        slidePage.setRequired(true);
        slidePage.addBranch(mContext.getString(R.string.slide_auto), slideTimerPage, slideIdlePage);
        slidePage.addBranch(mContext.getString(R.string.slide_manual));

        SitePage sitePage = new SitePage(this, "Site");
        sitePage.setParentKey("site_campaign");
        sitePage.setRequired(true);

        CampaignPage campaignPage = new CampaignPage(this, "Campaign");
        campaignPage.setParentKey("campaign_name");
        campaignPage.setRequired(true);

        pages.add(dataPage);
        pages.add(slidePage);
        pages.add(sitePage);
        pages.add(campaignPage);

        return pages;
    }
}
