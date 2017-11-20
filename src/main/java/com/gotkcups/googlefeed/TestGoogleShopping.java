/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.googlefeed;

import com.google.api.services.content.ShoppingContent;
import com.google.api.services.content.model.Product;
import com.google.api.services.content.model.ProductsListResponse;
import com.gotkcups.data.Constants;
import com.gotkcups.io.Utilities;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.bson.Document;

/**
 *
 * @author rfteves
 */
public class TestGoogleShopping {

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) throws Exception {
    
    ShoppingContent shopping = new GoogleShopping().getShoppingContentService();
    ShoppingContent.Products.List productsList = (ShoppingContent.Products.List) shopping.products().list(BigInteger.valueOf(Long.parseLong(Utilities.getApplicationProperty(Constants.Google_Merchant_Id))));
    Set<String> sizes = new LinkedHashSet<>();
    Map<String, Object> holly = new LinkedHashMap<>();
    do {
      ProductsListResponse page = productsList.execute();
      if (page.getResources() == null) {
        System.out.println("No products found.");
        break;
      }
      for (Product pr : page.getResources()) {
        Document prr = new Document();
        prr.putAll(pr);
        if (!pr.toString().contains("9797752650")) {
          continue;
        }
        //System.out.println(prr.toJson());
        System.out.println(pr.toString());
      }
      if (page.getNextPageToken() == null) {
        break;
      }

      productsList.setPageToken(page.getNextPageToken());
    } while (true);
  }
  
}
