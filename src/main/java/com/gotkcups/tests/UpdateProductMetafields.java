/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.tests;

import com.gotkcups.data.Constants;
import com.gotkcups.io.RestHttpClient;
import com.gotkcups.io.Utilities;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bson.Document;

/**
 *
 * @author rfteves
 */
public class UpdateProductMetafields {

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    Map<String, String> params = new HashMap<>();
    params.put("fields", "id,title,variants");
    String json = RestHttpClient.getProduct(Constants.Production, 10376781898l, params);
    Document products = Document.parse(json);
    Document product = (Document)products.get(Constants.Product);
    List<Document> variants = (List) product.get(Constants.Variants);
    for (Document variant : variants) {
      Document metafield = Utilities.getMetafield("prod", variant, Constants.Inventory, Constants.Vendor);
      String value = metafield.getString("value");
      Document values = Document.parse(value);
      List<Document>vendors = (List)values.get(Constants.Vendors);
      for (Document vendor: vendors) {
        
        int debug = 0;
      }
    }
  }

}
