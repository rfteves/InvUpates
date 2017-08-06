/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.page;

import com.gotkcups.data.Constants;
import com.gotkcups.data.KeurigSelect;
import com.gotkcups.data.KeurigSpan;
import com.gotkcups.io.Utilities;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bson.Document;

/**
 *
 * @author rfteves
 */
public class KeurigProcessor {

  public final static float KEURIG_DISCOUNT_BREWERS = 0.275f;
  public final static float KEURIG_DISCOUNT_BEVERAGES = 0.125f;

  public static void costing(Document product, String html) {
    if (html == null) {
      product.put(Constants.Status, Constants.Page_Not_Available);
      return;
    }
    if (html.contains("<h1>Product Not Found</h1>")) {
      product.put(Constants.Status, Constants.Product_Not_Found);
      return;
    }
    String url = product.getString("url");
    if (url.contains("/Beverages") && html.contains("<select id=\"package-variant-select\"")) {
      int start = html.indexOf("<select id=\"package-variant-select\"");
      int end = html.indexOf("</select>", start) + 9;
      String options = html.substring(start, end).replaceAll("[\t\r\n]", " ").replaceAll("[ ]{2,}", " ");
      KeurigSelect select = (KeurigSelect) Utilities.objectify(options, new KeurigSelect());
      select.getOption().stream().filter(o -> product.getString("sku").startsWith(o.getDataCode().concat("K"))).forEach(o -> {
        if (o.getDataStock().equalsIgnoreCase("inStock") && o.getDataPurchasable().equalsIgnoreCase("true")) {
          product.put(Constants.Status, Constants.In_Stock);
          double cost = Double.parseDouble(o.getDataPrice().substring(1));
          cost = Math.round((cost * (1 - KEURIG_DISCOUNT_BEVERAGES)) * 100) * 0.01;
          if (product.containsKey(Constants.Default_Cost) && product.getDouble(Constants.Default_Cost) > 0) {
            cost = product.getDouble(Constants.Default_Cost);
          }
          product.put(Constants.Final_Cost, cost);
          double shipping = retrieveShipping(product);
          product.put(Constants.Shipping, shipping);
          int qty = retrieveDefaultMinQty(product);
          product.put(Constants.Min_Quantity, qty);
        } else {
          product.put(Constants.Status, Constants.Out_Of_Stock);
        }
      });
    } else if (url.contains("/Brewers") && html.contains("<span class=\"custom-color-swatch\">")) {
      int start = html.indexOf("<span class=\"custom-color-swatch\">");
      int end = html.indexOf("</span>", start) + 7;
      String opts = html.substring(start, end).replaceAll("[\t\r\n]", " ").replaceAll("[\"]{2}", "\"\" ").replaceAll("[ ]{2,}", " ");
      int select = 0;
      sb.setLength(0);
      sb.append("<span>");
      while ((start = opts.indexOf("<a ", select)) != -1) {
        end = opts.indexOf("/a>", start) + 3;
        sb.append(opts.substring(start, end));
        select = end;
      }
      sb.append("</span>");
      KeurigSpan span = (KeurigSpan) Utilities.objectify(sb.toString(), new KeurigSpan());
      span.getAnchor().stream().filter(o -> product.getString("sku").startsWith(o.getDataCode().concat("K"))).forEach(o -> {
        if (o.getDataPurchasable().equalsIgnoreCase("true")) {
          product.put(Constants.Status, Constants.In_Stock);
          double cost = Double.parseDouble(o.getDataPrice().substring(1));
          cost = Math.round((cost * (1 - KEURIG_DISCOUNT_BREWERS)) * 100) * 0.01;
          if (product.containsKey(Constants.Default_Cost) && product.getDouble(Constants.Default_Cost) > 0) {
            cost = product.getDouble(Constants.Default_Cost);
          }
          product.put(Constants.Final_Cost, cost);
          double shipping = retrieveShipping(product);
          product.put(Constants.Shipping, shipping);
          product.put(Constants.Min_Quantity, 1);
        } else {
          product.put(Constants.Status, Constants.Out_Of_Stock);
        }
      });
    } else if (url.contains("/Coffee-Makers") && html.contains("<div class=\"in-stock\"")
      && html.contains("<button id=\"addToCartButton\" type=\"submit\"")) {
      product.put(Constants.Status, Constants.In_Stock);
      Matcher m = Pattern.compile("<div class=\"big-price left\">[\r\n\t ]+\\$[0-9]{1,}.[0-9]{2}</div>").matcher(html);
      if (m.find()) {
        m = Pattern.compile("[0-9]{1,}.[0-9]{2}").matcher(m.group());
        if (m.find()) {
          double cost = Double.parseDouble(m.group());
          cost = Math.round((cost * (1 - KEURIG_DISCOUNT_BREWERS)) * 100) * 0.01;
          if (product.containsKey(Constants.Default_Cost) && product.getDouble(Constants.Default_Cost) > 0) {
            cost = product.getDouble(Constants.Default_Cost);
          }
          product.put(Constants.Final_Cost, cost);
          double shipping = retrieveShipping(product);
          product.put(Constants.Shipping, shipping);
          product.put(Constants.Min_Quantity, 1);
        }
      }
    } else {
      product.put(Constants.Status, Constants.Product_Not_Found);
    }
  }

  private static double retrieveShipping(Document product) {
    // Beverages shipping are set in Shopify unless otherwise we specifically created defaultshipping
    if (!product.containsKey(Constants.Default_Shipping) || product.getDouble(Constants.Default_Shipping) == 0) {
      return 0d;
    } else {
      return product.getDouble(Constants.Default_Shipping);
    }
  }

  private static int retrieveDefaultMinQty(Document product) {
    if (product.get(Constants.Default_Min_Quantity) == null || product.getInteger(Constants.Default_Min_Quantity) <= 0) {
      return 1;
    } else {
      return product.getInteger(Constants.Default_Min_Quantity);
    }
  }
  private final static StringBuilder sb = new StringBuilder();
}
