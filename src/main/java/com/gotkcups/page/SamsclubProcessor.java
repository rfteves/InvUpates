/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.page;

import com.gotkcups.data.Constants;
import com.gotkcups.io.Utilities;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.bson.Document;

/**
 *
 * @author rfteves
 */
public class SamsclubProcessor {

  //<button class="biggreenbtn" tabindex="2" id="addtocartsingleajaxonline"> Ship this item</button>
  private static StringBuilder pad = new StringBuilder();

  public static void costing(Document variant, String html) {
    if (html == null) {
      variant.put(Constants.Status, Constants.Page_Not_Available);
      return;
    }
    if (html.contains("41,98")) {
      int i = 0;
    }
    String s = (String) variant.get("sku");
    String sku = Utilities.trimSku(s);
    String id = String.format("<span itemprop=productID>%s</span>", s.substring(0, s.length() - 1));
    String id2 = String.format("Item # %s", s.substring(0, s.length() - 1));
    if (html.contains("<div id=moneyBoxJson style=display:none>")) {
      int start = html.indexOf("<div id=moneyBoxJson style=display:none>") + "<div id=moneyBoxJson style=display:none>".length();
      int end = html.indexOf("</div>", start);
      pad.setLength(0);
      pad.append(StringEscapeUtils.unescapeHtml(html.substring(start, end)));
      Document mbj = Document.parse(pad.toString());
      List<Document> availableSkus = (List) mbj.get("availableSKUs");
      variant.put(Constants.Status, Constants.Out_Of_Stock);
      availableSkus.stream().filter(available -> available.getString("itemNo").equals(sku)
        && available.containsKey("onlineInventoryVO")).forEach(available -> {
        Document onlineInv = (Document) available.get("onlineInventoryVO");
        Document onlinePrice = (Document) available.get("onlinePriceVO");
        if (onlineInv == null || onlinePrice == null) {
          int debug = 0;
        }
        variant.put(Constants.Status, "inStock".equals(onlineInv.getString("status")) ? Constants.In_Stock : Constants.Out_Of_Stock);
        variant.put(Constants.Final_Cost, onlinePrice.getDouble("finalPrice"));
        variant.put(Constants.List_Cost, onlinePrice.getDouble("listPrice"));
        double shipping = retrieveShipping(variant, html);
        variant.put(Constants.Shipping, shipping);
      });
    } else if (html.contains(id)) {
      if (html.contains("<link itemprop=availability href=\"http://schema.org/InStock\"/>")
        && html.contains("<button class=biggreenbtn tabindex=2 id=addtocartsingleajaxonline> Ship this item</button>")) {
        variant.put(Constants.Status, Constants.In_Stock);
        double finalCost = retrieveCost(html);
        variant.put(Constants.Final_Cost, finalCost);
        double shipping = retrieveShipping(variant, html);
        variant.put(Constants.Shipping, shipping);
      } else {
        variant.put(Constants.Status, Constants.Out_Of_Stock);
      }
    } else if (html.contains(id2)) {
      if (html.contains("this item is not available in your selected club")
        || html.contains("Select a club for price and availability")) {
        variant.put(Constants.Status, Constants.Out_Of_Stock);
      } else if (html.contains(">Add to cart</button>")) {
        variant.put(Constants.Status, Constants.In_Stock);
        double finalCost = retrieveCost(html);
        variant.put(Constants.Final_Cost, finalCost);
        double shipping = retrieveShipping(variant, html);
        variant.put(Constants.Shipping, shipping);
      } else {
        variant.put(Constants.Status, Constants.Page_Not_Available);
      }
    } else {
      variant.put(Constants.Status, Constants.Product_Not_Found);
    }
    if (!variant.containsKey(Constants.Status)) {
      int debug = 0;
    }
    if (variant.getString(Constants.Status).equals(Constants.In_Stock)
      && (variant.getDouble(Constants.Final_Cost) == null || variant.getDouble(Constants.Final_Cost) <= 0)) {
      variant.put(Constants.Status, Constants.Product_Cost_Not_Found);
    }
    if (!variant.getString(Constants.Status).equals(Constants.In_Stock)) {
      return;
    }
    int qty = retrieveMinimumQuantity(variant);
    variant.put(Constants.Min_Quantity, qty);
    double cost = variant.getDouble(Constants.Final_Cost);
    if (variant.getDouble(Constants.Shipping) == 0 && cost < 50) {
      cost *= 1.04;
    } else if (cost < 50) {
      cost *= 1.02;
    }
    if (variant.getDouble(Constants.Shipping) == null) {
      variant.put(Constants.Shipping, 0d);
    }
    variant.put(Constants.Discounted, false);
    cost = Math.floor(cost * 100) / 100;
    variant.put(Constants.Final_Cost, cost);
  }

