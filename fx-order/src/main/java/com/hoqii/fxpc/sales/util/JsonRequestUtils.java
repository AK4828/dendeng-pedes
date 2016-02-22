package com.hoqii.fxpc.sales.util;

import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoqii.fxpc.sales.SignageAppication;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by meruvian on 29/07/15.
 */
public class JsonRequestUtils {
    private List<NameValuePair> queryParams = new ArrayList<NameValuePair>();
    private List<NameValuePair> headers = new ArrayList<>();
    private String requestUri;

    public JsonRequestUtils(String requestUri) {
        this.requestUri = requestUri;
    }

    public JsonRequestUtils addQueryParam(String key, String value) {
        queryParams.add(new BasicNameValuePair(key, value));

        return this;
    }

    public JsonRequestUtils removeAllQueryParam() {
        queryParams.clear();

        return this;
    }

    public JsonRequestUtils addHeader(String key, String value) {
        headers.add(new BasicNameValuePair(key, value));

        return this;
    }

    public <T> HttpResponseWrapper<T> post(Object body, TypeReference<T> type) {
        String param = URLEncodedUtils.format(queryParams, "UTF-8");
        HttpPost httpPost = new HttpPost(requestUri + "?" + param);

        return request(httpPost, body, type);
    }

    public <T> HttpResponseWrapper<T> get(TypeReference<T> type) {
        String param = URLEncodedUtils.format(queryParams, "UTF-8");
        HttpGet httpGet = new HttpGet(requestUri + "?" + param);

        return request(httpGet, null, type);
    }

    public <T> HttpResponseWrapper<T> put(Object body, TypeReference<T> type) {
        String param = URLEncodedUtils.format(queryParams, "UTF-8");
        HttpPut httpPut = new HttpPut(requestUri + "?" + param);

        return request(httpPut, body, type);
    }

    public <T> HttpResponseWrapper<T> delete(Object body, TypeReference<T> type) {
        String param = URLEncodedUtils.format(queryParams, "UTF-8");
        HttpDelete httpDelete = new HttpDelete(requestUri + "?" + param);

        return request(httpDelete, body, type);
    }

    private <T> HttpResponseWrapper<T> request(HttpUriRequest uriRequest, Object body, TypeReference type) {
        try {
            ObjectMapper mapper = SignageAppication.getInstance().getJsonMapper();

            uriRequest.setHeader("Content-Type", "application/json");
            uriRequest.setHeader("Accept", "application/json");
            uriRequest.setHeader("Host", uriRequest.getURI().getHost());

            if (AuthenticationUtils.getCurrentAuthentication() != null) {
                uriRequest.setHeader("Authorization", "Bearer " +
                        AuthenticationUtils.getCurrentAuthentication().getAccessToken());
            }

            for (NameValuePair pair : headers) {
                uriRequest.setHeader(pair.getName(), pair.getValue());
                Log.d(getClass().getSimpleName(), "Header : " + pair.getName() + "  +  " + pair.getValue());
            }


            if (uriRequest instanceof HttpEntityEnclosingRequest) {
                HttpEntityEnclosingRequest r = (HttpEntityEnclosingRequest) uriRequest;

                if (body != null)
                    r.setEntity(new StringEntity(mapper.writeValueAsString(body)));
            }

            Log.d(getClass().getSimpleName(), uriRequest.getURI().toString());
            Log.d(getClass().getSimpleName(), Arrays.asList(uriRequest.getAllHeaders()).toString());

            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse response = httpClient.execute(uriRequest);

            Log.d(getClass().getSimpleName(), "Response Code: " + response.getStatusLine().getStatusCode()
                    + " " +response.getStatusLine().getReasonPhrase());

            return new HttpResponseWrapper<T>(response, mapper, type);
        } catch (UnsupportedEncodingException e) {
            Log.e(getClass().getName(), e.getMessage(), e);
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            Log.e(getClass().getName(), e.getMessage(), e);
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            Log.e(getClass().getName(), e.getMessage(), e);
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(getClass().getName(), e.getMessage(), e);
            e.printStackTrace();
        }

        return null;
    }

    public static class HttpResponseWrapper<T> {
        private HttpResponse httpResponse;
        T content = null;

        public HttpResponseWrapper(HttpResponse httpResponse, ObjectMapper mapper, TypeReference<T> type) throws IOException {
            this.httpResponse = httpResponse;

            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
                this.content = mapper.readValue(httpResponse.getEntity().getContent(), type);
        }

        public HttpResponseWrapper(T content) {
            this.content = content;
        }

        public T getContent() {
            return content;
        }

        public HttpResponse getHttpResponse() {
            return httpResponse;
        }
    }
}
