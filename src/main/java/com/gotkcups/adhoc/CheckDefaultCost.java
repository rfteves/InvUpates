/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.adhoc;

import com.gotkcups.data.Constants;
import com.gotkcups.data.JDocument;
import com.gotkcups.io.RestHelper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
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
@Profile("check")
public class CheckDefaultCost implements CommandLineRunner {

  @Autowired
  protected RestHelper restHelper;
  
  @Override
  public void run(String... strings) throws Exception {
    Map<String, String> params = new HashMap<>();
    params.put("fields", "id,title,variants");
    Set<Document> sorted = new TreeSet<>();
    Document resp = restHelper.getAllProducts(params, 150, -1);
    List<Document> products = (List) resp.get("products");
    for (Document product : products) {
      if (!(product.getLong("id") == 59082047511L
        || product.getLong("id") == 93350756417033l)) {
        //continue;
      }
      List<Document> variants = (List) product.get("variants");
      for (Document variant : variants) {
        if (variant.getLong(Constants.Id) != 11838139146l) {
          //continue;
        }
        if (!variant.getString(Constants.Sku).toLowerCase().endsWith("k")) {
          //continue;
        }
        Document d = new JDocument();
        variant.put("product_title", product.getString("title"));
        d.putAll(variant);
        sorted.add(d);
      }
    }
    for (Document variant : sorted) {
      System.out.println("Querying " + variant.get("product_title"));
      Document metafield = restHelper.getProductMetafield(variant.getLong(Constants.Product_Id), Constants.Inventory, Constants.Vendor);
      if (metafield != null) {
        String value = metafield.getString("value");
        Document values = Document.parse(value);
        Document vendor = (Document)((List)values.get("vendor")).get(0);
        if (vendor.containsKey(Constants.Default_Cost)) {
          System.out.println(variant.getString("product_title") + " " +
            variant.getLong(Constants.Product_Id) + " " + vendor.getDouble(Constants.Default_Cost));
        }
      }
    }
  }
  
}
