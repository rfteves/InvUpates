/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.data;

import com.gotkcups.io.RestHelper;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author rfteves
 */
@Service
public class SingleProduct {
  @Autowired
  private RestHelper restHelper;
  
  public Document getProduct(long productId) {
    Document product = restHelper.getProduct(productId);
    return product;
  }
  
  public Document getProductAndMetafields(long productId) {
    Document product = getProduct(productId);
    Document metas = restHelper.getProductMetafields(productId);
    product.append("metafields", metas);
    return product;
  }
  
  public String getUrl(long productId) {
    Document metas = restHelper.getProductMetafields(productId);
    Document vendor = (Document)Document.parse(metas.getString("vendor")).get("vendor");
    return vendor.getString(Constants.URL);
  }
}
