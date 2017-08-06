/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.tests;

import static com.gotkcups.data.RequestsHandler.register;
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
public class TestRequestsHandler {

  public static void main(String[] args) throws Exception {
    loopProducts();
    //manualProduct();
  }

  private static void manualProduct() {
    String s = "{ \"vendors\" : [{ \"variantid\" : { \"$numberLong\" : \"34766442442\" }, \"productid\" : { \"$numberLong\" : \"9668125130\" }, \"taxable\" : false, \"sku\" : \"1071072C\", \"url\" : \"https://www.costco.com/.product.100292996.html\", \"pageid\" : \"1071072C\" }, { \"variantid\" : { \"$numberLong\" : \"34766442442\" }, \"productid\" : { \"$numberLong\" : \"9668125130\" }, \"taxable\" : false, \"sku\" : \"1071072C\", \"url\" : \"http://www.keurig.com/Beverages/Coffee/Caff%C3%A9-Verona%C2%AE-Coffee/p/Caffe-Verona-Coffee-Starbucks\", \"defaultminqty\" : 6, \"pageid\" : \"5000054318K\" }, { \"variantid\" : { \"$numberLong\" : \"34766442442\" }, \"productid\" : { \"$numberLong\" : \"9668125130\" }, \"taxable\" : false, \"sku\" : \"1071072C\", \"url\" : \"http://www.keurig.com/Beverages/Coffee/Caff%C3%A9-Verona%C2%AE-Coffee/p/Caffe-Verona-Coffee-Starbucks\", \"defaultminqty\" : 9, \"pageid\" : \"5000054255K\" }] }";
    Document vendors = Document.parse(s);
    register(vendors);
  }

  private static void loopProducts() throws Exception {
    int limit = 0;
    Map<String, String> params = new HashMap<>();
    params.put("fields", "id,variants");
    Document resp = Utilities.getAllProducts("prod", params, 50, -1);
    List<Document> products = (List) resp.get("products");
    for (Document product : products) {
      List<Document> variants = (List) product.get("variants");
      for (Document variant : variants) {
        if (variant.getLong("product_id") != 7035661383l) {
          //continue;
        }
        if (!variant.getString("sku").toLowerCase().endsWith("b")) {
          continue;
        }
        if (limit++ > 200)break;
        String meta = RestHttpClient.getVariantMetaField("prod", product.getLong("id"), variant.getLong("id"));
        Document metas = Document.parse(meta);
        List<Document> metafields = (List) metas.get("metafields");
        for (Document metafield : metafields) {
          if (metafield.getString("namespace").equals("inventory") && metafield.getString("key").equals("vendor")) {
            String value = metafield.getString("value");
            Document values = Document.parse(value);
            register(values);
          }
        }
      }
    }
  }
}
