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
import com.google.api.services.content.model.Inventory;
import com.google.api.services.content.model.InventoryCustomBatchRequest;
import com.google.api.services.content.model.InventoryCustomBatchRequestEntry;
import com.google.api.services.content.model.InventoryCustomBatchResponse;
import com.google.api.services.content.model.InventorySetRequest;
import com.google.api.services.content.model.Product;
import com.google.api.services.content.model.ProductsCustomBatchRequest;
import com.google.api.services.content.model.ProductsCustomBatchRequestEntry;
import com.google.api.services.content.model.ProductsCustomBatchResponse;
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
import jersey.repackaged.com.google.common.base.Optional;
import org.bson.Document;

/**
 *
 * @author rfteves
 */
public class GoogleProductAds {

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
      = GoogleProductAds.class.getResourceAsStream("/client_secret.com.json");
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
    ShoppingContent shopping = GoogleProductAds.getShoppingContentService();
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
        prr.remove("taxes");
        System.out.println(pr.getId() + " : " + pr.size());
        Document ddd = getItemId(pr.getOfferId());
        ddd.remove("taxes");
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
        if (!prrJson.toString().toLowerCase().equals(json.toLowerCase())) {
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

    Map<String, String> params = new HashMap<>();
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
    }

  }

  private static Document getItemId(String itemId) {
    String[] ids = itemId.split("_");
    long productId = Long.parseLong(ids[2]);
    long variantId = Long.parseLong(ids[3]);
    String resp = GateWay.getProduct(Constants.Production, productId);
    Document result = Document.parse(resp);
    Document product = (Document) result.get(Constants.Product);
    resp = GateWay.getProductMetafields(Constants.Production, productId);
    Document metas = Document.parse(resp);
    List<Document> variants = (List) product.get(Constants.Variants);
    Document[] doc = new Document[1];
    variants.stream().filter(variant -> variant.getLong(Constants.Id) == variantId).
      forEach(variant -> {
        doc[0] = buildProduct(product, variant, metas);
      });
    Optional<Document> opts = Optional.of(doc[0]);
    return opts.orNull();
  }

  private static Document buildProduct(Document product, Document variant, Document metas) {
    Document retval = new Document();
    retval.append(Constants.OfferId, String.format("shopify_US_%s_%s",
      product.getLong(Constants.Id).toString(),
      variant.getLong(Constants.Id)));
    if (retval.toJson().contains("shopify_US_10135803082_38125854602")) {
      int imagelinks = 0;
    }
    retval.append(Constants.Id, String.format("online:en:US:%s", retval.getString(Constants.OfferId)));
    retval.append(Constants.AdwordsRedirect,
      String.format("https://www.gotkcups.com/products/%s?utm_medium=cpc&utm_source=googlepla&variant=%s",
        product.getString(Constants.Handle),
        variant.getLong(Constants.Id)));
    retval.append(Constants.Link,
      String.format("https://www.gotkcups.com/products/%s?utm_medium=cpc&utm_source=googleshopping&variant=%s",
        product.getString(Constants.Handle),
        variant.getLong(Constants.Id)));
    retval.append(Constants.Availability, variant.getInteger(Constants.Inventory_Quantity) == 0 ? "out of stock":"in stock");
    retval.append(Constants.Brand, product.getString(Constants.Vendor));
    retval.append(Constants.Channel, Constants.Online);
    retval.append(Constants.ContentLanguage, Constants.En);
    retval.append(Constants.GTIN, variant.getString(Constants.Barcode));
    retval.append(Constants.IdentifierExists, true);
    retval.append(Constants.Kind, Constants.Content_Product);
    Document price = new Document("currency", "USD");
    price.append(Constants.Value, variant.getString(Constants.Price));
    retval.append(Constants.Price, price);
    retval.append(Constants.ProductType, product.getString(Constants.Product_Type));
    Document weight = new Document("unit", "g");
    weight.append(Constants.Value, Double.valueOf(variant.getInteger(Constants.Grams)));
    retval.append(Constants.ShippingWeight, weight);
    retval.append(Constants.TargetCountry, Constants.US);

    List<Document> images = (List) product.get(Constants.Images);
    images.stream()
      .filter(image -> image.getInteger(Constants.Position).equals(variant.getInteger(Constants.Position)))
      .forEach(image -> retval.append(Constants.ImageLink, image.getString(Constants.Src)));
    List<String> additionalImageLinks = new ArrayList<>();
    images.stream().forEach(image -> {
      List<Long> variant_ids = (List) image.get(Constants.Variant_Ids);
      if (variant_ids.isEmpty() || variant_ids.contains(variant.getLong(Constants.Id))) {
        if (!image.getString(Constants.Src).equals(retval.getString(Constants.ImageLink))
          && !additionalImageLinks.contains(image.getString(Constants.Src))) {
          additionalImageLinks.add(image.getString(Constants.Src));
        }
      }
    });
    if (!additionalImageLinks.isEmpty()) {
      retval.append(Constants.AdditionalImageLinks, additionalImageLinks);
    }
    List<Document> metafields = (List) metas.get(Constants.Metafields);
    metafields.stream().forEach(meta -> {
      if (meta.getString(Constants.Namespace).equals(Constants.Google)) {
        if (meta.getString(Constants.Key).equalsIgnoreCase(Constants.Adwords_Grouping)) {
          retval.append(Constants.AdwordsGrouping, meta.getString(Constants.Value));
        } else if (meta.getString(Constants.Key).equalsIgnoreCase(Constants.Adwords_Labels)) {
          List<String>labels = new ArrayList<>();
          labels.add(meta.getString(Constants.Value));
          retval.append(Constants.AdwordsLabels, labels);
        } else if (meta.getString(Constants.Key).equalsIgnoreCase(Constants.Condition)) {
          retval.append(Constants.Condition, meta.getString(Constants.Value));
        } else if (meta.getString(Constants.Key).equalsIgnoreCase(Constants.Google_Product_Type)) {
          retval.append(Constants.GoogleProductCategory, meta.getString(Constants.Value));
        } else if (meta.getString(Constants.Key).equalsIgnoreCase(Constants.Age_Group)) {
          retval.append(Constants.AgeGroup, meta.getString(Constants.Value));
        } else if (meta.getString(Constants.Key).equalsIgnoreCase(Constants.Gender)) {
          retval.append(Constants.Gender, meta.getString(Constants.Value));
        } else if (meta.getString(Constants.Key).startsWith("custom_label_")) {
          String key = meta.getString(Constants.Key);
          retval.append("customLabel".concat(key.substring(key.length() - 1)), meta.getString(Constants.Value));
        }
      } else if (meta.getString(Constants.Namespace).equals(Constants.Global)) {
        if (meta.getString(Constants.Key).equalsIgnoreCase(Constants.Description_Tag)) {
          retval.append(Constants.Description, meta.getString(Constants.Value));
        } else if (meta.getString(Constants.Key).equalsIgnoreCase(Constants.Title_Tag)) {
          retval.append(Constants.Title, meta.getString(Constants.Value));
        }
      }
    });
    if (!variant.getString(Constants.Option_1).equals(Constants.Default_Title)) {
      List<Document> options = (List) product.get(Constants.options);
      options.stream().filter(option -> option.getString(Constants.Name)
        .equalsIgnoreCase(Constants.Color)).forEach(option -> retval.append(Constants.Color, variant.getString(Constants.Option_1)));
    }

    //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    List<Document> taxes = new ArrayList<>();
    //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    Document tax = new Document();
    tax.append("country", "US");
    tax.append("rate", 0.0);
    tax.append("taxShip", false);
    taxes.add(tax);
    retval.append("taxes", taxes);
    // Final check
    if (!retval.containsKey(Constants.Description)) {
      retval.append(Constants.Description, product.getString(Constants.Title));
    }
    if (!retval.containsKey(Constants.Title)) {
      retval.append(Constants.Title, product.getString(Constants.Title));
    }
    if (!retval.containsKey(Constants.Condition)) {
      retval.append(Constants.Condition, Constants.New);
    }
    return retval;
  }
}
