/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.adhoc;

import com.gotkcups.data.Constants;
import com.gotkcups.data.JDocument;
import com.gotkcups.data.QDocument;
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
public class RearrangeVariants {
  public static void main(String[] args) throws Exception {
        loopProducts();
    }

    private static void loopProducts() throws Exception {
        int limit = 0;
        Map<String, String> params = new HashMap<>();
        params.put("fields", "id,title,variants");
        Set<Document> sorted = new TreeSet<>();
        Document resp = GateWay.getAllProducts("prod", params, 50, 1);
        List<QDocument> products = (List) resp.get("products");
        for (Document product : products) {
            List<Document> variants = (List) product.get("variants");
            sorted.clear();
            for (Document variant : variants) {
                if (!(variant.getLong("product_id") == 9760583434L
                  || variant.getLong("product_id") == 93350756417033l)) {
                    continue;
                }
                if (variant.getLong(Constants.Id) != 11838139146l) {
                    //continue;
                }
                if (!variant.getString(Constants.Sku).toLowerCase().endsWith("k")) {
                    //continue;
                }
                if (limit++ > 200) {
                    //break;
                }
                Document d = new QDocument();
                d.putAll(variant);
                sorted.add(d);
            }
            int position = 0;
            boolean repositioned = false;
            for (Document variant: sorted) {
              if (++position != variant.getInteger("position")) {
                repositioned = true;
                variant.put("position", position);
              }
              System.out.println("variant 3: " + variant.toJson());
            }
            if (repositioned) {
              variants.clear();
              sorted.stream().forEach(variants::add);
              System.out.println(product.toJson());
              Document p = new Document();
              p.put("product", product);
              GateWay.updateProduct("prod", product.getLong("id"), p.toJson());
            }
        }
    }
}
