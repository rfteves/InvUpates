/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.googlefeed;

import com.gotkcups.configs.MainConfiguration;
import com.gotkcups.data.Constants;
import com.gotkcups.data.RequestsHandler;
import static com.gotkcups.googlefeed.UpdatePLA.ONE_DAY;
import com.gotkcups.io.RestHelper;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 *
 * @author rfteves
 */
@Component
@Profile("rollcall")
public class ProductRollCall implements CommandLineRunner {
  @Autowired
  protected RestHelper restHelper;
  
  @Autowired
  private MainConfiguration config;
  
  @Autowired
  private RequestsHandler requestsHandler;
  
  @Override
  public void run(String... strings) throws Exception {
    Calendar today = Calendar.getInstance();
    long updated_at = today.getTimeInMillis() - (720 * ONE_DAY);
    List<Document> filteredProducts = this.getFilteredProducts(updated_at);
    boolean marked = false;
    for (Document product: filteredProducts) {
      if (true || product.getLong(Constants.Id).longValue() == 10128655114L) {
        marked = true;
      }
      if (!marked)continue;
      List<Document> variants = (List) product.get(Constants.Variants);
      if (false || variants.get(0).getString(Constants.Sku).toUpperCase().endsWith("C")) {
        requestsHandler.register(product.getLong(Constants.Id));
      }
      marked = false;
    }
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
    params.clear();
    googleProducts = restHelper.getAllProducts(params, 150, -1);
    List<Document> products = (List) googleProducts.get("products");
    List<Document> filtered = new ArrayList<>();
    //long debugProduct = 9588638218L;
    //long debugProduct = 6931144775L;
    long debugProduct = 0L;
    for (Document product : products) {
      String date = product.getString("updated_at");
      String modified = date.substring(0, date.lastIndexOf(":")) + date.substring(date.lastIndexOf(":") + 1);
      SimpleDateFormat sdf = config.getDateConverter();
      Date updated = sdf.parse(modified);
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
