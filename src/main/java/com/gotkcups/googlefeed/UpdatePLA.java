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
import com.gotkcups.data.Constants;
import com.gotkcups.data.RequestsHandler;
import com.gotkcups.io.GateWay;
import com.gotkcups.io.RestHelper;
import com.gotkcups.io.Utilities;
import java.io.IOException;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author rfteves
 */
@Component
public class UpdatePLA extends Task {

  private AtomicLong counter = new AtomicLong();

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

  @Override
  public void process(String... args) throws Exception {
    List<Document>filteredProducts = this.getFilteredProducts();
    Map<Long, Product> products = new LinkedHashMap<>();
    long startIndex = counter.incrementAndGet();
    for (Document product : filteredProducts) {
      List<Document> variants = (List) product.get(Constants.Variants);
      variants.stream().forEach(var -> {
        Product productEntry = gkp.setProductVariant(product, variants, var.getLong(Constants.Id)).buildProduct();
        products.put(counter.incrementAndGet(), productEntry);
        System.out.println(productEntry.getOfferId());
        if (productEntry.getOfferId().equals("shopify_US_10769327626_41991345354")) {
          int u=0;
        }
      });
    }
    long endIndex = counter.incrementAndGet();
    this.processProducts(products, startIndex, endIndex, 50);
    //this.processProducts(products, 1, 102, 50);
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
        System.out.println(productEntry.toString());
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
  
  private List<Document> getFilteredProducts() throws IOException, ParseException {
    Calendar today = Calendar.getInstance();
    Map<String, String> params = new HashMap<>();
    params.put(Constants.Collection_Id, Constants.GoogleProductAds_CollectionId.toString());
    Document resp = restHelper.getAllCollects(params, 120, -1);
    List<Document> collects = (List) resp.get(Constants.Collects);
    long updated_at = today.getTimeInMillis() - (10 * ONE_HOUR);
    Set<Long>validIds = new HashSet<>();
    for (Document collect : collects) {
      validIds.add(collect.getLong(Constants.Product_Id));
    }
    params.clear();
    //params.put("fields", "id,title,variants,updated_at");
    resp = GateWay.getAllProducts("prod", params, 150, -1);
    List<Document>products = (List) resp.get("products");
    List<Document>filtered = new ArrayList<>();
    long debugProduct = 0;//11865486474L;
    for (Document product : products) {
      Date updated = Utilities.parseDate(product.getString("updated_at"));
      if(product.getLong(Constants.Id) == debugProduct) {
        filtered.add(product);
        break;
      } else if(debugProduct == 0L && validIds.contains(product.getLong(Constants.Id))
        && updated.getTime() >= updated_at) {
        filtered.add(product);
      }
    }
    return filtered;
  }
}
