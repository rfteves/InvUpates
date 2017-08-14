/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.tests;

import com.gotkcups.data.Constants;
import com.gotkcups.io.GateWay;
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
    String json = GateWay.getProduct(Constants.Production, 10376781898l, params);
    Document products = Document.parse(json);
    Document product = (Document) products.get(Constants.Product);
    List<Document> variants = (List) product.get(Constants.Variants);
    for (Document variant : variants) {
      Document metafield = GateWay.getMetafield("prod", variant, Constants.Inventory, Constants.Vendor);
      metafield.remove("owner_id");
      metafield.remove("created_at");
      metafield.remove("updated_at");
      metafield.remove("owner_resource");
      String value = metafield.getString("value");
      Document values = Document.parse(value);
      List<Document> vendors = (List) values.get(Constants.Vendors);
      // Only one vendor
      vendors.get(0).put(Constants.Default_Shipping, 4d);
      Document m = new Document();
      metafield.put(Constants.Value, values.toJson());
      m.put(Constants.Metafield, metafield);
      String result = GateWay.updateMetafield(Constants.Production, metafield.getLong(Constants.Id), m.toJson());
      System.out.println(metafield.getLong(Constants.Id));
      int debug = 0;
    }
  }

}
