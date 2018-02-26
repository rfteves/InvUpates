/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.googlefeed;

import com.gotkcups.data.Constants;
import static com.gotkcups.googlefeed.UpdatePLA.ONE_HOUR;
import com.gotkcups.io.RestHelper;
import com.gotkcups.model.Metafield;
import com.gotkcups.repos.MetafieldJPARepository;
import com.gotkcups.repos.MetafieldRepository;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author rfteves
 */
@Component
public class UpdateMetafields extends Task {

  @Autowired
  protected RestHelper restHelper;
  
  @Autowired
  private MetafieldJPARepository jpa;
  
  @Autowired
  private UpdatePLA plas;

  @Override
  public void process(String... args) throws Exception {
    Calendar today = Calendar.getInstance();
    long updated_at = today.getTimeInMillis() - (8 * ONE_HOUR);
    List<Document> filteredProducts = plas.getFilteredProducts(updated_at);
    Set<Long>updatedProducts = new HashSet<>();
    filteredProducts.stream().forEach(filteredProduct->{
      updatedProducts.add(filteredProduct.getLong(Constants.Id));
    });
    Map<String, String> params = new HashMap<>();
    params.put(Constants.Collection_Id, Constants.GoogleProductAds_CollectionId.toString());
    Document resp = restHelper.getAllCollects(params, 100, -1);
    List<Document> collects = (List) resp.get(Constants.Collects);
    for (Document collect : collects) {
      if (!updatedProducts.contains(collect.getLong(Constants.Product_Id)))continue;
      List<Document> metafields = (List) restHelper.getProductMetafields(collect.getLong(Constants.Product_Id), true).get(Constants.Metafields);
      List<Metafield>metas = new ArrayList<>();
      metafields.stream().forEach(meta->{
        Metafield m = Task.createMetafield(meta);
        metas.add(m);
      });
      jpa.save(metas);
      System.out.println("Processing " + collect.getLong(Constants.Product_Id));
    }
  }
}
