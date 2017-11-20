/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.googlefeed;

import com.gotkcups.data.Constants;
import com.gotkcups.io.RestHelper;
import com.gotkcups.model.Metafield;
import com.gotkcups.repos.MetafieldJPARepository;
import com.gotkcups.repos.MetafieldRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

  @Override
  public void process(String... args) throws Exception {
    Map<String, String> params = new HashMap<>();
    params.put(Constants.Collection_Id, Constants.GoogleProductAds_CollectionId.toString());
    Document resp = restHelper.getAllCollects(params, 100, -1);
    List<Document> collects = (List) resp.get(Constants.Collects);
    for (Document collect : collects) {
      String result = restHelper.getProductMetafields(collect.getLong(Constants.Product_Id));
      List<Document> metafields = (List) ((Document) Document.parse(result)).get(Constants.Metafields);
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
