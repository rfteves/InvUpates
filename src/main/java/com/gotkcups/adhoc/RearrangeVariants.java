/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.adhoc;

import com.gotkcups.io.GateWay;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.Document;

/**
 *
 * @author Ricardo
 */
public class RearrangeVariants {
  private final static Log log = LogFactory.getLog(RearrangeVariants.class);
  public static void process(Document product) {
    List<Document> variants = (List) product.get("variants");
    if (variants.size() == 1)return;
    boolean repositioned = false;
    int seen = 0;
    while (seen < variants.size()) {
      Document prev = null;
      seen = 0;
      for (Document variant : variants) {
        if (prev == null) {
          prev = variant;
        } else if (variant.getInteger("inventory_quantity") > 0
          && prev.getInteger("inventory_quantity") == 0) {
          variants.remove(prev);
          variants.add(prev);
          repositioned = true;
          break;
        } else if (variant.getInteger("inventory_quantity") > 0 &&
          prev.getInteger("inventory_quantity") > 0 &&
          Double.parseDouble(variant.getString("price")) >
          Double.parseDouble(prev.getString("price"))) {
          variants.remove(prev);
          variants.add(prev);
          repositioned = true;
          break;
        } else {
          prev = variant;
        }
        ++seen;
      }
    }
    if (repositioned) {
      int position = 1;
      for (Document variant : variants) {
        variant.put("position", position++);
      }
      Document p = new Document();
      p.put("product", product);
      GateWay.updateProduct("prod", product.getLong("id"), p.toJson());
    }
  }
}
