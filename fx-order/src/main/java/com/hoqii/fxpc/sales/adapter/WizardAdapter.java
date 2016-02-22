package com.hoqii.fxpc.sales.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import org.meruvian.midas.wizard.model.Page;
import org.meruvian.midas.wizard.ui.ReviewFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ludviantoovandi on 10/02/15.
 */
public class WizardAdapter extends FragmentStatePagerAdapter {
    private int mCutOffPage;
    private Fragment mPrimaryItem;

    private List<Page> pages = new ArrayList<Page>();

    public WizardAdapter(FragmentManager fm, List<Page> pages) {
        super(fm);

        this.pages = pages;
    }

    public void setPages(List<Page> pages) {
        this.pages = pages;
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int position) {
        if (position == pages.size()) {
            return new ReviewFragment();
        }

        return pages.get(position).createFragment();
    }

    @Override
    public int getItemPosition(Object object) {
        // TODO: be smarter about this
        if (object == mPrimaryItem) {
            // Re-use the current fragment (its position never changes)
            return POSITION_UNCHANGED;
        }

        return POSITION_NONE;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        mPrimaryItem = (Fragment) object;
    }

    @Override
    public int getCount() {
        if (pages == null) {
            return 0;
        }
        return Math.min(mCutOffPage + 1, pages.size() + 1);
    }

    public void setCutOffPage(int cutOffPage) {
        if (cutOffPage < 0) {
            cutOffPage = Integer.MAX_VALUE;
        }
        mCutOffPage = cutOffPage;
    }

    public int getCutOffPage() {
        return mCutOffPage;
    }
}
