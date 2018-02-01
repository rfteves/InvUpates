/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.googlefeed;

import com.google.api.services.content.ShoppingContent;
import com.google.api.services.content.model.Product;
import com.google.api.services.content.model.ProductsCustomBatchRequest;
import com.google.api.services.content.model.ProductsCustomBatchRequestEntry;
import com.google.api.services.content.model.ProductsCustomBatchResponse;
import com.gotkcups.configs.MainConfiguration;
import com.gotkcups.data.Constants;
import com.gotkcups.io.RestHelper;
import java.io.IOException;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 *
 * @author rfteves
 */
@Component
public class UpdatePLA extends Task {

  public static long ONE_SECOND = 1000;
  public static long ONE_MINUTE = 60 * ONE_SECOND;
  public static long ONE_HOUR = 60 * ONE_MINUTE;
  public static long ONE_DAY = 24 * ONE_HOUR;

  @Autowired
  private GKProduct gkp;
  @Autowired
  private GoogleShopping gs;
  @Autowired
  protected RestHelper restHelper;
  @Value("${google.merchant.id}")
  private BigInteger merchantId;

  @Autowired
  private MainConfiguration config;

  @Override
  public void process(String... args) throws Exception {
    //if (true)return;
    Calendar today = Calendar.getInstance();
    long updated_at = today.getTimeInMillis() - (4 * ONE_DAY);
    List<Document> filteredProducts = this.getFilteredProducts(updated_at);
    Map<Long, Product> products = config.googleModelProductMap();
    long startIndex = config.counter().incrementAndGet();
    for (Document product : filteredProducts) {
      List<Document> variants = (List) product.get(Constants.Variants);
      variants.stream().filter(var
        -> {
        String date = var.getString("updated_at");
        String modified = date.substring(0, date.lastIndexOf(":")) + date.substring(date.lastIndexOf(":") + 1);
        SimpleDateFormat sdf = config.getDateConverter();
        Date updated = new Date();
        try {
          updated = sdf.parse(modified);
        } catch (ParseException ex) {
          Logger.getLogger(UpdatePLA.class.getName()).log(Level.SEVERE, null, ex);
        }
        return var.getInteger("inventory_quantity") > 0 || updated.getTime() >= updated_at;
      }
      ).forEach(var -> {
        Product productEntry = gkp.setProductVariant(product, variants, var.getLong(Constants.Id)).buildProduct();
        products.put(config.counter().incrementAndGet(), productEntry);
        System.out.println(productEntry.getOfferId() + " " + productEntry.getTitle());
      });
    }
    long endIndex = config.counter().incrementAndGet();
    this.processProducts(products, startIndex, endIndex, 50);
  }

  private void processProducts(Map<Long, Product> products, long start, long end, int limit) throws IOException {
    ProductsCustomBatchRequest request = new ProductsCustomBatchRequest();
    List<ProductsCustomBatchRequestEntry> entries = new ArrayList<>();
    products.keySet().stream()
      .filter(key -> key > start && key < end && key <= start + limit)
      .forEach(key -> {
        Product productEntry = products.get(key);
        ProductsCustomBatchRequestEntry entry = new ProductsCustomBatchRequestEntry();
        entry.setMerchantId(merchantId);
        entry.setBatchId(key);
        entry.setProduct(productEntry);
        entry.setProductId(productEntry.getOfferId());
        entry.setMethod("insert");
        entries.add(entry);
        //System.out.println(productEntry.toString());
      });
    if (entries.isEmpty()) {
      return;
    }
    request.setEntries(entries);
    ShoppingContent shopping = gs.getShoppingContentService();
    ShoppingContent.Products.Custombatch customBatch = shopping.products().custombatch(request);
    ProductsCustomBatchResponse response = customBatch.execute();
    response.getEntries().forEach(entry -> {
      System.out.println(entry.getProduct().getTitle() + " " + entry.getProduct().getMultipack() + " " + entry.getErrors());
    });
    if (entries.size() < limit) {
      return;
    }
    this.processProducts(products, start + limit, end, limit);
  }

  private List<Document> getFilteredProducts(long updated_at) throws IOException, ParseException {
    // Google Products
    Map<String, String> params = new HashMap<>();
    params.put(Constants.Collection_Id, Constants.GoogleProductAds_CollectionId.toString());
    Document googleProducts = restHelper.getAllCollects(params, 120, -1);
    List<Document> googleProductsCollects = (List) googleProducts.get(Constants.Collects);
    Set<Long> validIds = new HashSet<>();
    for (Document collect : googleProductsCollects) {
      validIds.add(collect.getLong(Constants.Product_Id));
    }
    // Keurig Small Boxes
    params.put(Constants.Collection_Id, Constants.KeurigSmallBoxes_CollectionId.toString());
    Document keurigSmallProducts = restHelper.getAllCollects(params, 120, -1);
    List<Document> keurigSmallProductsCollects = (List) keurigSmallProducts.get(Constants.Collects);
    Set<Long> smallBoxIds = new HashSet<>();
    for (Document keurigSmallProduct : keurigSmallProductsCollects) {
      smallBoxIds.add(keurigSmallProduct.getLong(Constants.Product_Id));
    }
    params.clear();
    //params.put("fields", "id,title,variants,updated_at");
    googleProducts = restHelper.getAllProducts(params, 150, -1);
    List<Document> products = (List) googleProducts.get("products");
    List<Document> filtered = new ArrayList<>();
    //long debugProduct = 10015252106L;//8199286919L;
    long debugProduct = 0L;
    for (Document product : products) {
      String date = product.getString("updated_at");
      String modified = date.substring(0, date.lastIndexOf(":")) + date.substring(date.lastIndexOf(":") + 1);
      SimpleDateFormat sdf = config.getDateConverter();
      Date updated = sdf.parse(modified);
      if (product.get(Constants.Product_Type).equals("K Cups")) {
        int y = 0;
      }
      if (product.getLong(Constants.Id) == 8199286919L
        || product.getLong(Constants.Id) == 10015252106L) {
        product.append("promotionIds", Arrays.asList("50OFF"));
        filtered.add(product);
      } else if (smallBoxIds.contains(product.getLong(Constants.Id))) {
        product.append("promotionIds", Arrays.asList("STARTSAVING"));
      }
      if (product.getLong(Constants.Id) == debugProduct) {
        filtered.add(product);
        break;
      } else if (debugProduct == 0L && validIds.contains(product.getLong(Constants.Id))
        && updated.getTime() >= updated_at) {
        filtered.add(product);
      }
    }
    return filtered;
  }
}
