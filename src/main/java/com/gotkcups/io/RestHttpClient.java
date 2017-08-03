/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.io;

import java.util.Map;
import java.util.HashMap;

import java.util.Scanner;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;


/**
 *
 * @author ricardo
 */
public class RestHttpClient {

    private static Map<String, String> KEYS = new HashMap<String, String>();

    static {
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.client.protocol.ResponseProcessCookies", "fatal");
        RestHttpClient.initKeys();
    }

    public static String processGetHtml(String url) {
        String json = null;
        Scanner in = null;
        try {
            StringBuilder sb = new StringBuilder();
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpGet getRequest = new HttpGet(url);
            RequestConfig globalConfig = RequestConfig.custom()
                    .setCookieSpec(CookieSpecs.DEFAULT)
                    .build();
            RequestConfig localConfig = RequestConfig.copy(globalConfig)
                    .setCookieSpec(CookieSpecs.STANDARD_STRICT)
                    .build();
            getRequest.setConfig(localConfig);
            getRequest.addHeader("Content-Type", "text/html;charset=UTF-8");
            getRequest.addHeader("Accept", "text/html;charset=UTF-8");
            getRequest.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36 Edge/12.0");
            HttpResponse response = httpClient.execute(getRequest);
            in = new Scanner(response.getEntity().getContent());
            while (in.hasNext()) {
                sb.append(in.nextLine());
                sb.append("\r\n");
            }
            if (response.getStatusLine().getStatusCode() != 200) {
                sb.insert(0, "Severe Error\r\n\r\n\r\n");
                throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode() + sb.toString());
            }
            json = sb.toString();
        } catch (Throwable ex) {
            ex.printStackTrace();
        } finally {
            if (in != null) {
                in.close();
            }
            return json;
        }
    }

    public static String processGet(String url) {
        String json = null;
        Scanner in = null;
        try {
            StringBuilder sb = new StringBuilder();
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpGet getRequest = new HttpGet(url);
            getRequest.addHeader("Content-Type", "application/json;charset=UTF-8");
            getRequest.addHeader("Accept", "application/json;charset=UTF-8");
            HttpResponse response = httpClient.execute(getRequest);
            in = new Scanner(response.getEntity().getContent());
            while (in.hasNext()) {
                sb.append(in.nextLine());
                sb.append("\r\n");
            }
            if (response.getStatusLine().getStatusCode() != 200) {
                sb.insert(0, "\r\n\r\n\r\n");
                throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode() + sb.toString());
            }
            json = sb.toString();
        } catch (Throwable ex) {
            ex.printStackTrace();
        } finally {
            if (in != null) {
                in.close();
            }
            return json;
        }
    }

    public static String processPut(String url, String data) {
        String json = null;
        Scanner in = null;
        try {
            StringBuilder sb = new StringBuilder();
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpPut putRequest = new HttpPut(url);
            putRequest.addHeader("Content-Type", "application/json;charset=UTF-8");
            putRequest.addHeader("Accept", "application/json;charset=UTF-8");
            StringEntity input = new StringEntity(data);
            putRequest.setEntity(input);
            HttpResponse response = httpClient.execute(putRequest);
            in = new Scanner(response.getEntity().getContent());
            while (in.hasNext()) {
                sb.append(in.nextLine());
                sb.append("\r\n");
            }
            if (response.getStatusLine().getStatusCode() != 200) {
                sb.insert(0, "\r\n\r\n\r\n");
                throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode() + sb.toString());
            }
            json = sb.toString();
        } catch (Throwable ex) {
            ex.printStackTrace();
        } finally {
            if (in != null) {
                in.close();
            }
            return json;
        }
    }

    public static String processPost(String url, String data) {
        String json = null;
        Scanner in = null;
        try {
            StringBuilder sb = new StringBuilder();
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpPost postRequest = new HttpPost(url);
            postRequest.addHeader("Content-Type", "application/json;charset=UTF-8");
            postRequest.addHeader("Accept", "application/json;charset=UTF-8");
            String jsondata = data.toString();
            StringEntity input = new StringEntity(jsondata);
            postRequest.setEntity(input);
            HttpResponse response = httpClient.execute(postRequest);
            in = new Scanner(response.getEntity().getContent());
            while (in.hasNext()) {
                sb.append(in.nextLine());
            }
            if (response.getStatusLine().getStatusCode() != 201) {
                sb.insert(0, "\r\n\r\n\r\n");
                throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode() + sb.toString());
            }
            json = sb.toString();
        } catch (Throwable ex) {
            ex.printStackTrace();
        } finally {
            if (in != null) {
                in.close();
            }
            return json;
        }
    }

    private static void processDelete(String url) {
        Scanner in = null;
        try {
            StringBuilder sb = new StringBuilder();
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpDelete deleteRequest = new HttpDelete(url);
            deleteRequest.addHeader("Content-Type", "application/json;charset=UTF-8");
            deleteRequest.addHeader("Accept", "application/json;charset=UTF-8");
            HttpResponse response = httpClient.execute(deleteRequest);
            in = new Scanner(response.getEntity().getContent());
            while (in.hasNext()) {
                sb.append(in.nextLine());
            }
            if (response.getStatusLine().getStatusCode() != 200) {
                sb.insert(0, "\r\n\r\n\r\n");
                throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode() + sb.toString());
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    public static String getProductVariant(String env, String variant_id) {
        StringBuilder sb = new StringBuilder(RestHttpClient.getKeyPass(env));
        sb.append(String.format("/admin/variants/%s.json", variant_id));
        return RestHttpClient.processGet(sb.toString());
    }

    public static String getCollects(String env) {
        StringBuilder sb = new StringBuilder(RestHttpClient.getKeyPass(env));
        sb.append("/admin/collects.json");
        return RestHttpClient.processGet(sb.toString());
    }

    public static String getProducts(String env, Map<String, String> params) {
        StringBuilder url = new StringBuilder(RestHttpClient.getKeyPass(env));
        if (params != null && params.containsKey("id")) {
            url.append(String.format("/admin/products/%s.json", params.remove("id").toString()));
        } else {
            url.append(String.format("/admin/products.json"));
        }
        RestHttpClient.processParams(url, params);
        return RestHttpClient.processGet(url.toString());
    }

    private static void processParams(StringBuilder url, Map<String, String> params) {
        if (params != null && params.size() > 0) {
            url.append("?");
            boolean andit = false;
            for (String key : params.keySet()) {
                if (andit)url.append("&");
                andit = true;
                url.append(key);
                url.append("=");
                url.append(params.get(key));
            }
        }
    }

    public static String createVariantMetaField(String env, long productId, long variantId, String jsondata) {
        StringBuilder url = new StringBuilder(RestHttpClient.getKeyPass(env));
        url.append(String.format("/admin/products/%s/variants/%s/metafields.json", productId, variantId));
        return RestHttpClient.processPost(url.toString(), jsondata);
    }

    public static String getVariantMetaField(String env, long productId, long variantId) {
        StringBuilder url = new StringBuilder(RestHttpClient.getKeyPass(env));
        url.append(String.format("/admin/products/%s/variants/%s/metafields.json", productId, variantId));
        return RestHttpClient.processGet(url.toString());
    }

    public static String createProduct(String env, String json) {
        StringBuilder url = new StringBuilder(RestHttpClient.getKeyPass(env));
        url.append(String.format("/admin/products.json", ""));
        return RestHttpClient.processPost(url.toString(), json);
    }

    public static void deleteMetaField(String env, long productId, long metaid) {
        StringBuilder url = new StringBuilder(RestHttpClient.getKeyPass(env));
        url.append(String.format("/admin/products/%d/metafields/%d.json", productId, metaid));
        RestHttpClient.processDelete(url.toString());
    }

    public static String getCollects(String env, int limit, int page, Map<String, String> params) {
        StringBuilder sb = new StringBuilder(RestHttpClient.getKeyPass(env));
        StringBuilder url = new StringBuilder(String.format("/admin/custom_collections.json?limit=%d&page=%d&", limit, page));
        RestHttpClient.processParams(url, params);
        sb.append(url);
        return RestHttpClient.processGet(sb.toString());
    }

    public static String getProductUrl(String env, long productId, Map<String, String> params) {
        StringBuilder sb = new StringBuilder(RestHttpClient.getKeyPass(env));
        sb.append(String.format("/admin/products/%d.json", productId));
        RestHttpClient.processParams(sb, params);
        return sb.toString();
    }

    public static String updateProduct(String env, long productId, String data) {
        String retval = null;
        String url = getProductUrl(env, productId, null);
        retval = processPut(url, data);
        return retval;
    }

    public static String getProduct(String env, long productId, Map<String, String> params) {
        String url = getProductUrl(env, productId, params);
        String so = RestHttpClient.processGet(url);
        return so;
    }

    public static void initProductionKeys() {
        KEYS.put("prod", Utilities.getApplicationProperty("https.key"));
    }

    private static void initKeys() {
        StringBuilder sb = new StringBuilder("https://");
        sb.append(Utilities.getApplicationProperty("key.prod"));
        sb.append(":");
        sb.append(Utilities.getApplicationProperty("password.prod"));
        sb.append("@");
        sb.append(Utilities.getApplicationProperty("store.prod"));
        sb.append(".myshopify.com");
        KEYS.put("prod", sb.toString());
        sb = new StringBuilder("https://");
        sb.append(Utilities.getApplicationProperty("key.dev"));
        sb.append(":");
        sb.append(Utilities.getApplicationProperty("password.dev"));
        sb.append("@");
        sb.append(Utilities.getApplicationProperty("store.dev"));
        sb.append(".myshopify.com");
        KEYS.put("dev", sb.toString());
    }

    private static String getKeyPass(String env) {
        return KEYS.get(env);
    }

    public static void main(String[] args) throws Exception {
    }

    public static String getProductMetafields(String env, long productId) {
        StringBuilder sb = new StringBuilder(RestHttpClient.getKeyPass(env));
        sb.append(String.format("/admin/products/%d/metafields.json", productId));
        return RestHttpClient.processGet(sb.toString());
    }
  
}
