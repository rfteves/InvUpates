/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.page;

import com.gotkcups.data.KeurigAnchor;
import com.gotkcups.io.RestHttpClient;
import com.gotkcups.io.Utilities;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author Ricardo
 */
public class KeurigRewards {

  private static KeurigRewards kw;

  public static KeurigAnchor getKeurigAnchor(String sku) {
    if (kw == null) {
      kw = new KeurigRewards();
      kw.login(sku);
    } else if (kw.life < System.currentTimeMillis()) {
      kw.logout();
      kw = new KeurigRewards();
      kw.login(sku);
    }
    kw.settle(sku);
    return kw.anchor;
  }

  private RequestConfig globalConfig;
  private CookieStore cookieStore;
  private KeurigAnchor anchor;
  private long life = System.currentTimeMillis() + (10 * 60 * 1000);
  private Document rewardsDocument;

  private KeurigRewards() {
    globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD_STRICT).build();
    cookieStore = new BasicCookieStore();
    HttpClientContext context = HttpClientContext.create();
    context.setCookieStore(cookieStore);
  }

  private void login(String sku) {
    try {
      String postUrl = "https://www.keurig.com/j_spring_security_check";
      String url = "https://www.keurig.com/login";
      String page = this.sendGet(url);
      List<NameValuePair> postParams = this.getFormParams(page,
        Utilities.getApplicationProperty("keurig.user"), Utilities.getApplicationProperty("keurig.password"));
      org.bson.Document cookies = this.sendPost(postUrl, postParams);
      String rewardsUrl = "https://www.keurig.com/membership/rewards-catalog?show=All";
      String rewards = this.sendGet(rewardsUrl);
      rewardsDocument = Jsoup.parse(rewards);
    } catch (Exception ex) {
      Logger.getLogger(KeurigRewards.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private void settle(String sku) {
    String id = String.format("addToCartForm%s", sku);
    Element form = rewardsDocument.getElementById(id);
    if (form != null) {
      Elements inputs = form.getElementsByTag("input");
      anchor = new KeurigAnchor();
      for (Element input : inputs) {
        if (input.attr("name").equals("productCodePost")) {
          anchor.setDataCode(input.attr("value"));
        } else if (input.attr("name").equals("rPUnitPrice")) {//rPDisplayPrice
          anchor.setDataPrice(input.attr("value"));
        }
      }
      Elements button = form.getElementsByTag("button");
      anchor.setDataPurchasable("" + (button.size() == 1));
    }
  }

  private void logout() {
    try {
      String url = "https://www.keurig.com/logout";
      this.sendGet(url);
    } catch (Exception ex) {
      Logger.getLogger(KeurigRewards.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private org.bson.Document sendPost(String url, List<NameValuePair> postParams)
    throws Exception {
    org.bson.Document cookies = new org.bson.Document();
    CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(globalConfig).setDefaultCookieStore(cookieStore).build();
    HttpPost post = new HttpPost(url);
    post.setHeader("User-Agent", RestHttpClient.USER_AGENT);
    post.setHeader("Accept",
      "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
    post.setHeader("Accept-Language", "en-US,en;q=0.5");
    post.setHeader("Connection", "keep-alive");
    post.setHeader("Content-Type", "application/x-www-form-urlencoded");
    post.setEntity(new UrlEncodedFormEntity(postParams));
    HttpResponse response = httpClient.execute(post);
    HttpEntity entity = response.getEntity();
    String result = EntityUtils.toString(entity);
    Header[] headers = response.getAllHeaders();
    Arrays.asList(headers).stream().forEach(hed -> {
      cookies.put(hed.getName(), hed.getValue());
    });
    int responseCode = response.getStatusLine().getStatusCode();
    cookies.put("response-code", responseCode);
    return cookies;
  }

  private String sendGet(String url) throws Exception {
    CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(globalConfig).setDefaultCookieStore(cookieStore).build();
    HttpGet request = new HttpGet(url);
    request.setHeader("User-Agent", RestHttpClient.USER_AGENT);
    request.setHeader("Accept",
      "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
    request.setHeader("Accept-Language", "en-US,en;q=0.5");
    HttpResponse response = httpClient.execute(request);
    HttpEntity entity = response.getEntity();
    return EntityUtils.toString(entity);

  }

  public List<NameValuePair> getFormParams(
    String html, String username, String password)
    throws UnsupportedEncodingException {
    Document doc = Jsoup.parse(html);
    Element loginform = doc.getElementsByTag("form").first();
    Elements inputElements = loginform.getElementsByTag("input");
    List<NameValuePair> paramList = new ArrayList<NameValuePair>();
    for (Element inputElement : inputElements) {
      String key = inputElement.attr("name");
      String value = inputElement.attr("value");
      if (key.contains("j_username")) {
        value = username;
      } else if (key.equals("j_password")) {
        value = password;
      }
      paramList.add(new BasicNameValuePair(key, value));
    }
    return paramList;
  }
}
