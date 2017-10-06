/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.tests;

import com.gotkcups.io.Utilities;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.http.Header;
import org.apache.http.HttpEntity;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HttpClientExample {

  private String cookies;
  private final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:47.0) Gecko/20100101 Firefox/47.0";

  public static void main(String[] args) throws Exception {

    String url = "https://gotkcups.myshopify.com/admin/auth/login";
    String abandon = "https://gotkcups.myshopify.com/admin/checkouts/47196700695";

    // make sure cookies is turn on
    RequestConfig globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD_STRICT).build();
    CookieStore cookieStore = new BasicCookieStore();
    HttpClientContext context = HttpClientContext.create();
    context.setCookieStore(cookieStore);

    HttpClientExample http = new HttpClientExample();

    String page = http.GetPageContent(url, globalConfig, cookieStore);

    List<NameValuePair> postParams
      = http.getFormParams(page, Utilities.getApplicationProperty("gotkcups.user"), Utilities.getApplicationProperty("gotkcups.password"));
    http.sendPost(url, globalConfig, cookieStore, postParams);
    String[] urls = new String[1];
    
    /*Arrays.asList(headers).stream().forEach((Header header) -> {
      System.out.println(header.getName() + ":::" + header.getValue());
      if (header.getName().equals("Location")) {
        urls[0] = header.getValue();
      }
      if (header.getName().equals("Set-Cookie")) {
        String[] splits = header.getValue().split(";");
        System.out.println(header.getValue());
        BasicClientCookie cookie = null;
        for (String split : splits) {
          String[] kv = split.split("=");
          System.out.println(kv[0] + ":::" + (kv.length == 2 ? kv[1] : null));
          if (cookie == null) {
            if (kv.length == 2) {
              cookie = new BasicClientCookie(kv[0].trim(), kv[1]);
            } else {
              cookie = new BasicClientCookie(kv[0].trim(), "");
            }
            cookie.setDomain(".myshopify.com");
            cookieStore.addCookie(cookie);
          } else {
            if (kv[0].trim().equals("secure")) {
              cookie.setSecure(true);
            } else if (kv[0].trim().equals("expires")) {
              try {
                SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss -0000 zzz");
                Date d = sdf.parse(kv[1].trim() + " GMT");
                Calendar k = Calendar.getInstance();
                cookie.setExpiryDate(k.getTime());
              } catch (ParseException ex) {
                Logger.getLogger(HttpClientExample.class.getName()).log(Level.SEVERE, null, ex);
              }
            } else if (kv[0].trim().equals("secure")) {
              cookie.setSecure(true);
            } else if (kv[0].trim().equals("path")) {
              cookie.setPath(kv[1]);
            } else if (kv[0].trim().equals("domain")) {
              cookie.setDomain(kv[1]);
            } else if (kv[0].trim().equals("HttpOnly")) {
              cookie.setAttribute("HttpOnly", "");
            } else if (kv.length == 2) {
              cookie.setAttribute(kv[0].trim(), kv[1]);
            } else {
              cookie.setAttribute(kv[0].trim(), "");
            }
          }
        }
      }

    });*/
    if (true) {
      //System.exit(0);
    }
    String result = null;//http.GetPageContent(urls[0], cookieStore);
    System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
    System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
    System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
    System.out.println(result);
    System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
    System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
    System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");

    System.out.println("Done");
  }

  private void sendPost(String url, RequestConfig globalConfig, CookieStore cookieStore, List<NameValuePair> postParams)
    throws Exception {
    CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(globalConfig).setDefaultCookieStore(cookieStore).build();
    HttpPost post = new HttpPost(url);
    post.setHeader("User-Agent", USER_AGENT);
    post.setHeader("Accept",
      "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
    post.setHeader("Accept-Language", "en-US,en;q=0.5");
    post.setHeader("Connection", "keep-alive");
    post.setHeader("Content-Type", "application/x-www-form-urlencoded");
    post.setEntity(new UrlEncodedFormEntity(postParams));
    HttpResponse response = httpClient.execute(post);
    HttpEntity entity = response.getEntity();
    String result = EntityUtils.toString(entity);
    System.out.println("result: " + result);
    //EntityUtils.consume(entity);
    Header[]headers = response.getAllHeaders();
    Arrays.asList(headers).stream().forEach(hed->{
      System.out.println(hed.getName() + ":::"+ hed.getValue());
    });
    int responseCode = response.getStatusLine().getStatusCode();
    System.out.println("\nSending 'POST' request to URL : " + url);
    System.out.println("Response Code : " + responseCode);
  }

  private String GetPageContent(String url, RequestConfig globalConfig, CookieStore cookieStore) throws Exception {
    CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(globalConfig).setDefaultCookieStore(cookieStore).build();
    
    HttpGet request = new HttpGet(url);
    request.setHeader("User-Agent", USER_AGENT);
    request.setHeader("Accept",
      "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
    request.setHeader("Accept-Language", "en-US,en;q=0.5");

    HttpResponse response = httpClient.execute(request);
    int responseCode = response.getStatusLine().getStatusCode();

    System.out.println("\nSending 'GET' request to URL : " + url);
    System.out.println("Response Code : " + responseCode);

    BufferedReader rd = new BufferedReader(
      new InputStreamReader(response.getEntity().getContent()));

    HttpEntity entity = response.getEntity();
    return EntityUtils.toString(entity);

  }

  public List<NameValuePair> getFormParams(
    String html, String username, String password)
    throws UnsupportedEncodingException {

    System.out.println("Extracting form's data...");

    Document doc = Jsoup.parse(html);

    // Google form id
    Element loginform = doc.getElementsByTag("form").first();
    Elements inputElements = loginform.getElementsByTag("input");

    List<NameValuePair> paramList = new ArrayList<NameValuePair>();

    for (Element inputElement : inputElements) {
      String key = inputElement.attr("name");
      String value = inputElement.attr("value");
      System.out.println(key + " " + value);
      if (key.contains("login")) {
        value = username;
      } else if (key.equals("password")) {
        value = password;
      }

      paramList.add(new BasicNameValuePair(key, value));

    }

    return paramList;
  }
}
