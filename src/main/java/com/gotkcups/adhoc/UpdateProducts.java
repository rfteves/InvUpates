/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.adhoc;

import com.gotkcups.data.Constants;
import com.gotkcups.data.JDocument;
import static com.gotkcups.data.RequestsHandler.register;
import com.gotkcups.io.GateWay;
import com.gotkcups.io.Utilities;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.Document;

/**
 *
 * @author ricardo
 */
public class UpdateProducts {
  private final static Log log = LogFactory.getLog(UpdateProducts.class);
  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) throws Exception {
    log.info("am here");
    loopProducts();
  }

  private static void loopProducts() throws Exception {
    int limit = 0;
    Map<String, String> params = new HashMap<>();
    params.put("fields", "id,title,variants");
    Set<Document> sorted = new TreeSet<>();
    Document resp = GateWay.getAllProducts("prod", params, 50, -1);
    List<Document> products = (List) resp.get("products");
    for (Document product : products) {
      if (!(product.getLong("id") == 10093322442L
        || product.getLong("id") == 97605568109999l
        || product.getLong("id") == 933507564170339999l)) {
        //continue;
      }
      RearrangeVariants.process(product);
      List<Document> variants = (List) product.get("variants");
      for (Document variant : variants) {
        if (!variant.getString(Constants.Sku).toLowerCase().endsWith("b")) {
          //continue;
        }
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
        d.putAll(variant);
        sorted.add(d);
      }
    }
    for (Document variant : sorted) {
      Document metafield = GateWay.getMetafield("prod", variant, Constants.Inventory, Constants.Vendor);
      if (metafield != null) {
        String value = metafield.getString("value");
        Document values = Document.parse(value);
        variant.put("results", values);
        if (variant.get(Constants.Sku).equals("286985S")) {
          int i = 0;
        }
        System.out.println("sku" + variant.get(Constants.Sku) + " url:");
        register(values);
      }
    }
    for (Document variant : sorted) {
      if (!variant.containsKey("results")) {
        continue;
      }
      Document results = (Document) variant.get("results");
      List<Document> vendors = (List) results.get("vendors");
      String status = null;
      String currentStatus = variant.getInteger(Constants.Inventory_Quantity) > 0 ? Constants.In_Stock : Constants.Out_Of_Stock;
      Double price = null;
      Double currentPrice = Double.valueOf(variant.getString(Constants.Price));
      for (Document vendor : vendors) {
        Utilities.waitForStatus(vendor);
        if (vendor.getString(Constants.Status).equals(Constants.In_Stock)) {
          status = Constants.In_Stock;
          if (price == null || vendor.getDouble(Constants.Final_Price).doubleValue() < price) {
            price = vendor.getDouble(Constants.Final_Price);
          }
        } else if (status == null) {
          status = Constants.Out_Of_Stock;
        }
      }
      message.setLength(0);
      int minQty = 0, maxQty = 0, qty = 0;
      if (status.equals(Constants.In_Stock)) {
        maxQty = Math.min((int) (MAX_PURCHASE / price), 150);
        minQty = (int) (0.25 * maxQty);
      }
      if (status.equals(Constants.In_Stock) && currentStatus.equals(status)) {
        if (price.doubleValue() != currentPrice) {
          message.append(variant.getString(Constants.Sku) + " old: " + currentPrice + " change: " + price);
          qty = maxQty;
        } else if (variant.getInteger(Constants.Inventory_Quantity) < minQty
          || variant.getInteger(Constants.Inventory_Quantity) > maxQty) {
          qty = maxQty;
          message.append(variant.getString(Constants.Sku) + " change qty");
        } else {
          message.append(variant.getString(Constants.Sku) + " same: " + currentPrice);
        }
      } else if (status.equals(Constants.In_Stock)) {
        if (price.doubleValue() != currentPrice) {
          message.append(variant.getString(Constants.Sku) + " old: " + currentPrice + " change: " + price + " change inStock");
          qty = maxQty;
        } else if (variant.getInteger(Constants.Inventory_Quantity) < minQty
          || variant.getInteger(Constants.Inventory_Quantity) > maxQty) {
          qty = maxQty;
          message.append(variant.getString(Constants.Sku) + " change inStock");
        } else {
          message.append(variant.getString(Constants.Sku) + " same: " + currentPrice + " inStock");
        }
      } else if (!status.equals(currentStatus)) {
        message.append(variant.getString(Constants.Sku) + " change outOfStock");
      } else {
        if (status.equals(Constants.In_Stock)) {
          if (variant.getInteger(Constants.Inventory_Quantity) < minQty
            || variant.getInteger(Constants.Inventory_Quantity) > maxQty) {
            qty = maxQty;
            message.append(variant.getString(Constants.Sku) + " change qty");
          }
        }
      }
      if (message.toString().contains("change")) {
        Document change = new Document();
        change.put(Constants.Id, variant.getLong(Constants.Id));
        if (status.equals(Constants.In_Stock)) {
          change.put(Constants.Inventory_Quantity, qty);
          change.put(Constants.Price, price.toString());
          change.put(Constants.Compare_At_Price, Double.valueOf(0d).toString());
        } else {
          change.put(Constants.Inventory_Quantity, 0);
        }
        Document pack = new Document();
        pack.put(Constants.Variant, change);
        message.insert(0, ", ");
        message.insert(0, variant.getLong(Constants.Id));
        System.out.println(message.toString());
        int debug = 0;
        GateWay.updateVariant(Constants.Production, variant.getLong(Constants.Id), pack.toJson());
        Thread.sleep(1000);
      } else {
        //System.out.println(message.toString());
      }
    }
    System.exit(0);
  }

  private final static int MIN_PURCHASE = 6500;
  private final static int MAX_PURCHASE = 11500;
  private static StringBuilder message = new StringBuilder();
}
