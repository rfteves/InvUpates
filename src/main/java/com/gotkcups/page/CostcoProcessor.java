/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.page;

import com.cwd.db.Base64Coder;
import com.gotkcups.data.Constants;
import com.gotkcups.io.Utilities;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author rfteves
 */
@Service
public class CostcoProcessor {

  @Autowired
  private Utilities utilities;
  
  public void costing(Document variant, String html) {
    dig(variant, html);
  }

  public void dig(Document vendor, String html) {
    if (html == null) {
      vendor.put(Constants.Status, Constants.Page_Not_Available);
      return;
    }
    if (html.contains("<h1>Product Not Found</h1>")) {
      vendor.put(Constants.Status, Constants.Product_Not_Found);
      return;
    }
    initProductOptions(vendor, html);
    String s = (String) vendor.get("sku");
    if (s.equals("1074897C")) {
      int b = 0;
    }
    String sku = utilities.trimSku(s);
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

  private void initProduct(Document variant, Document product, String html) {
    if (!product.getString("inventory").equalsIgnoreCase("IN_STOCK")) {
      variant.put(Constants.Status, Constants.Out_Of_Stock);
      return;
    } else if (Double.parseDouble(product.getString("ordinal")) <= 20) {
      variant.put(Constants.Status, Constants.Out_Of_Stock);
      return;
    }
    org.jsoup.nodes.Document doc = Jsoup.parse(html);
    if (doc.getElementById("grocery-fee-amount") != null) {
      //variant.put(Constants.Status, Constants.Out_Of_Stock);
      //return;
    }
    Matcher m = null;
    variant.put(Constants.OrdinalCount, Double.parseDouble(product.getString("ordinal")));
    variant.put(Constants.Status, Constants.In_Stock);
    if (product.getString("listPrice") != null) {
      String str = product.getString("listPrice");
      variant.put(Constants.List_Cost, Double.parseDouble(Base64Coder.decode(str)));
    }
    if (product.getString("price") != null) {
      String str = product.getString("price");
      double cost = Double.parseDouble(Base64Coder.decode(str));
      if (variant.getDouble(Constants.List_Cost).doubleValue() > cost) {
        variant.put(Constants.Final_Cost, variant.getDouble(Constants.List_Cost));
        variant.put(Constants.List_Cost, cost);
      } else {
        variant.put(Constants.Final_Cost, cost);
      }
    } else {
      variant.put(Constants.Final_Cost, variant.getDouble(Constants.List_Cost));
    }
    if (variant.getDouble(Constants.List_Cost) == -1) {
      Elements elements = doc.getElementsByClass("online-price");
      for (Element element : elements) {
        if (element.attr("data-catentry").equals(product.getString("catentry"))) {
          int k = 0;
          String str = element.attr("data-opvalue");
          double cost = Double.parseDouble(Base64Coder.decode(str));
          variant.put(Constants.List_Cost, cost);
          break;
        }
      }
    }
    boolean expired = true;
    if (variant.getDouble(Constants.List_Cost) > 30.0
      && doc.getElementsByClass("PromotionalText").size() == 1
      && !doc.getElementsByClass("PromotionalText").get(0).text().contains("Limit ")) {
      int start = html.indexOf("<p class=\"PromotionalText\">") + "<p class=\"PromotionalText\">".length();
      int end = html.indexOf("</p>", start);
      String str = html.substring(start, end);
      m = Pattern.compile("through [0-9]{1,2}/[0-9]{1,2}/[0-9]{2}").matcher(str);
      if (m.find() && !str.contains("Limit")) {
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
    variant.put(Constants.Discounted, !expired);
    if (!expired) {
      variant.put(Constants.Final_Cost, variant.get(Constants.List_Cost));
    }
    if (((Document) variant.get("vendor")).containsKey(Constants.ExtraCost)) {
      variant.put(Constants.ExtraCost, ((Document) variant.get("vendor")).get(Constants.ExtraCost));
    }
    int qty = retrieveMinimumQuantity(variant, html);
    variant.put(Constants.Min_Quantity, qty);
    int start = html.indexOf("<p id=\"shipping-statement\">");
    int end = html.indexOf("</p>", start);
    double shipping = 0;
    if (doc.getElementById("grocery-fee-amount") != null) {
      shipping = 3.0;
      variant.put(Constants.Shipping, 3.0d);
    } else if (start != -1) {
      String str = html.substring(start, end);
      m = Pattern.compile("[0-9]{1,}.[0-9]{2}").matcher(str);
      if (m.find()) {
        String group = m.group();
        if (group.equals("0:00") || group.equals("0.00")) {
          variant.put(Constants.Shipping, 0d);
        } else {
          shipping = Double.parseDouble(m.group());
          variant.put(Constants.Shipping, shipping);
        }
      } else {
        variant.put(Constants.Shipping, 0d);
      }
    } else {
      variant.put(Constants.Shipping, 0d);
    }
    boolean taxable = ((Document) variant.get("vendor")).getBoolean("taxable");
    if (!taxable) {
      variant.put(Constants.Final_Cost, variant.getDouble(Constants.Final_Cost) * 1.02);
    }
  }

  private int retrieveMinimumQuantity(Document variant, String html) {
    Document vendor = (Document) variant.get("vendor");
    int minqty = 1;
    Matcher m = Pattern.compile("Minimum Order Quantity: [0-9]{1,}").matcher(html);
    if (m.find()) {
      m = Pattern.compile("[0-9]{1,}").matcher(m.group());
      if (m.find()) {
        minqty = Integer.parseInt(m.group());
      }
      return minqty;
    } else if (vendor.get(Constants.Default_Min_Quantity) == null || vendor.getInteger(Constants.Default_Min_Quantity) <= 0) {
      return 1;
    } else {
      return vendor.getInteger(Constants.Default_Min_Quantity);
    }
  }

  public void initProductOptions(Document vendor, String html) {
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
  }

  private void describeOptions(Document vendor, Document product, Document options) {
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
