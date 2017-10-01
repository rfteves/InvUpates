/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.adhoc;

import com.gotkcups.data.Constants;
import com.gotkcups.data.JDocument;
import com.gotkcups.io.GateWay;
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
public class IsolateBjsMetafield {

  /**
   * @param args the command line arguments
   */
    public static void main(String[] args) throws Exception {
    doit();
  }
  
  private static void doit() throws Exception {
    Map<String, String> params = new HashMap<>();
    params.put("fields", "id,title,variants");
    Set<Document> sorted = new TreeSet<>();
    Document resp = GateWay.getAllProducts("prod", params, 50, -1);
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
      Document metafield = GateWay.getMetafield("prod", variant, Constants.Inventory, Constants.Vendor);
      if (metafield != null) {
        String value = metafield.getString("value");
        Document values = Document.parse(value);
        Document vendor = (Document)((List)values.get("vendors")).get(0);
        if (vendor.containsKey("bjs.com")) {
          System.out.println(variant.getString("product_title") + " " +
            variant.getLong(Constants.Product_Id) + " " + variant.getLong(Constants.Id));
        }
      }
    }
  }
  
}
