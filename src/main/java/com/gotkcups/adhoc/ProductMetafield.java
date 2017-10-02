/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.adhoc;

import com.gotkcups.data.Constants;
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
public class ProductMetafield {

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) throws Exception {
    Map<String, String> params = new HashMap<>();
    params.put("fields", "id,title,variants");
    Document resp = GateWay.getAllProducts("prod", params, 150, -1);
    List<Document> products = (List) resp.get("products");
    for (Document product : products) {
      List<Document> variants = (List) product.get("variants");
      for (Document variant : variants) {
        Document metafield = GateWay.getProductMetafield("prod", variant.getLong(Constants.Product_Id), Constants.Inventory, Constants.Vendor);
        if (metafield != null) {
          Document meta = new Document();
          meta.append("namespace", "inventory");
          meta.append("key", "vendor");
          meta.append("value_type", "string");
          Document value = new Document();
          String s = metafield.getString("value");
          Document vendors = Document.parse(s);
          List<Document>list = (List)vendors.get("vendor");
          Document vendor = list.get(0);
          vendor.remove(Constants.Sku);
          vendor.remove(Constants.Pageid);
          if (vendor.containsKey(Constants.DefaultShipping) && !vendor.getString(Constants.URL).contains("samsclub.com")) {
            vendor.put("extra-cost", vendor.get(Constants.DefaultShipping));
            vendor.remove(Constants.DefaultShipping);
          }
          value.append("vendor", vendor);
          meta.append("value", value.toJson());
          Document field = new Document();
          field.append("metafield", meta);
          System.out.println(field.toJson());
          GateWay.createProductMetaField(Constants.Production, variant.getLong(Constants.Product_Id), field.toJson());
          //String sss = GateWay.getProductMetafields(Constants.Production, variant.getLong(Constants.Product_Id));
          //Document productMetas = Document.parse(sss);
          break;
        }
      }

    }
  }

}
