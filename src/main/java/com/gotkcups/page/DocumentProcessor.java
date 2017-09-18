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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.Document;

/**
 *
 * @author Ricardo
 */
public class DocumentProcessor extends Thread {

  private static boolean processing;
  private final static Log log = LogFactory.getLog(DocumentProcessor.class);

  public static void accept(Map<String, String> urls, Document vendors) {
    processing = true;
    process(urls, vendors);
    processing = false;
  }

  private static void process(Map<String, String> urls, Document vendors) {
    List<Document> obj = (List) vendors.get("vendors");
    obj.stream().forEach(vendor -> {
      //System.out.println("Start processing " + vendor.getString(Constants.Sku));
      String key = (String) vendor.get("url");
      String html = null;
      if (!key.toLowerCase().startsWith("http")) {
        System.out.println("Undefined url " + vendor.getString(Constants.Sku));
      } else if (!urls.containsKey(key)) {
        html = fetchPage(key);
        urls.put(key, html);
      } else {
        html = urls.get(key);
      }
      log.info(String.format("DocProcessing %s %s", vendor.get(Constants.Id), vendor.get(Constants.Sku)));
      if (html == null || html.startsWith("Severe Error")) {
        vendor.put(Constants.Status, Constants.Page_Not_Available);
      } else {
        fetchCost(vendor, key, urls.get(key));
        calculatePrice(vendor);
      }
      log.info(String.format("DocProcessing %s %s done", vendor.get(Constants.Id), vendor.get(Constants.Sku)));
    });
  }

  private static String fetchPage(String url) {
    try {
      Thread.sleep(500);
    } catch (InterruptedException ex) {
      Logger.getLogger(DocumentProcessor.class.getName()).log(Level.SEVERE, null, ex);
    }
    String html = null;
    int trials = 3;
    while (html == null && trials-- > 0) {
      try {
        html = RestHttpClient.processGetHtml(url);
      } catch (Throwable e) {
        System.err.println("Unable to fetch " + url);
      }
    }
    return html;
  }

  private static void fetchCost(Document vendor, String key, String html) {
    if (key.contains("samsclub.com")) {
      SamsclubProcessor.costing(vendor, html);
    } else if (key.contains("costco.com")) {
      CostcoProcessor.costing(vendor, html);
    } else if (key.contains("keurig.com")) {
      KeurigProcessor.costing(vendor, html);
    } else if (key.contains("bjs.com")) {
      BjsProcessor.costing(vendor, html);
    }
  }
  public final static double MARKUP_TAXABLE = 0.82;
  public final static double MARKUP_NON_TAXABLE = 0.9;
  public final static double MARKUP_DISCOUNT = 0.04;

  private static void calculatePrice(Document vendor) {
    if (!vendor.containsKey(Constants.Status)) {
      vendor.put(Constants.Status, Constants.Page_Not_Available);
      return;
    } else if (!vendor.get(Constants.Status).equals(Constants.In_Stock)) {
      return;
    }
    double cost = vendor.getDouble(Constants.Final_Cost);
    int minqty = vendor.getInteger(Constants.Min_Quantity);
    double shipping = vendor.getDouble(Constants.Shipping);
    boolean taxable = vendor.getBoolean(Constants.Taxable);
    boolean discounted = vendor.getBoolean(Constants.Discounted);
    double price = cost * minqty;
    price += shipping;
    if (minqty >= 5 && price > 50) {
      price -= minqty * 0.75;
    }
    if (taxable) {
      price /= (MARKUP_TAXABLE - (discounted ? MARKUP_DISCOUNT : 0.0));
    } else {
      price /= (MARKUP_NON_TAXABLE - (discounted ? MARKUP_DISCOUNT : 0.0));
    }
    price = Math.floor(price);
    if (price % 2 == 0) {
      price += 1.00;
    }
    price += 0.98;
    vendor.put(Constants.Final_Price, price);
    vendor.put(Constants.List_Price, 0d);
    if (discounted) {
    }
  }

  public static boolean isProcessing() {
    return processing;
  }
}
