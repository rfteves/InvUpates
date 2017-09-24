/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.page;

import com.gotkcups.data.Constants;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bson.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author rfteves
 */
public class BjsProcessor {

  public static void costing(Document vendor, String html) {
    if (html == null) {
      vendor.put(Constants.Status, Constants.Page_Not_Available);
      return;
    }
    if (html.contains("<h1>Product Not Found</h1>")) {
      vendor.put(Constants.Status, Constants.Product_Not_Found);
      return;
    }
    org.jsoup.nodes.Document doc = Jsoup.parse(html);
    Element model = doc.getElementById("productModel");
    if (model != null) {
      int start = 0, end = 0;
      String s = (String) vendor.get("sku");
      String sku = s.substring(0, s.length() - 1);
      String idp = model.text();
      if (idp.contains(sku)) {
        String available = doc.getElementById("itemNotAvail").attr("style");
        if (available.equals("display:none")) {
          boolean shippingIncluded = false;
          vendor.put(Constants.Status, Constants.In_Stock);
          vendor.put(Constants.Min_Quantity, 1);
          vendor.put(Constants.Discounted, false);
          if (doc.getElementsByClass("shipping").size() == 1
            && doc.getElementsByClass("shipping").get(0).text().contains("Shipping Included")) {
            shippingIncluded = true;
          }
          boolean discounted = false;
          if (doc.getElementsByTag("strike").size() == 1) {
            vendor.put(Constants.Default_Cost, Double.parseDouble(doc.getElementsByTag("strike").get(0).text().substring(1)));
            discounted = true;
          } else if (doc.getElementsByClass("price-container").size() == 1
            && doc.getElementsByClass("price-container").get(0).getElementsByClass("amount").size() == 1) {
            vendor.put(Constants.Default_Cost, Double.parseDouble(doc.getElementsByClass("price-container").get(0).getElementsByClass("amount").text().substring(1)));
          }
          vendor.put(Constants.Final_Cost, Double.parseDouble(doc.getElementById("addToCartPrice").attr("value")));
          if (shippingIncluded) {
            vendor.put(Constants.Shipping, 0d);
          } else if (vendor.containsKey(Constants.Default_Shipping) && vendor.getDouble(Constants.Default_Shipping) > 0) {
            vendor.put(Constants.Shipping, vendor.getDouble(Constants.Default_Shipping));
          }
          if (discounted) {
            Elements scripts = doc.getElementsByTag("script");
            scripts.stream().filter(script -> script.outerHtml().contains("expirydate")).forEach(script -> {
              String text = script.outerHtml();
              Matcher m = Pattern.compile("[a-zA-Z]+ [0-9]{1,2}, [0-9]{4}").matcher(text);
              if (m.find()) {
                String group = m.group();
                SimpleDateFormat sdb = new SimpleDateFormat("MMMM d, yyyy");
                Calendar date = Calendar.getInstance();
                try {
                  date.setTime(sdb.parse(m.group()));
                  date.add(Calendar.HOUR, 19);
                  if (System.currentTimeMillis() > date.getTimeInMillis()) {
                    vendor.put(Constants.Discounted, false);
                  } else {
                    vendor.put(Constants.Discounted, true);
                  }
                } catch (ParseException ex) {
                  Logger.getLogger(CostcoProcessor.class.getName()).log(Level.SEVERE, null, ex);
                }
              }
            });
          }
        } else {
          vendor.put(Constants.Status, Constants.Out_Of_Stock);
        }
      }
    }
  }
  private final static StringBuilder sb = new StringBuilder();
}
