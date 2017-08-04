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
public class SamsclubProcessor {

    //<button class="biggreenbtn" tabindex="2" id="addtocartsingleajaxonline"> Ship this item</button>
    private static StringBuilder pad = new StringBuilder();

    public static void costing(Document vendor, String html) {
        if (html == null) {
            vendor.put(Constants.Status, Constants.Page_Not_Available);
            return;
        }
        String s = (String) vendor.get("sku");
        String sku = s.substring(0, s.length() - 1);
        String id = String.format("<span itemprop=productID>%s</span>", s.substring(0, s.length() - 1));
        String id2 = String.format("Item # %s", s.substring(0, s.length() - 1));
        if (html.indexOf("<div id=moneyBoxJson style=display:none>") > 0) {
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
        } else if (html.indexOf(id) != -1) {
            if (html.indexOf("<link itemprop=availability href=\"http://schema.org/InStock\"/>") > 0
              && html.indexOf("<button class=biggreenbtn tabindex=2 id=addtocartsingleajaxonline> Ship this item</button>") > 0) {
                vendor.put(Constants.Status, Constants.In_Stock);
                double finalCost = retrieveCost(html);
                vendor.put(Constants.Final_Cost, finalCost);
                double shipping = retrieveShipping(vendor, html);
                vendor.put(Constants.Shipping, shipping);
            } else {
                vendor.put(Constants.Status, Constants.Out_Of_Stock);
            }
        } else if (html.indexOf(id2) != -1) {
            if (html.indexOf("this item is not available in your selected club") != -1
              || html.indexOf("Select a club for price and availability") != -1) {
                vendor.put(Constants.Status, Constants.Out_Of_Stock);
            } else if (html.indexOf(">Add to cart</button>") != -1) {
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
        if (!vendor.getString(Constants.Status).equals(Constants.In_Stock)) {
            return;
        }
        int qty = retrieveMinimumQuantity(vendor);
        vendor.put(Constants.Min_Quantity, qty);
        double cost = vendor.getDouble(Constants.Final_Cost);
        if (vendor.getDouble(Constants.Shipping) == 0) {
            cost *= 1.04;
        } else {
            cost *= 1.02;
        }
        cost = Math.floor(cost * 100) / 100;
        vendor.put(Constants.Final_Cost, cost);
        System.out.println("vendor: "+vendor);
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
                    }
                }
            }
        }
        return retval;
    }

    private static double retrieveShipping(Document vendor, String html) {
        if (html.indexOf("<div class=freeDelvryTxt>") > 0 || html.indexOf(">Free shipping</span>") != -1) {
            return 0d;
        } else {
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
