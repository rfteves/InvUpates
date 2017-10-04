/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.adhoc;

import com.gotkcups.data.Constants;
import com.gotkcups.data.RequestsHandler;
import static com.gotkcups.data.RequestsHandler.register;
import com.gotkcups.io.GateWay;
import com.gotkcups.io.Utilities;
import com.gotkcups.sendmail.SendMail;
import com.gotkcups.tools.CheckHello;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.Document;

/**
 *
 * @author ricardo
 */
public class UpdateProducts {

  private final static Log log = LogFactory.getLog(UpdateProducts.class);

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) throws Exception {
    log.info("am here");
    loopProducts();
  }

  private static void loopProducts() throws Exception {
    int limit = 0;
    Map<String, String> params = new HashMap<>();
    params.put("fields", "id,title,variants");
    Set<Document> sorted = new TreeSet<>();
    Document resp = GateWay.getAllProducts("prod", params, 150, -1);
    List<Document> products = (List) resp.get("products");
    for (Document product : products) {
      if (!(product.getLong("id") == 83063144471L
        || product.getLong("id") == 9760556810993399l
        || product.getLong("id") == 933507564170339999l)) {
        //continue;
      }
      RequestsHandler.register(product.getLong(Constants.Id));
      RearrangeVariants.process(product);
    }
    System.exit(0);
  }
  private static StringBuilder message = new StringBuilder();

  
}