  private static double retrieveCost(String html) {
    double retval = -1;
    String[] patterns = {"<span class=\"striked strikedPrice\">\\$[0-9]{1,}.[0-9]{2}</span>",
      "<span itemprop=price>[0-9]{1,}.[0-9]{2}</span>",
      "<span class=hidden itemprop=price>[0-9]{1,}.[0-9]{2}</span>",
      "<span class=sc-channel-savings-list-price>\\$[0-9]{1,}.[0-9]{2}</span>",
      "<span class=Price-mantissa>[0-9]{1,}.[0-9]{2}</span>",
      "<span itemprop=priceCurrency content=USD>\\$</span><span itemprop=price>[0-9]{1,}.[0-9]{2}</span>"};
    for (String pattern : patterns) {
      Matcher m = Pattern.compile(pattern).matcher(html);
      if (m.find()) {
        m = Pattern.compile("[0-9]{1,}.[0-9]{2}").matcher(m.group());
        if (m.find()) {
          String parseme = m.group().replaceAll(",", ".").replaceAll(" ", "");
          retval = Math.max(retval, Double.parseDouble(parseme));
          break;
        }
      }
    }
    if (retval == -1) {
      String[][] patts = {{"<span class=price>[0-9]{1,}</span>", "<span class=superscript>[0-9]{2}</span>"},
      {"<span class=Price-characteristic>[0-9]{1,}</span>", "<span class=Price-mantissa>[0-9]{2}</span>"},
      {"<span aria-hidden=true class=Price-characteristic>[0-9]{1,}</span>", "<span aria-hidden=true class=Price-mantissa>[0-9]{2}</span>"}};
      for (String[] pats : patts) {
        for (String pattern : pats) {
          Matcher m = Pattern.compile(pattern).matcher(html);
          if (m.find()) {
            m = Pattern.compile("[0-9]{1,}").matcher(m.group());
            if (m.find()) {
              double r = Double.parseDouble(m.group());
              if (retval == -1) {
                retval = r;
              } else {
                retval += r / 100;
              }
              break;
            }
          }
        }
        if (retval != -1) {
          break;
        }
      }
    }
    return retval;
  }

  private static double retrieveShipping(Document variant, String html) {
    //Shipping trumps defaultShipping
    Document vendor = (Document)variant.get("vendor");
    if (html.contains("<div class=freeDelvryTxt>") || html.contains(">Free shipping</span>")) {
      return 0d;
    } else {
      // Not free shipping. Either we defined a defaultshipping or get it from application.properties
      if (vendor.get(Constants.Default_Shipping) == null || vendor.getDouble(Constants.Default_Shipping) == 0) {
        return Double.parseDouble(Utilities.getApplicationProperty("samsclub.defaultshipping"));
      } else {
        return vendor.getDouble(Constants.Default_Shipping);
      }
    }
  }

  private static int retrieveMinimumQuantity(Document variant) {
    Document vendor = (Document)variant.get("vendor");
    if (vendor.get(Constants.Default_Min_Quantity) == null || vendor.getInteger(Constants.Default_Min_Quantity) <= 0) {
      return 1;
    } else {
      return vendor.getInteger(Constants.Default_Min_Quantity);
    }
  }
}
