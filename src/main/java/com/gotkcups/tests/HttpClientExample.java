/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.tests;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HttpClientExample {

  private String cookies;
  private HttpClient client = HttpClientBuilder.create().build();
  private final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:47.0) Gecko/20100101 Firefox/47.0";

  public static void main(String[] args) throws Exception {

    String url = "https://www.samsclub.com/sams/account/signin/login.jsp?xid=hdr_account_login&redirectURL=%2Fsams%2Fhomepage.jsp%3Fxid%3Dhdr_account_logout%26locale%3Den_US%26DPSLogout%3Dtrue";
    String gmail = "https://mail.google.com/mail/";

    // make sure cookies is turn on
    CookieHandler.setDefault(new CookieManager());

    HttpClientExample http = new HttpClientExample();

    String page = http.GetPageContent(url);

    List<NameValuePair> postParams
      = http.getFormParams(page, "samscue@teves.us", "Nji9Bhu8");

    http.sendPost(url, postParams);

    String result = http.GetPageContent(gmail);
    System.out.println(result);

    System.out.println("Done");
  }

  private void sendPost(String url, List<NameValuePair> postParams)
    throws Exception {

    HttpPost post = new HttpPost(url);

    // add header
    post.setHeader("Host", "www.samsclub.com");
    post.setHeader("User-Agent", USER_AGENT);
    post.setHeader("Accept",
      "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
    post.setHeader("Accept-Language", "en-US,en;q=0.5");
    post.setHeader("Cookie", getCookies());
    post.setHeader("Connection", "keep-alive");
    post.setHeader("Referer", "https://www.samsclub.com/sams/account/signin/login.jsp?xid=hdr_account_login&redirectURL=%2Fsams%2Fhomepage.jsp%3Fxid%3Dhdr_account_logout%26locale%3Den_US%26DPSLogout%3Dtrue&displayMessage=false&displayMessage=false&_DARGS=/sams/account/signin/memberLogin_new.jsp");
    post.setHeader("Content-Type", "application/x-www-form-urlencoded");

    post.setEntity(new UrlEncodedFormEntity(postParams));

    HttpResponse response = client.execute(post);

    int responseCode = response.getStatusLine().getStatusCode();

    System.out.println("\nSending 'POST' request to URL : " + url);
    System.out.println("Post parameters : " + postParams);
    System.out.println("Response Code : " + responseCode);

    BufferedReader rd = new BufferedReader(
      new InputStreamReader(response.getEntity().getContent()));

    StringBuffer result = new StringBuffer();
    String line = "";
    while ((line = rd.readLine()) != null) {
      result.append(line);
    }

    // System.out.println(result.toString());
  }

  private String GetPageContent(String url) throws Exception {

    HttpGet request = new HttpGet(url);

    request.setHeader("User-Agent", USER_AGENT);
    request.setHeader("Accept",
      "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
    request.setHeader("Accept-Language", "en-US,en;q=0.5");

    HttpResponse response = client.execute(request);
    int responseCode = response.getStatusLine().getStatusCode();

    System.out.println("\nSending 'GET' request to URL : " + url);
    System.out.println("Response Code : " + responseCode);

    BufferedReader rd = new BufferedReader(
      new InputStreamReader(response.getEntity().getContent()));

    StringBuffer result = new StringBuffer();
    String line = "";
    while ((line = rd.readLine()) != null) {
      result.append(line);
    }

    // set cookies
    setCookies(response.getFirstHeader("Set-Cookie") == null ? ""
      : response.getFirstHeader("Set-Cookie").toString());

    return result.toString();

  }

  public List<NameValuePair> getFormParams(
    String html, String username, String password)
    throws UnsupportedEncodingException {

    System.out.println("Extracting form's data...");

    Document doc = Jsoup.parse(html);

    // Google form id
    Element loginform = doc.getElementById("loginForm");
    Elements inputElements = loginform.getElementsByTag("input");

    List<NameValuePair> paramList = new ArrayList<NameValuePair>();

    for (Element inputElement : inputElements) {
      String key = inputElement.attr("name");
      String value = inputElement.attr("value");
      System.out.println(key + " " + value);
      if (key.contains("userName")) {
        value = username;
      } else if (key.equals(".password")) {
        value = password;
      }

      paramList.add(new BasicNameValuePair(key, value));

    }

    return paramList;
  }

  public String getCookies() {
    return cookies;
  }

  public void setCookies(String cookies) {
    this.cookies = cookies;
  }

}
