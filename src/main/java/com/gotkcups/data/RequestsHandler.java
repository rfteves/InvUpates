/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.data;

import com.gotkcups.io.RestHelper;
import com.gotkcups.io.Utilities;
import com.gotkcups.page.DocumentProcessor;
import com.gotkcups.sendmail.SendMail;
import com.gotkcups.tools.CheckHello;
import java.util.ArrayList;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Ricardo
 */
@Service
public class RequestsHandler extends Thread {

  @Autowired
  protected RestHelper restHelper;
  
  @Autowired
  protected MongoDBJDBC mongodb;
  
  @Autowired
  protected DocumentProcessor documentProcessor;
  
  private final static Log log = LogFactory.getLog(RequestsHandler.class);
  private final List<Document> REQUESTS = new ArrayList<>();

  public void registerProduct(Document product) {
    List<Document> variants = (List) product.get("variants");
    Set<Document> sorted = new TreeSet<>();
    for (Document variant : variants) {
      Document d = new JDocument();
      d.putAll(variant);
      sorted.add(d);
    }
    // Get variant infos
    for (Document variant : sorted) {
      Document metafield = restHelper.getProductMetafield(variant.getLong(Constants.Product_Id), Constants.Inventory, Constants.Vendor);
      if (metafield != null) {
        String value = metafield.getString("value");
        variant.append("debug", product.get("debug"));
        variant.append("debug-id", product.getLong(Constants.Id));
        Document values = Document.parse(value);
        variant.put("vendor", values.get("vendor"));
        register(variant);
      }
    }
    // Now set prices/invqty
    for (Document variant : sorted) {
      if (!variant.containsKey("vendor")) {
        continue;
      }
      String status = null;
      String currentStatus = variant.getInteger(Constants.Inventory_Quantity) > 0 ? Constants.In_Stock : Constants.Out_Of_Stock;
      Double price = null;
      Double currentPrice = Double.valueOf(variant.getString(Constants.Price));
      Utilities.waitForStatus(variant);
      if (variant.getString(Constants.Status).equals(Constants.In_Stock)) {
        status = Constants.In_Stock;
        if (price == null || variant.getDouble(Constants.Final_Price).doubleValue() < price) {
          price = variant.getDouble(Constants.Final_Price);
        }
      } else if (status == null) {
        status = Constants.Out_Of_Stock;
      }
      updateVariant(status, currentStatus, price, currentPrice, variant);
      mongodb.updateVariantIP(variant, message);
      log.info(String.format("Variant %s, %s ", variant.getLong(Constants.Product_Id), message.toString()));
    }
  }
  private final static int MAX_PURCHASE = 11500;
  private StringBuilder message = new StringBuilder();

  public void register(long id) {
    register(id, false);
  }

  public void register(long id, boolean debug) {
    log.info("Register product " + id);
    Document result = restHelper.getProduct(id);
    Document product = (Document) result.get(Constants.Product);
    product.append("debug", debug);
    registerProduct(product);
  }

  public void register(Document variant) {
    if (variant != null) {
      add(variant);
    }
  }

  private boolean accessing;

  private void add(Document variant) {
    System.out.println("Adding variant");
    synchronized (this) {
      while (accessing) {
        try {
          this.wait(150);
        } catch (InterruptedException ex) {
          Logger.getLogger(RequestsHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
      REQUESTS.add(variant);
      System.out.println("Variant added");
      accessing = true;
      this.notifyAll();
    }
  }

  public void run() {
    Document variant = null;
    Map<String, String> urls = new LinkedHashMap<>();
    while (true) {
      synchronized (this) {
        while (!accessing) {
          try {
            System.out.println("thread waiting " + accessing);
            this.wait();
            System.out.println("thread waiting done " + accessing);
          } catch (InterruptedException ex) {
            Logger.getLogger(RequestsHandler.class.getName()).log(Level.SEVERE, null, ex);
          }
        }
        if (!REQUESTS.isEmpty()) {
          System.out.println("Retrieve variant ");
          variant = REQUESTS.remove(0);
        }
        accessing = false;
        this.notifyAll();
      }
      if (variant != null) {
        documentProcessor.accept(urls, variant);
      }
      if (urls.size() > 30) {
        urls.clear();
      }
    }
  }

  public void updateVariant(String status, String currentStatus, Double price, double currentPrice,
    Document variant) {
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
        if (isPriceOkToChange(variant, price, currentPrice)) {
          change.put(Constants.Price, price.toString());
        }
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
      restHelper.updateVariant(variant.getLong(Constants.Id), pack.toJson());
    } else {
      System.out.println(message.toString());
    }
  }
  private List<String> messages = new ArrayList<>();

  public boolean isPriceOkToChange(Document variant, Double price, Double currentPrice) {
    boolean retval = false;
    if (price.doubleValue() >= currentPrice.doubleValue()) {
      retval = true;
    } else {
      // Price decrease
      double ratio = (currentPrice - price) / currentPrice;
      if (ratio > 0.20) {
        messages.add(String.format("Price change > %s, product %s %f to %f<br>", "20%",
          variant.getString(Constants.Sku), currentPrice, price));
        notifyMessages();
      } else {
        retval = true;
      }
    }
    return retval;
  }

  

  private long lastSent = 0;

  private void notifyMessages() {
    if (messages.size() == 0 || lastSent > System.currentTimeMillis()) {
      return;
    }
    lastSent = System.currentTimeMillis() + (15 * 60 * 1000);
    try {
      StringBuilder mess = new StringBuilder();
      messages.stream().forEach(mess::append);
      messages.clear();
      SendMail sendEmail = new SendMail("ricardo.teves@gotkcups.com", "ricardo.teves@gotkcups.com",
        "ricardo.teves@gotkcups.com", "UpdatesProducts Notification", mess.toString());
      sendEmail.send();
    } catch (Exception ex1) {
      Logger.getLogger(CheckHello.class.getName()).log(Level.SEVERE, null, ex1);
    }
  }
}
