/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.tests;

import java.net.URI;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * A example that demonstrates how HttpClient APIs can be used to perform
 * form-based logon.
 */
public class ClientFormLogin {

  public static void main(String[] s) throws Exception {
    samsclub();
  }
  private static CloseableHttpClient SAMSCLUB;

  public static CloseableHttpClient samsclub() throws Exception {
    if (SAMSCLUB != null) {
      return SAMSCLUB;
    }
    BasicCookieStore cookieStore = new BasicCookieStore();
    CloseableHttpClient httpclient = HttpClients.custom()
      .setDefaultCookieStore(cookieStore)
      .build();
    try {
      HttpGet httpget = new HttpGet("https://www.samsclub.com/sams/account/signin/login.jsp?xid=hdr_account_login&redirectURL=%2Fsams%2Fhomepage.jsp%3Fxid%3Dhdr_account_logout%26locale%3Den_US%26DPSLogout%3Dtrue");
      CloseableHttpResponse response1 = httpclient.execute(httpget);
      try {
        HttpEntity entity = response1.getEntity();

        System.out.println("Login form get: " + response1.getStatusLine());
        EntityUtils.consume(entity);

        System.out.println("Initial set of cookies:");
        List<Cookie> cookies = cookieStore.getCookies();
        if (cookies.isEmpty()) {
          System.out.println("None");
        } else {
          for (int i = 0; i < cookies.size(); i++) {
            System.out.println("- " + cookies.get(i).toString());
          }
        }
      } finally {
        response1.close();
      }

      HttpUriRequest login = RequestBuilder.post()
        .setUri(new URI("https://www.samsclub.com/sams/account/signin/login.jsp?xid=hdr_account_login&redirectURL=%2Fsams%2Fhomepage.jsp%3Fxid%3Dhdr_account_logout%26locale%3Den_US%26DPSLogout%3Dtrue&displayMessage=false&displayMessage=false&_DARGS=/sams/account/signin/memberLogin_new.jsp"))
        .addParameter("txtLoginEmailID", "samscue@teves.us")
        .addParameter("txtLoginPwd", "Nji9Bhu8")
        .build();
      CloseableHttpResponse response2 = httpclient.execute(login);
      try {
        HttpEntity entity = response2.getEntity();

        System.out.println("Login form get: " + response2.getStatusLine());
        EntityUtils.consume(entity);

        System.out.println("Post logon cookies:");
        List<Cookie> cookies = cookieStore.getCookies();
        if (cookies.isEmpty()) {
          System.out.println("None");
        } else {
          for (int i = 0; i < cookies.size(); i++) {
            System.out.println("- " + cookies.get(i).toString());
          }
        }
      } finally {
        response2.close();
      }

      
      HttpGet httpget3 = new HttpGet("https://www.samsclub.com/sams/keurig-k425s/prod20081682.ip?xid=plp:product:1:1");
      CloseableHttpResponse response3 = httpclient.execute(httpget3);
      try {
        HttpEntity entity = response3.getEntity();

        System.out.println("Login form get: " + response1.getStatusLine());
        //EntityUtils.consume(entity);
        String content = EntityUtils.toString(entity);
        System.out.println(content);

        System.out.println("Initial set of cookies:");
        List<Cookie> cookies = cookieStore.getCookies();
        if (cookies.isEmpty()) {
          System.out.println("None");
        } else {
          for (int i = 0; i < cookies.size(); i++) {
            System.out.println("- " + cookies.get(i).toString());
          }
        }
      } finally {
        response3.close();
      }
      
      
    } finally {
      //httpclient.close();
      SAMSCLUB = httpclient;
    }

    return SAMSCLUB;
  }
}
