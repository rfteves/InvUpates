/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.io;

import com.gotkcups.forms.Login;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


/**
 *
 * @author ricardo
 */
@Service
public class HttpService {

  @Value("${browser.user.agent}")
  private String browserUserAgent;

  private long lastGet = 0;
  
  private void throttle() {
    while (lastGet > System.currentTimeMillis()) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException ex) {
        Logger.getLogger(HttpService.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
    lastGet = System.currentTimeMillis() + 300;
  }
  
  public String processGet(String url) {
    String json = null;
    Scanner in = null;
    try {
      StringBuilder sb = new StringBuilder();
      HttpClient httpClient = HttpClientBuilder.create().build();
      HttpGet request = new HttpGet(url);
      request.setHeader("User-Agent", browserUserAgent);
      request.addHeader("Content-Type", "application/json;charset=UTF-8");
      request.addHeader("Accept", "application/json;charset=UTF-8");
      throttle();
      HttpResponse response = httpClient.execute(request);
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

  public String processPut(String url, String data) {
    String json = null;
    Scanner in = null;
    try {
      StringBuilder sb = new StringBuilder();
      HttpClient httpClient = HttpClientBuilder.create().build();
      HttpPut request = new HttpPut(url);
      request.setHeader("User-Agent", browserUserAgent);
      request.addHeader("Content-Type", "application/json;charset=UTF-8");
      request.addHeader("Accept", "application/json;charset=UTF-8");
      StringEntity input = new StringEntity(Utilities.clean(data));
      request.setEntity(input);
      throttle();
      HttpResponse response = httpClient.execute(request);
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

  public String processPost(String url, String data) {
    String json = null;
    Scanner in = null;
    try {
      StringBuilder sb = new StringBuilder();
      HttpClient httpClient = HttpClientBuilder.create().build();
      HttpPost request = new HttpPost(url);
      request.setHeader("User-Agent", browserUserAgent);
      request.addHeader("Content-Type", "application/json;charset=UTF-8");
      request.addHeader("Accept", "application/json;charset=UTF-8");
      String jsondata = data.toString();
      StringEntity input = new StringEntity(jsondata);
      request.setEntity(input);
      throttle();
      HttpResponse response = httpClient.execute(request);
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

  public void processDelete(String url) {
    Scanner in = null;
    try {
      StringBuilder sb = new StringBuilder();
      HttpClient httpClient = HttpClientBuilder.create().build();
      HttpDelete request = new HttpDelete(url);
      request.setHeader("User-Agent", browserUserAgent);
      request.addHeader("Content-Type", "application/json;charset=UTF-8");
      request.addHeader("Accept", "application/json;charset=UTF-8");
      throttle();
      HttpResponse response = httpClient.execute(request);
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
}
