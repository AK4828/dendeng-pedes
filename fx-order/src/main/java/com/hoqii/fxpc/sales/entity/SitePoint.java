package com.hoqii.fxpc.sales.entity;

import com.hoqii.fxpc.sales.core.DefaultPersistence;
import com.hoqii.fxpc.sales.core.commons.Site;

/**
 * Created by miftakhul on 1/21/16.
 */
public class SitePoint extends DefaultPersistence{
    private Site site = new Site();
    private Site siteFrom = new Site();
    private double point;

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    public Site getSiteFrom() {
        return siteFrom;
    }

    public void setSiteFrom(Site siteFrom) {
        this.siteFrom = siteFrom;
    }

    public double getPoint() {
        return point;
    }

    public void setPoint(double point) {
        this.point = point;
    }
}
