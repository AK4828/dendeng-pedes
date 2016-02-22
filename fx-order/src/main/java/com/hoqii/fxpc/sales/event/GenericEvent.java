package com.hoqii.fxpc.sales.event;

import com.hoqii.fxpc.sales.util.JsonRequestUtils;

/**
 * Created by meruvian on 29/07/15.
 */
public class GenericEvent {
    public static class RequestInProgress {
        private final int processId;

        public RequestInProgress(int processId) {
            this.processId = processId;
        }

        public int getProcessId() {
            return processId;
        }
    }

    public static class RequestSuccess {
        private final JsonRequestUtils.HttpResponseWrapper response;
        private final int processId;
        private final String refId;
        private final String entityId;

        public RequestSuccess(int processId, JsonRequestUtils.HttpResponseWrapper response, String refId, String entityId) {
            this.response = response;
            this.processId = processId;
            this.refId = refId;
            this.entityId = entityId;
         }

        public JsonRequestUtils.HttpResponseWrapper getResponse() {
            return response;
        }

        public int getProcessId() {
            return processId;
        }

        public String getRefId(){
            return refId;
        }

        public String getEntityId(){
            return entityId;
        }
    }

    public static class RequestFailed {
        private final JsonRequestUtils.HttpResponseWrapper response;
        private final int processId;

        public RequestFailed(int processId, JsonRequestUtils.HttpResponseWrapper response) {
            this.response = response;
            this.processId = processId;
        }

        public JsonRequestUtils.HttpResponseWrapper getResponse() {
            return response;
        }

        public int getProcessId() {
            return processId;
        }
    }
}
