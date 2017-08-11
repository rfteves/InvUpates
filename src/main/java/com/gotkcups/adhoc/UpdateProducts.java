/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.adhoc;

import com.gotkcups.data.Constants;
import com.gotkcups.data.JDocument;
import static com.gotkcups.data.RequestsHandler.register;
import com.gotkcups.io.RestHttpClient;
import com.gotkcups.io.Utilities;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bson.Document;

/**
 *
 * @author ricardo
 */
public class UpdateProducts {

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    // TODO code application logic here
    String[]names = null;
    List<String> arrayList = new ArrayList<String>(
            Arrays.asList(names));
    

  }

  private static void loopProducts() throws Exception {
    int limit = 0;
    Map<String, String> params = new HashMap<>();
    params.put("fields", "id,title,variants");
    Set<Document> sorted = new TreeSet<>();
    Document resp = Utilities.getAllProducts("prod", params, 50, -1);
    List<Document> products = (List) resp.get("products");
    for (Document product : products) {
      List<Document> variants = (List) product.get("variants");
      for (Document variant : variants) {
        if (!(variant.getLong("product_id") == 6945798279l
                || variant.getLong("product_id") == 93350756417033l)) {
          //continue;
        }
        if (variant.getLong(Constants.Id) != 35213584842l) {
          //continue;
        }
        if (!variant.getString(Constants.Sku).toLowerCase().endsWith("k")) {
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
    int ordinal = 0;
    for (Document variant : sorted) {
      Document metafield = Utilities.getMetafield("prod", variant, Constants.Inventory, Constants.Vendor);
      if (metafield != null) {
        String value = metafield.getString("value");
        Document values = Document.parse(value);
        variant.put("results", values);
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
        waitForStatus(vendor);
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
      int qty = 0;
      if (status.equals(Constants.In_Stock) && currentStatus.equals(status)) {
        qty = (int) (MAX_PURCHASE / price);
        if (price.doubleValue() != currentPrice) {
          message.append(variant.getString(Constants.Sku) + " old: " + currentPrice + " change: " + price);
        } else if (variant.getInteger(Constants.Inventory_Quantity) < 100 || variant.getInteger(Constants.Inventory_Quantity) > qty) {
          message.append(variant.getString(Constants.Sku) + " change qty");
        } else {
          message.append(variant.getString(Constants.Sku) + " same: " + currentPrice);
        }
      } else if (status.equals(Constants.In_Stock)) {
        qty = (int) (MAX_PURCHASE / price);
        if (price.doubleValue() != currentPrice) {
          message.append(variant.getString(Constants.Sku) + " old: " + currentPrice + " change: " + price + " change inStock");
        } else if (variant.getInteger(Constants.Inventory_Quantity) < 100 || variant.getInteger(Constants.Inventory_Quantity) > qty) {
          message.append(variant.getString(Constants.Sku) + " change inStock");
        } else {
          message.append(variant.getString(Constants.Sku) + " same: " + currentPrice + " inStock");
        }
      } else if (!status.equals(currentStatus)) {
        message.append(variant.getString(Constants.Sku) + " change outOfStock");
      } else {
        if (status.equals(Constants.In_Stock)) {
          qty = (int) (MAX_PURCHASE / price);
          if (variant.getInteger(Constants.Inventory_Quantity) < 100 || variant.getInteger(Constants.Inventory_Quantity) > qty) {
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
        RestHttpClient.updateVariant(Constants.Production, variant.getLong(Constants.Id), pack.toJson());
        Thread.sleep(1000);
      } else {
        System.out.println(message.toString());
      }
    }
  }

  private static void waitForStatus(Document vendor) {
    while (true) {
      if (!vendor.containsKey(Constants.Status)) {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException ex) {
          Logger.getLogger(UpdateProducts.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("waiting status " + vendor.getString(Constants.Sku));
      } else {
        break;
      }
    }
  }

  private final static double MAX_PURCHASE = 5000;
  private static StringBuilder message = new StringBuilder();
}
