/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.page;

import com.gotkcups.data.Constants;
import com.gotkcups.data.KeurigAnchor;
import com.gotkcups.data.KeurigSelect;
import com.gotkcups.io.Utilities;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author rfteves
 */
@Service
public class KeurigProcessor {

  @Autowired
  private Utilities utilities;
  
  @Autowired
  private KeurigRewards keurigRewards;
  
  public final static float KEURIG_DISCOUNT_BREWERS = 0.275f;
  public final static float KEURIG_DISCOUNT_BEVERAGES = 0.125f;

  public void costing(Document variant, String html) {
    if (html == null) {
      variant.put(Constants.Status, Constants.Page_Not_Available);
      return;
    }
    if (html.contains("<h1>Product Not Found</h1>")) {
      variant.put(Constants.Status, Constants.Product_Not_Found);
      return;
    }
    String url = ((Document)variant.get("vendor")).getString("url");
    if (url.contains("/Beverages") && html.contains("<select id=\"package-variant-select\"")) {
      int start = html.indexOf("<select id=\"package-variant-select\"");
      int end = html.indexOf("</select>", start) + 9;
      String options = html.substring(start, end).replaceAll("[\t\r\n]", " ").replaceAll("[ ]{2,}", " ").replaceAll(" \\& ", " and ");
      while (options.contains("data-content=\"<span ")) {
        start = options.indexOf(" data-content=\"<span ");
        end = options.indexOf("</span>\">", 20);
        options = options.substring(0, start) + options.substring(end + 8);
        options = options.replaceAll(",", "");
      }
      KeurigSelect select = (KeurigSelect) utilities.objectify(options, new KeurigSelect());
      select.getOption().stream().filter(o -> variant.getString("sku").startsWith(o.getDataCode().concat("K"))).forEach(o -> {
        if (o.getDataStock().equalsIgnoreCase("inStock") && o.getDataPurchasable().equalsIgnoreCase("true")) {
          variant.put(Constants.Status, Constants.In_Stock);
          variant.put(Constants.Discounted, false);
          double cost = Double.parseDouble(o.getDataPrice().substring(1));
          cost = Math.round((cost * (1 - KEURIG_DISCOUNT_BEVERAGES)) * 100) * 0.01;
          variant.put(Constants.Final_Cost, cost);
          variant.put(Constants.Shipping, 0d);
          int qty = retrieveDefaultMinQty(variant);
          variant.put(Constants.Min_Quantity, qty);
        } else {
          variant.put(Constants.Status, Constants.Out_Of_Stock);
        }
      });
    } else if (url.contains("/Brewers") || url.contains("/Coffee-Makers")) {
      String s = (String) variant.get("sku");
      String sku = Utilities.trimSku(s);
      KeurigAnchor anchor = keurigRewards.getKeurigAnchor(sku);
      if (anchor != null && anchor.getDataPurchasable().equalsIgnoreCase("true")) {
        variant.put(Constants.Status, Constants.In_Stock);
        variant.put(Constants.Discounted, false);
        double cost = Double.parseDouble(anchor.getDataPrice());
        variant.put(Constants.Final_Cost, cost);
        variant.put(Constants.Shipping, 0d);
        variant.put(Constants.Min_Quantity, 1);
      } else {
        variant.put(Constants.Status, Constants.Out_Of_Stock);
      }
    } else {
      variant.put(Constants.Status, Constants.Product_Not_Found);
    }
  }

  private static int retrieveDefaultMinQty(Document variant) {
    Document vendor = (Document)variant.get("vendor");
    if (vendor.get(Constants.Default_Min_Quantity) == null || vendor.getInteger(Constants.Default_Min_Quantity) <= 0) {
      return 1;
    } else {
      return vendor.getInteger(Constants.Default_Min_Quantity);
    }
  }
  private final static StringBuilder sb = new StringBuilder();

}
