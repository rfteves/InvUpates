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
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.gotkcups.data.Constants;
import static com.gotkcups.googlefeed.GoogleShopping.authorize;
import com.gotkcups.io.GateWay;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bson.Document;

/**
 *
 * @author Ricardo
 */
public class Springsale {

  private static final String APPLICATION_NAME
    = "GotKcups Promotion";

  /**
   * Directory to store user credentials for this application.
   */
  private static final java.io.File DATA_STORE_DIR = new java.io.File(
    System.getProperty("user.home"), ".credentials/products.googleapis.com-java-promotion");

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
  
      /** Global instance of the scopes required by this quickstart.
     *
     * If modifying these scopes, delete your previously saved credentials
     * at ~/.credentials/sheets.googleapis.com-java-quickstart
     */
    
  private static final List<String> SCOPES = Arrays.asList(
    "https://www.googleapis.com/auth/userinfo.profile",
    "https://www.googleapis.com/auth/userinfo.email",
    "https://www.googleapis.com/auth/content");


  static {
    try {
      HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
      DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }

  /**
   * Creates an authorized Credential object.
   *
   * @return an authorized Credential object.
   * @throws IOException
   */
  public static Credential authorize() throws IOException {
    // Load client secrets.
    InputStream in
      = Springsale.class.getResourceAsStream("/client_secret.com.json");
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

  /**
   * Build and return an authorized Sheets API client service.
   *
   * @return an authorized Sheets API client service
   * @throws IOException
   */
  public static Sheets getSheetsService() throws IOException {
    Credential credential = authorize();
    return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
      .setApplicationName(APPLICATION_NAME)
      .build();
  }
  
  public static ShoppingContent getShoppingContentService() throws IOException {
    Credential credential = authorize();
    ShoppingContent shopping = new ShoppingContent.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();
    return shopping;

  }

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) throws Exception {
    Map<String, String> params = new HashMap<>();
    params.put("collection_id", Constants.KeurigSmallBoxes_CollectionId.toString());
    //Document resp = GateWay.getAllCustomCollection("prod", 6732382231L, params, 150, -1);
    List<String>ids = new ArrayList<>();
    ids.add("SPRING2017");
    Document response = GateWay.getAllCollects(Constants.Production, params, 10, 10);
    List<Document> collects = (List) response.get("collects");
    for (Document collect : collects) {
      int yyy=0;
      //String result = GateWay.createProductMetaField(Constants.Production, collect.getLong(Constants.Product_Id), meta.toJson());
      int q = 0;
    }
    int y = 0;
  }

}
