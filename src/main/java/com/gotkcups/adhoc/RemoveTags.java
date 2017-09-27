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
 * @author Ricardo
 */
public class RemoveTags {

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) throws Exception {
    doit();
  }
  
  private static void doit() throws Exception {
    Map<String, String> params = new HashMap<>();
    params.put("fields", "id,title,tags,product_type");
    Document resp = GateWay.getAllProducts("prod", params, 100, -1);
    List<Document> products = (List) resp.get("products");
    Document p = new Document();
    for (Document product : products) {
      if (product.getLong(Constants.Id) != 6931151687l)continue;
      System.out.println(product.getString("title") + ":" + product.getString("product_type") + ":"+ product.getString("tags"));
      product.put("tags", product.getString("product_type"));
      p.clear();
      p.append("product", product);
      GateWay.updateProduct("prod", product.getLong(Constants.Id), p.toJson());
    }
  }
  
}
