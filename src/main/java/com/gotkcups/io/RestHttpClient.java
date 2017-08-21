/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.io;

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

  

  

  public static String processGetHtml(String url) {
    Scanner in = null;
    StringBuilder sb = new StringBuilder("Severe Error\r\n\r\n\r\n");
    try {
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
      String edge = "Mozilla/5.0 (Windows NT 10.0; <64-bit tags>) AppleWebKit/<WebKit Rev> (KHTML, like Gecko) Chrome/<Chrome Rev> Safari/<WebKit Rev> Edge/<EdgeHTML Rev>.<Windows Build>";
      String other = "Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36 Edge/12.0";
      getRequest.addHeader("User-Agent", edge);
      HttpResponse response = httpClient.execute(getRequest);
      in = new Scanner(response.getEntity().getContent());
      sb.setLength(0);
      while (in.hasNext()) {
        sb.append(in.nextLine());
        sb.append("\r\n");
      }
      if (response.getStatusLine().getStatusCode() != 200) {
        sb.insert(0, "Severe Error\r\n\r\n\r\n");
        //throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode() + sb.toString());
      }
    } catch (Throwable ex) {
      System.out.println("Error URL: " + url);
      //ex.printStackTrace();
    } finally {
      if (in != null) {
        in.close();
      }
      return sb.toString();
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
      StringEntity input = new StringEntity(Utilities.clean(data));
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

  public static void processDelete(String url) {
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
}
