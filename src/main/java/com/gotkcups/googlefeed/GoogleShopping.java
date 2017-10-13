/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.googlefeed;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.content.ShoppingContent;
import com.google.api.services.content.model.Product;
import com.google.api.services.content.model.ProductsListResponse;
import com.google.api.services.sheets.v4.Sheets;
import com.gotkcups.data.Constants;
import com.gotkcups.io.GateWay;
import com.gotkcups.io.Utilities;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.bson.Document;

/**
 *
 * @author rfteves
 */
public class GoogleShopping {

  private static final String APPLICATION_NAME
    = "Google Product Ads";

  /**
   * Directory to store user credentials for this application.
   */
  private static final java.io.File DATA_STORE_DIR = new java.io.File(
    System.getProperty("user.home"), ".credentials/sheets.googleapis.com-java-google-ads-products");

  /**
   * Global instance of the JSON factory.
   */
  private static final JsonFactory JSON_FACTORY
    = JacksonFactory.getDefaultInstance();

  /**
   * Global instance of the HTTP transport.
   */
  private static HttpTransport HTTP_TRANSPORT;

  /**
   * Global instance of the {@link FileDataStoreFactory}.
   */
  private static FileDataStoreFactory DATA_STORE_FACTORY;

  static {
    try {
      HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
      DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }
  /* If modifying these scopes, delete your previously saved credentials
     * at ~/.credentials/sheets.googleapis.com-java-quickstart
   */

  private static final List<String> SCOPES = Arrays.asList(
    "https://www.googleapis.com/auth/userinfo.profile",
    "https://www.googleapis.com/auth/userinfo.email",
    "https://www.googleapis.com/auth/content");

  /**
   * Creates an authorized Credential object.
   *
   * @return an authorized Credential object.
   * @throws IOException
   */
  public static Credential authorize() throws IOException {
    // Load client secrets.
    InputStream in
      = GoogleShopping.class.getResourceAsStream("/client_secret.com.json");
    GoogleClientSecrets clientSecrets
      = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

    // Build flow and trigger user authorization request.
    GoogleAuthorizationCodeFlow flow
      = new GoogleAuthorizationCodeFlow.Builder(
        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
        .setDataStoreFactory(DATA_STORE_FACTORY)
        .setAccessType("offline")
        .build();
    Credential credential = new AuthorizationCodeInstalledApp(
      flow, new LocalServerReceiver()).authorize("user");
    System.out.println(
      "Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
    return credential;
  }

  public static ShoppingContent getShoppingContentService() throws IOException {
    Credential credential = authorize();
    ShoppingContent shopping = new ShoppingContent.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();
    return shopping;

  }

  public static Sheets getSheetsService() throws IOException {
    Credential credential = authorize();
    return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
      .setApplicationName(APPLICATION_NAME)
      .build();
  }

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) throws Exception {
    

    /*Map<String, String> params = new HashMap<>();
    params.put(Constants.Collection_Id, Constants.GoogleProductAds_CollectionId.toString());
    Document resp = GateWay.getAllCollects(Constants.Production, params, 10, 10);
    List<Document> collects = (List) resp.get(Constants.Collects);
    List<List<Object>> ads = new ArrayList<>();
    for (Document collect : collects) {
      
      
      String result = GateWay.getProductMetafields(Constants.Production, collect.getLong(Constants.Product_Id));
      List<Document> metafields = (List) ((Document) Document.parse(result)).get(Constants.Metafields);
      result = GateWay.getProduct(Constants.Production, collect.getLong(Constants.Product_Id));
      Document product = (Document) ((Document) Document.parse(result)).get(Constants.Product);
      Document ad = new Document();
      metafields.stream().filter(meta -> meta.getString(Constants.Key).equals(Constants.Gmc_Id)).forEach(meta -> {
        ad.append(Constants.Id, meta.getString(Constants.Value));
      });
      metafields.stream().filter(meta -> meta.getString(Constants.Key).equals(Constants.Title_Tag)).forEach(meta -> {
        ad.append(Constants.Title, meta.getString(Constants.Value));
      });
      if (!ad.containsKey(Constants.Title)) {
        ad.append(Constants.Title, product.getString(Constants.Title));
      }

      int q = 0;
    }*/
    
    
    
    
    
    
    
    
    
    
    
    
    ShoppingContent shopping = GoogleShopping.getShoppingContentService();
    Product prod = shopping.products().get(BigInteger.valueOf(Long.parseLong(Utilities.getApplicationProperty(Constants.Google_Merchant_Id))), "online:en:US:shopify_US_6931144007_21985025479").execute();
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
        if (!pr.toString().contains("9798266122"))continue;
        //prr.remove("taxes");
        System.out.println(pr.getId() + " : " + pr.size());
        Document ddd = new GKProduct(pr.getOfferId()).buildProduct();
        
        int hhh=0;
        Map<String,Object>sort = new TreeMap<>();
        sort.putAll(ddd);
        ddd.clear();
        ddd.putAll(sort);
        String json = ddd.toJson()
          .replaceAll("\" : \"", "\":\"")
          .replaceAll("\", \"", "\",\"")
          .replaceAll("\" : ", "\":")
          .replaceAll(", \"", ",\"")
          .replaceAll("\\{ ", "\\{")
          .replaceAll(" \\}", "\\}")
          .replaceAll("[ ]{2,}", " ");
        String prrJson = prr.toJson()
          .replaceAll("\" : \"", "\":\"")
          .replaceAll("\", \"", "\",\"")
          .replaceAll("\" : ", "\":")
          .replaceAll(", \"", ",\"")
          .replaceAll("\\{ ", "\\{")
          .replaceAll(" \\}", "\\}");
        if (!prrJson.toLowerCase().equals(json.toLowerCase())) {
          int kkk=0;
          System.out.println(prrJson);
          System.out.println(json);
          int kkkkk=6;
        }
        sizes.addAll(pr.keySet());
      }
      if (page.getNextPageToken() == null) {
        break;
      }

      productsList.setPageToken(page.getNextPageToken());
    } while (true);

    for (String key : sizes) {
      System.out.println("xxx: " + key);
    }

  }
}
