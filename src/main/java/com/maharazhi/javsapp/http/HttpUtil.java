package com.maharazhi.javsapp.http;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static com.maharazhi.javsapp.common.Constants.HEADER_ORIGIN;

/**
 * HttpRequest can be send using this
 * This is written similar to JavaScript fetch() API
 */
public class HttpUtil {
    public static HashMap<String, String> getDefaultHeaders() {
        HashMap<String, String> params = new HashMap<>();
        params.put("Origin", HEADER_ORIGIN);
        return params;
    }

    public static String fetch(String url, String method, HashMap<String, String> params, byte[] body,
                               HashMap<String, String> headers) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(url + "?" + getParamsString(params)).openConnection();
        con.setRequestMethod(method);
        headers.putAll(getDefaultHeaders());
        headers.forEach(con::addRequestProperty);
        con.setDoOutput(!"get".equalsIgnoreCase(method));
        HttpURLConnection.setFollowRedirects(true);
        if (body != null) {
            try (DataOutputStream out = new DataOutputStream(con.getOutputStream())) {
                out.write(body);
            }
        }
        int responseCode = con.getResponseCode();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(
                responseCode < 299 ? con.getInputStream() : con.getErrorStream()))) {
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            return content.toString();
        }
    }

    static String getParamsString(Map<String, String> params) throws UnsupportedEncodingException {
        if (params == null) return null;
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            result.append("&");
        }
        String resultString = result.toString();
        return resultString.length() > 0 ? resultString.substring(0, resultString.length() - 1) : resultString;
    }
}
