/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.page;

import com.cwd.db.Base64Coder;
import com.gotkcups.data.Constants;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringEscapeUtils;
import org.bson.Document;

/**
 *
 * @author rfteves
 */
public class CostcoProcessor {

  public static void costing(Document vendor, String html) {
    dig(vendor, html);
  }

  public static void dig(Document vendor, String html) {
    if (html == null) {
      vendor.put(Constants.Status, Constants.Page_Not_Available);
      return;
    }
    if (html.contains("<h1>Product Not Found</h1>")) {
      vendor.put(Constants.Status, Constants.Product_Not_Found);
      return;
    }
    if (html.contains("var products = ")) {
      int start = html.indexOf("var products = ") + 15;
      int end = html.indexOf("];", start) + 1;
      StringBuilder products = new StringBuilder(html.substring(start, end).replaceAll("[\n\r\t]", "").replaceAll("[ ]{2,}", " "));
      products.insert(0, "{\"products\":");
      products.append("}");
      vendor.put(Constants.Costco_Products, Document.parse(products.toString()));
    } else {
      vendor.put(Constants.Status, Constants.Product_Not_Found);
      return;
    }
    if (html.contains("var options = ")) {
      int start = html.indexOf("var options = ") + 14;
      int end = html.indexOf("];", start) + 1;
      StringBuilder options = new StringBuilder(html.substring(start, end).replaceAll("[\n\r\t]", "").replaceAll("[ ]{2,}", " "));
      options.insert(0, "{\"options\":");
      options.append("}");
      vendor.put(Constants.Costco_Options, Document.parse(options.toString()));
    }
    String s = (String) vendor.get("sku");
    String sku = s.substring(0, s.length() - 1);
    Document products = (Document) vendor.get(Constants.Costco_Products);
    List<Document> prods = (List) ((List) products.get("products")).get(0);
    Document options = (Document) vendor.get(Constants.Costco_Options);
    List<Document> opts = (List) ((List) options.get("options")).get(0);
    boolean inited = false;
    for (Document product : prods) {
      if (product.getString("partNumber").equals(sku)) {
        describeOptions(vendor, product, options);
        inited = true;
        initProduct(vendor, product, html);
        break;
      }
    }
    if (!inited) {
      vendor.put(Constants.Status, Constants.Out_Of_Stock);
    }
    vendor.remove(Constants.Costco_Options);
    vendor.remove(Constants.Costco_Products);
  }

  private static void initProduct(Document vendor, Document product, String html) {
    if (!product.getString("inventory").equalsIgnoreCase("IN_STOCK")) {
      vendor.put(Constants.Status, Constants.Out_Of_Stock);
      return;
    }
    vendor.put(Constants.Status, Constants.In_Stock);
    if (product.getString("listPrice") != null) {
      String str = product.getString("listPrice");
      vendor.put(Constants.List_Cost, Double.parseDouble(Base64Coder.decode(str)));
    }
    if (product.getString("price") != null) {
      String str = product.getString("price");
      double cost = Double.parseDouble(Base64Coder.decode(str));
      if (product.containsKey(Constants.Default_Cost) && product.getDouble(Constants.Default_Cost) > 0) {
        cost = product.getDouble(Constants.Default_Cost);
      }
      vendor.put(Constants.Final_Cost, cost);
    }
    boolean expired = true;
    if (html.contains("<p class=\"PromotionalText\">")) {
      int start = html.indexOf("<p class=\"PromotionalText\">") + "<p class=\"PromotionalText\">".length();
      int end = html.indexOf("</p>", start);
      String str = html.substring(start, end);
      Matcher m = Pattern.compile("through [0-9]{1,2}/[0-9]{1,2}/[0-9]{2}").matcher(str);
      if (m.find()) {
        m = Pattern.compile("[0-9]{1,2}/[0-9]{1,2}/[0-9]{2}").matcher(m.group());
        if (m.find()) {
          SimpleDateFormat sdb = new SimpleDateFormat("M/d/yy");
          Calendar date = Calendar.getInstance();
          try {
            date.setTime(sdb.parse(m.group()));
            date.add(Calendar.HOUR, 19);
            expired = System.currentTimeMillis() > date.getTimeInMillis();
          } catch (ParseException ex) {
            Logger.getLogger(CostcoProcessor.class.getName()).log(Level.SEVERE, null, ex);
          }
        }
      }
    }
    vendor.put(Constants.Discounted, !expired);
    int qty = retrieveMinimumQuantity(vendor);
    vendor.put(Constants.Min_Quantity, qty);
    int start = html.indexOf("<p id=\"shipping-statement\">");
    int end = html.indexOf("</p>", start);
    double shipping = 0;
    if (start != -1) {
      String str = html.substring(start, end);
      Matcher m = Pattern.compile("[0-9]{1,}.[0-9]{2}").matcher(str);
      if (m.find()) {
        String group = m.group();
        if (group.equals("0:00") || group.equals("0.00")) {
          shipping = 0d;
        } else {
          shipping = Double.parseDouble(m.group());
        }
      }
    }
    double defaultShipping = retrieveShipping(vendor);
    if (defaultShipping > 0) {
      vendor.put(Constants.Shipping, defaultShipping);
    } else if (shipping > 0) {
      vendor.put(Constants.Shipping, shipping);
    } else {
      vendor.put(Constants.Shipping, 0d);
    }
  }

  private static double retrieveShipping(Document product) {
    // 
    if (!product.containsKey(Constants.Default_Shipping) || product.getDouble(Constants.Default_Shipping) == 0) {
      return 0d;
    } else {
      return product.getDouble(Constants.Default_Shipping);
    }
  }

  private static int retrieveMinimumQuantity(Document product) {
    if (product.get(Constants.Default_Min_Quantity) == null || product.getInteger(Constants.Default_Min_Quantity) <= 0) {
      return 1;
    } else {
      return product.getInteger(Constants.Default_Min_Quantity);
    }
  }

  private static void describeOptions(Document vendor, Document product, Document options) {
    List<Document> opts = (List) ((List) options.get("options")).get(0);
    for (Document option : opts) {
      String key = StringEscapeUtils.unescapeHtml(option.getString("n"));
      String value = null;
      Document v = (Document) option.get("v");
      List<String> productOptions = (List) product.get("options");
      for (String productOption : productOptions) {
        if (v.containsKey(productOption)) {
          value = StringEscapeUtils.unescapeHtml(v.getString(productOption));
          break;
        }
      }
      vendor.put(key, value);
    }
  }
}