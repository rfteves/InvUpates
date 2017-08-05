/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.page;

import com.gotkcups.data.Constants;
import com.gotkcups.io.RestHttpClient;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bson.Document;

/**
 *
 * @author Ricardo
 */
public class DocumentProcessor extends Thread {

  private static boolean processing;

  public static void accept(Document vendors) {
    processing = true;
    process(vendors);
    processing = false;
  }

  private static void process(Document vendors) {
    List<Document> obj = (List) vendors.get("vendors");
    Map<String, String> urls = new HashMap<>();
    obj.stream().forEach(vendor -> {
      String key = (String) vendor.get("url");
      if (!urls.containsKey(key)) {
        urls.put(key, fetchPage(key));
      }
      fetchCost(vendor, key, urls.get(key));
      calculatePrice(vendor);
      System.out.println(vendor.getString(Constants.Status) + ": " + vendor);
    });
  }

  private static String fetchPage(String url) {
    try {
      Thread.sleep(1500);
    } catch (InterruptedException ex) {
      Logger.getLogger(DocumentProcessor.class.getName()).log(Level.SEVERE, null, ex);
    }
    String html = RestHttpClient.processGetHtml(url);
    return html;
  }

  private static void fetchCost(Document vendor, String key, String html) {
    if (key.contains("samsclub.com")) {
      SamsclubProcessor.costing(vendor, html);
    }
  }
  public final static double MARKUP_TAXABLE = 0.835;
  public final static double MARKUP_NON_TAXABLE = 0.9;
  public final static double MARKUP_DISCOUNT = 0.035;

  private static void calculatePrice(Document vendor) {
    if (!vendor.get(Constants.Status).equals(Constants.In_Stock)) {
      return;
    }
    double cost = vendor.getDouble(Constants.Final_Cost);
    int minqty = vendor.getInteger(Constants.Min_Quantity);
    double shipping = vendor.getDouble(Constants.Shipping);
    boolean taxable = vendor.getBoolean(Constants.Taxable);
    boolean discounted = vendor.getBoolean(Constants.Discounted);
    double price = cost * minqty;
    price += shipping;
    if (minqty >= 5 && price > 60) {
      price -= minqty * 0.20;
    }
    if (taxable) {
      price /= (MARKUP_TAXABLE - (discounted ? MARKUP_DISCOUNT : 0.0));
    } else {
      price /= (MARKUP_NON_TAXABLE - (discounted ? MARKUP_DISCOUNT : 0.0));
    }
    price = Math.floor(price) + 0.98;
    vendor.put(Constants.Final_Price, price);
    vendor.put(Constants.List_Price, 0d);
    if (discounted) {
    }
  }

  public static boolean isProcessing() {
    return processing;
  }
}
