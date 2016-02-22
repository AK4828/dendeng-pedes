package com.hoqii.fxpc.sales.entity;

import com.hoqii.fxpc.sales.core.DefaultPersistence;
import com.hoqii.fxpc.sales.core.commons.User;

/**
 * Created by meruvian on 05/10/15.
 */
public class AssigmentDetail extends DefaultPersistence {
    public enum AssigmentDetailStatus {
        OPEN, SETTLE, CLOSE
    }

    private Assigment assigment = new Assigment();
    private User agent = new User();
    private AssigmentDetailStatus status = AssigmentDetailStatus.OPEN;

    public Assigment getAssigment() {
        return assigment;
    }

    public void setAssigment(Assigment assigment) {
        this.assigment = assigment;
    }

    public User getAgent() {
        return agent;
    }

    public void setAgent(User agent) {
        this.agent = agent;
    }

    public AssigmentDetailStatus getStatus() {
        return status;
    }

    public void setStatus(AssigmentDetailStatus status) {
        this.status = status;
    }
}
