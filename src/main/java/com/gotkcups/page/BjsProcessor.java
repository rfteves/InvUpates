/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.page;

import com.gotkcups.data.Constants;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bson.Document;

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
    int start = html.indexOf("<p class=models id=productModel>");
    int end = html.indexOf("</p>", start);
    if (start != -1) {
      String s = (String) vendor.get("sku");
      String sku = s.substring(0, s.length() - 1);
      String idp = html.substring(start, end);
      if (idp.contains(sku)) {
        start = html.indexOf("<input id=addItemTocartButton");
        if (start != -1) {
          vendor.put(Constants.Status, Constants.In_Stock);
          vendor.put(Constants.Min_Quantity, 1);
          start = html.indexOf("class=price4 id=prodFamilyId");
          start = html.indexOf("<span>", start);
          end = html.indexOf("</span>", start);
          String str = html.substring(start, end);
          Matcher m = Pattern.compile("[0-9]{1,}.[0-9]{2}").matcher(str);
          if (vendor.containsKey(Constants.Default_Cost) && vendor.getDouble(Constants.Default_Cost) > 0) {
            vendor.put(Constants.Final_Cost, vendor.getDouble(Constants.Default_Cost));
          } else if (m.find()) {
            double cost = Double.parseDouble(m.group());
            vendor.put(Constants.Final_Cost, cost);
          }
          if (html.contains("<div id=freeShipping")) {
            vendor.put(Constants.Shipping, 0d);
          } else if (vendor.containsKey(Constants.Default_Shipping) && vendor.getDouble(Constants.Default_Shipping) > 0) {
            vendor.put(Constants.Default_Shipping, vendor.getDouble(Constants.Default_Shipping));
          }
        } else {
          vendor.put(Constants.Status, Constants.Out_Of_Stock);
        }
      }
    }
  }
  private final static StringBuilder sb = new StringBuilder();
}
