/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.forms;

import com.gotkcups.io.RestHttpClient;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
 * @author ricardo
 */
public class Login {
  
  private RequestConfig globalConfig;
  private CookieStore cookieStore;
  
  public Login() {
    globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD_STRICT).build();
    cookieStore = new BasicCookieStore();
    HttpClientContext context = HttpClientContext.create();
    context.setCookieStore(cookieStore);
  }
  
  public org.bson.Document sendPost(String url, List<NameValuePair> postParams)
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

  public String sendGet(String url) throws Exception {
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
          String html, Map<String,String>keyval)
          throws UnsupportedEncodingException {
    Document doc = Jsoup.parse(html);
    Element loginform = doc.getElementsByTag("form").first();
    Elements inputElements = loginform.getElementsByTag("input");
    List<NameValuePair> paramList = new ArrayList<NameValuePair>();
    inputElements.stream().forEach(element->{
      String key = element.attr("name");
      String value = element.attr("value");
      keyval.keySet().stream().filter(k-> key.equals(key)).forEach(k->{
        paramList.add(new BasicNameValuePair(key, keyval.get(key)));
      });
      if (!paramList.contains(key)) {
        paramList.add(new BasicNameValuePair(key, value));
      }
    });
    return paramList;
  }
}
