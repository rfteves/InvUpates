/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.data;

import com.gotkcups.io.GateWay;
import com.gotkcups.io.Utilities;
import com.gotkcups.page.DocumentProcessor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.Document;

/**
 *
 * @author Ricardo
 */
public class RequestsHandler extends Thread {

  private final static Log log = LogFactory.getLog(RequestsHandler.class);
  private final static List<Document> REQUESTS = new ArrayList<>();
  private static RequestsHandler HANDLER;

  public static void registerProduct(Document product) {
    List<Document> variants = (List) product.get("variants");
    Set<Document> sorted = new TreeSet<>();
    for (Document variant : variants) {
      Document d = new JDocument();
      d.putAll(variant);
      sorted.add(d);
    }
    // Get variant infos
    for (Document variant : sorted) {
      Document metafield = GateWay.getMetafield("prod", variant, Constants.Inventory, Constants.Vendor);
      if (metafield != null) {
        String value = metafield.getString("value");
        Document values = Document.parse(value);
        variant.put("results", values);
        register(values);
      }
    }
    // Now set prices/invqty
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
          message.append(variant.getString(Constants.Sku));
          message.append(" old: ");
          message.append(currentPrice);
          message.append(" change: ");
          message.append(price);
          qty = maxQty;
        } else if (variant.getInteger(Constants.Inventory_Quantity) < minQty
          || variant.getInteger(Constants.Inventory_Quantity) > maxQty) {
          qty = maxQty;
          message.append(variant.getString(Constants.Sku));
          message.append(" change qty");
        } else {
          message.append(variant.getString(Constants.Sku));
          message.append(" same: ");
          message.append(currentPrice);
        }
      } else if (status.equals(Constants.In_Stock)) {
        if (price.doubleValue() != currentPrice) {
          message.append(variant.getString(Constants.Sku));
          message.append(" old: ");
          message.append(currentPrice);
          message.append(" change: ");
          message.append(price);
          message.append(" change inStock");
          qty = maxQty;
        } else if (variant.getInteger(Constants.Inventory_Quantity) < minQty
          || variant.getInteger(Constants.Inventory_Quantity) > maxQty) {
          qty = maxQty;
          message.append(variant.getString(Constants.Sku));
          message.append(" change inStock");
        } else {
          message.append(variant.getString(Constants.Sku));
          message.append(" same: ");
          message.append(currentPrice);
          message.append(" inStock");
        }
      } else if (!status.equals(currentStatus)) {
        message.append(variant.getString(Constants.Sku));
        message.append(" change outOfStock");
      } else {
        if (status.equals(Constants.In_Stock)) {
          if (variant.getInteger(Constants.Inventory_Quantity) < minQty
            || variant.getInteger(Constants.Inventory_Quantity) > maxQty) {
            qty = maxQty;
            message.append(variant.getString(Constants.Sku));
            message.append(" change qty");
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
        GateWay.updateVariant(Constants.Production, variant.getLong(Constants.Id), pack.toJson());
      }
      MongoDBJDBC.updateVariantIP(variant, message);
      log.info(String.format("Variant %s, %s ", variant.getLong(Constants.Id), message.toString()));
    }
  }
  private final static int MAX_PURCHASE = 11500;
  private static StringBuilder message = new StringBuilder();
  

  public static void register(long id) {
    log.info("Register product " + id);
    String json = GateWay.getProduct(Constants.Production, id);
    Document result = Document.parse(json);
    Document product = (Document)result.get(Constants.Product);
    registerProduct(product);
  }
  
  public static void register(Document vendors) {
    if (HANDLER == null || !HANDLER.isAlive()) {
      synchronized (REQUESTS) {
        if (HANDLER == null) {
          HANDLER = new RequestsHandler();
          HANDLER.start();
        }
      }
    }
    HANDLER.add(vendors);
  }

  private void add(Document vendors) {
    synchronized (this) {
      while (!REQUESTS.isEmpty()) {
        try {
          this.wait();
        } catch (InterruptedException ex) {
          Logger.getLogger(RequestsHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
      REQUESTS.add(vendors);
      this.notifyAll();
    }
  }

  public void run() {
    Document vendors = null;
    Map<String, String> urls = new LinkedHashMap<>();
    while (true) {
      synchronized (this) {
        while (REQUESTS.isEmpty()) {
          try {
            this.wait();
          } catch (InterruptedException ex) {
            Logger.getLogger(RequestsHandler.class.getName()).log(Level.SEVERE, null, ex);
          }
        }
        vendors = REQUESTS.remove(0);
        this.notifyAll();
      }
      if (urls.size() > 30) {
        urls.clear();
      }
      DocumentProcessor.accept(urls, vendors);
    }
  }
}
