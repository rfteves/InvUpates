/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.adhoc;

import com.gotkcups.configs.MainConfiguration;
import com.gotkcups.data.Constants;
import com.gotkcups.data.RequestsHandler;
import com.gotkcups.io.RestHelper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 *
 * @author ricardo
 */
@Component
@Profile("updateproducts")
public class UpdateProducts extends AbstractCLR {

  private final static Log log = LogFactory.getLog(UpdateProducts.class);

  /**
   * @param args the command line arguments
   */

  @Autowired
  protected RestHelper restHelper;
  
  @Autowired
  private MainConfiguration config;
  
  @Autowired
  private RequestsHandler requestsHandler;
  
  //@Override
  public void process (String... args) throws Exception {
    int limit = 0;
    Map<String, String> params = new HashMap<>();
    params.put("fields", "id,title,variants");
    Set<Document> sorted = new TreeSet<>();
    Document resp = restHelper.getAllProducts(params, 150, -1);
    List<Document> products = (List) resp.get("products");
    for (Document product : products) {
      if (!(product.getLong("id") == 9541792586L
        || product.getLong("id") == 9760556810993399l
        || product.getLong("id") == 933507564170339999l)) {
        continue;
      }
      requestsHandler.register(product.getLong(Constants.Id));
      //RearrangeVariants.process(product);
    }
    //System.exit(0);
  }
  private static StringBuilder message = new StringBuilder();
  public static void main(String[] args) throws Exception {
    new UpdateProducts().process(args);
  }
}
