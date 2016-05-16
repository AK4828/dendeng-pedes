package com.hoqii.fxpc.sales.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by miftakhul on 17/05/16.
 */
public class StringUtils {
    public static String encodeString(String string) {
        try {
            return URLEncoder.encode(string, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
