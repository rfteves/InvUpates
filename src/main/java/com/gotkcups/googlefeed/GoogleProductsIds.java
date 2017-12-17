/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.googlefeed;

import com.gotkcups.data.Constants;
import com.gotkcups.io.RestHelper;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author rfteves
 */
@Service
public class GoogleProductsIds {
  
  @Autowired
  protected RestHelper restHelper;
  public Set<Long> getValidIds() throws IOException {
    Map<String, String> params = new HashMap<>();
    params.put(Constants.Collection_Id, Constants.GoogleProductAds_CollectionId.toString());
    Document googleProducts = restHelper.getAllCollects(params, 120, -1);
    List<Document> googleProductsCollects = (List) googleProducts.get(Constants.Collects);
    Set<Long>validIds = new HashSet<>();
    for (Document collect : googleProductsCollects) {
      validIds.add(collect.getLong(Constants.Product_Id));
    }
    return validIds;
  }
}
