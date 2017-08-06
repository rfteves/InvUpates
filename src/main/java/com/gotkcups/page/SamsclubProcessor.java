/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.page;

import com.gotkcups.data.Constants;
import com.gotkcups.io.Utilities;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringEscapeUtils;
import org.bson.Document;

/**
 *
 * @author rfteves
 */
public class SamsclubProcessor {

  //<button class="biggreenbtn" tabindex="2" id="addtocartsingleajaxonline"> Ship this item</button>
  private static StringBuilder pad = new StringBuilder();

  public static void costing(Document vendor, String html) {
    if (html == null) {
      vendor.put(Constants.Status, Constants.Page_Not_Available);
      return;
    }
    String s = (String) vendor.get("sku");
    if (s.equals("586754S")) {
      System.out.println();
    }
    String sku = s.substring(0, s.length() - 1);
    String id = String.format("<span itemprop=productID>%s</span>", s.substring(0, s.length() - 1));
    String id2 = String.format("Item # %s", s.substring(0, s.length() - 1));
    if (html.contains("<div id=moneyBoxJson style=display:none>")) {
      int start = html.indexOf("<div id=moneyBoxJson style=display:none>") + "<div id=moneyBoxJson style=display:none>".length();
      int end = html.indexOf("</div>", start);
      pad.setLength(0);
      pad.append(StringEscapeUtils.unescapeHtml(html.substring(start, end)));
      Document mbj = Document.parse(pad.toString());
      List<Document> availableSkus = (List) mbj.get("availableSKUs");
      for (Document available : availableSkus) {
        if (available.getString("itemNo").equals(sku)) {
          Document onlineInv = (Document) available.get("onlineInventoryVO");
          Document onlinePrice = (Document) available.get("onlinePriceVO");
          vendor.put(Constants.Status, "inStock".equals(onlineInv.getString("status")) ? Constants.In_Stock : Constants.Out_Of_Stock);
          vendor.put(Constants.Final_Cost, onlinePrice.getDouble("finalPrice"));
          vendor.put(Constants.List_Cost, onlinePrice.getDouble("listPrice"));
          double shipping = retrieveShipping(vendor, html);
          vendor.put(Constants.Shipping, shipping);
          break;
        }
      }
    } else if (html.contains(id)) {
      if (html.contains("<link itemprop=availability href=\"http://schema.org/InStock\"/>")
        && html.contains("<button class=biggreenbtn tabindex=2 id=addtocartsingleajaxonline> Ship this item</button>")) {
        vendor.put(Constants.Status, Constants.In_Stock);
        double finalCost = retrieveCost(html);
        vendor.put(Constants.Final_Cost, finalCost);
        double shipping = retrieveShipping(vendor, html);
        vendor.put(Constants.Shipping, shipping);
      } else {
        vendor.put(Constants.Status, Constants.Out_Of_Stock);
      }
    } else if (html.contains(id2)) {
      if (html.contains("this item is not available in your selected club")
        || html.contains("Select a club for price and availability")) {
        vendor.put(Constants.Status, Constants.Out_Of_Stock);
      } else if (html.contains(">Add to cart</button>")) {
        vendor.put(Constants.Status, Constants.In_Stock);
        double finalCost = retrieveCost(html);
        vendor.put(Constants.Final_Cost, finalCost);
        double shipping = retrieveShipping(vendor, html);
        vendor.put(Constants.Shipping, shipping);
      } else {
        vendor.put(Constants.Status, Constants.Page_Not_Available);
      }
    } else {
      vendor.put(Constants.Status, Constants.Product_Not_Found);
    }
    if (vendor.getString(Constants.Status).equals(Constants.In_Stock)
      && (vendor.getDouble(Constants.Final_Cost) == null || vendor.getDouble(Constants.Final_Cost) <= 0)) {
      vendor.put(Constants.Status, Constants.Product_Cost_Not_Found);
    }
    if (!vendor.getString(Constants.Status).equals(Constants.In_Stock)) {
      return;
    }
    int qty = retrieveMinimumQuantity(vendor);
    vendor.put(Constants.Min_Quantity, qty);
    double cost = vendor.getDouble(Constants.Final_Cost);
    if (vendor.getDouble(Constants.Shipping) == 0 && cost < 50) {
      cost *= 1.04;
    } else {
      cost *= 1.02;
    }
    if (vendor.getDouble(Constants.Shipping) == null) {
      vendor.put(Constants.Shipping, 0d);
    }
    vendor.put(Constants.Discounted, false);
    cost = Math.floor(cost * 100) / 100;
    vendor.put(Constants.Final_Cost, cost);
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
          retval = Math.max(retval, Double.parseDouble(m.group()));
          break;
        }
      }
    }
    if (retval == -1) {
      // It's probably in two places
      String[] pats = {"<span class=price>[0-9]{1,}</span>", "<span class=superscript>[0-9]{2}</span>"};
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
    }
    if (retval == -1) {
      // It's probably in other two places
      String[] pats = {"<span class=Price-characteristic>[0-9]{1,}</span>", "<span class=Price-mantissa>[0-9]{2}</span>"};
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
    }
    return retval;
  }

  private static double retrieveShipping(Document vendor, String html) {
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

  private static int retrieveMinimumQuantity(Document vendor) {
    if (vendor.get(Constants.Default_Min_Quantity) == null || vendor.getDouble(Constants.Default_Min_Quantity) <= 0) {
      return 1;
    } else {
      return vendor.getInteger(Constants.Default_Min_Quantity);
    }
  }
}
