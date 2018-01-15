/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.page;

import com.gotkcups.data.Constants;
import com.gotkcups.io.RestHttpClient;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Ricardo
 */
@Service
public class DocumentProcessor extends Thread {

  @Autowired
  private BjsProcessor bjsProcessor;
  @Autowired
  private CostcoProcessor costcoProcessor;
  @Autowired
  private KeurigProcessor keurigProcessor;
  @Autowired
  private SamsclubProcessor samsclubProcessor;
  
  private static boolean processing;
  private final static Log log = LogFactory.getLog(DocumentProcessor.class);

  public void accept(Map<String, String> urls, Document vendors) {
    processing = true;
    process(urls, vendors);
    processing = false;
  }

  private void process(Map<String, String> urls, Document variant) {
    Document vendori = (Document) variant.get("vendor");
    //System.out.println("Start processing " + vendor.getString(Constants.Sku));
    String key = (String) vendori.get("url");
    String html = null;
    if (!key.toLowerCase().startsWith("http")) {
      System.out.println("Undefined url " + variant.getString(Constants.Sku));
    } else if (!urls.containsKey(key)) {
      html = fetchPage(key);
      urls.put(key, html);
    } else {
      html = urls.get(key);
    }
    log.info(String.format("DocProcessing %s %s", variant.get(Constants.Product_Id), variant.get(Constants.Sku)));
    if (html == null || html.startsWith("Severe Error")) {
      variant.put(Constants.Status, Constants.Page_Not_Available);
    } else {
      if (variant.getBoolean("debug").booleanValue()) {
        File location = new File(String.format("%s.html", "" + variant.getLong("debug-id")));
        if (!location.exists()) {
          try {
            IOUtils.write(html.getBytes(), new FileOutputStream(location));
            System.out.println("Writing file for debug " + location.getCanonicalPath());
          } catch (Exception ex) {
            Logger.getLogger(SamsclubProcessor.class.getName()).log(Level.SEVERE, null, ex);
          }
        }
      }
      fetchCost(variant, key, urls.get(key));
      calculatePrice(variant);
    }
    log.info(String.format("DocProcessing %s %s done", variant.get(Constants.Product_Id), variant.get(Constants.Sku)));
  }

  private String fetchPage(String url) {
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

  private void fetchCost(Document variant, String key, String html) {
    if (key.contains("samsclub.com")) {
      samsclubProcessor.costing(variant, html);
    } else if (key.contains("costco.com")) {
      costcoProcessor.costing(variant, html);
    } else if (key.contains("keurig.com")) {
      keurigProcessor.costing(variant, html);
    } else if (key.contains("bjs.com")) {
      bjsProcessor.costing(variant, html);
    }
  }
  public final static double MARKUP_TAXABLE = 0.82;
  public final static double MARKUP_NON_TAXABLE = 0.9;
  public final static double MARKUP_DISCOUNT = 0.04;

  private void calculatePrice(Document variant) {
    if (!variant.containsKey(Constants.Status)) {
      variant.put(Constants.Status, Constants.Page_Not_Available);
      return;
    } else if (!variant.get(Constants.Status).equals(Constants.In_Stock)) {
      return;
    }
    double cost = variant.getDouble(Constants.Final_Cost);
    if (((Document)variant.get("vendor")).containsKey(Constants.ExtraCost)) {
      cost += ((Document)variant.get("vendor")).getDouble(Constants.ExtraCost);
    }
    int minqty = variant.getInteger(Constants.Min_Quantity);
    double shipping = variant.getDouble(Constants.Shipping);
    boolean taxable = ((Document)variant.get("vendor")).getBoolean("taxable");
    boolean discounted = variant.getBoolean(Constants.Discounted);
    double price = cost * minqty;
    price += shipping;
    if (minqty >= 5 && price > 50) {
      price -= minqty * DocumentProcessor.BUNDLE_DISCOUNT;
    }
    if (taxable) {
      price /= (MARKUP_TAXABLE - (discounted ? MARKUP_DISCOUNT : 0.0));
    } else {
      price /= (MARKUP_NON_TAXABLE - (discounted ? MARKUP_DISCOUNT : 0.0));
    }
    price = Math.floor(price);
    if (price > 100) {
      price -= 0.02;
    } else {
      price += 0.98;
    }
    variant.put(Constants.Final_Price, price);
    variant.put(Constants.List_Price, 0d);
    if (discounted) {
    }
  }

  public static boolean isProcessing() {
    return processing;
  }

  public static final double BUNDLE_DISCOUNT = 0.4;
}
