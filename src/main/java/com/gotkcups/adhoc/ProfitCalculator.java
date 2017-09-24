/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.adhoc;

import com.gotkcups.data.Constants;
import com.gotkcups.data.JDocument;
import com.gotkcups.io.GateWay;
import static com.gotkcups.page.DocumentProcessor.MARKUP_DISCOUNT;
import static com.gotkcups.page.DocumentProcessor.MARKUP_NON_TAXABLE;
import static com.gotkcups.page.DocumentProcessor.MARKUP_TAXABLE;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.bson.Document;

/**
 *
 * @author rfteves
 */
public class ProfitCalculator {

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) throws Exception {
    list();
  }
  
  private static void list() throws IOException {
    int limit = 0;
    Map<String, String> params = new HashMap<>();
    params.put("fields", "id,title,product_type,variants");
    Set<Document> sorted = new TreeSet<>();
    Document resp = GateWay.getAllProducts("prod", params, 50, -11);
    List<Document> products = (List) resp.get("products");
    for (Document product : products) {
      if (!(product.getLong("id") == 6945801863L
        || product.getLong("id") == 9760556810l
        || product.getLong("id") == 10078370634l)) {
        continue;
      }
      List<Document> variants = (List) product.get("variants");
      for (Document variant : variants) {
        if (variant.getLong(Constants.Id) != 11838139146l) {
          //continue;
        }
        if (!variant.getString(Constants.Sku).toLowerCase().endsWith("1074897c")) {
          //continue;
        }
        if (limit++ > 200) {
          //break;
        }
        Document d = new JDocument();
        variant.put(Constants.Title, product.getString(Constants.Title));
        d.putAll(variant);
        sorted.add(d);
      }
    }
    StringBuilder builder = new StringBuilder();
    for (Document variant : sorted) {
      Document metafield = GateWay.getMetafield("prod", variant, Constants.Inventory, Constants.Vendor);
      builder.setLength(0);
      builder.append("shopify_us_");
      builder.append(variant.getLong(Constants.Product_Id));
      builder.append("_");
      builder.append(variant.getLong(Constants.Id));
      builder.append(SEPARATOR);
      builder.append(variant.getString(Constants.Title));
      builder.append(SEPARATOR);
      boolean taxable = !variant.getBoolean("taxable");
      double price = Double.parseDouble(variant.getString(Constants.Price));
      double cost = 0;
      if (taxable) {
        cost = price * (MARKUP_TAXABLE);
      } else {
        cost = price * MARKUP_NON_TAXABLE;
      }
      double defaultshipping = 0;
      if (metafield != null) {
        String value = metafield.getString("value");
        Document values = Document.parse(value);
        Document vendor = (Document)((List)values.get("vendors")).get(0);
        if (vendor.getString(Constants.URL).contains("/Coffee-Makers") ||
          vendor.getString(Constants.URL).contains("/Brewers")) {
          defaultshipping = -vendor.getDouble(Constants.DefaultShipping);
        } else if (vendor.containsKey(Constants.DefaultShipping)) {
          defaultshipping = vendor.getDouble(Constants.DefaultShipping);
        }
      }
      cost -= defaultshipping;
      cost = Math.floor(cost) + 1.98;
      builder.append(price - cost);
      System.out.println(builder.toString());
    }
  }
  
  public static String SEPARATOR = ",";
  public static double AVERAGE_TAX_RATE = 0.08;
}
