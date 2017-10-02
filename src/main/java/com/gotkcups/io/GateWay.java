/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.io;

import com.gotkcups.data.Constants;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.bson.Document;

/**
 *
 * @author rfteves
 */
public abstract class GateWay {

  public static Document getAllProducts(String env, Map<String, String> params) throws IOException {
    return getAllProducts(env, params, 50, -1);
  }
  
  public static void init() {
    
  }

  public static Document getAllProducts(String env, Map<String, String> params, int pageLimit, int bookLimit) throws IOException {
    final Document[] object = new Document[1];
    Document next = null;
    int page = 0;
    params.put("limit", "" + pageLimit);
    int pageMax = pageLimit;
    while (pageLimit == pageMax) {
      params.put("page", ++page + "");
      String take = GateWay.getProducts(env, params);
      Document tempdoc = Document.parse(take);
      if (object[0] == null) {
        object[0] = tempdoc;
      } else {
        next = tempdoc;
        List<Document> docs = (List) next.get("products");
        docs.stream().forEach(((List) object[0].get("products"))::add);
      }
      pageLimit = ((List) tempdoc.get("products")).size();
      if (bookLimit < 0) {
        continue;
      } else if (((List) object[0].get("products")).size() >= bookLimit) {
        break;
      }
      //if (true)break;
    }
    return object[0];
  }

  public static String getProductVariant(String env, String variant_id) {
    StringBuilder sb = new StringBuilder(Utilities.getApplicationProperty(env));
    sb.append(String.format("/admin/variants/%s.json", variant_id));
    return RestHttpClient.processGet(sb.toString());
  }

  public static String getCollects(String env) {
    StringBuilder sb = new StringBuilder(Utilities.getApplicationProperty(env));
    sb.append("/admin/collects.json");
    return RestHttpClient.processGet(sb.toString());
  }

  public static String getProducts(String env, Map<String, String> params) {
    StringBuilder url = new StringBuilder(Utilities.getApplicationProperty(env));
    if (params != null && params.containsKey("id")) {
      url.append(String.format("/admin/products/%s.json", params.remove("id").toString()));
    } else {
      url.append(String.format("/admin/products.json"));
    }
    GateWay.processParams(url, params);
    return RestHttpClient.processGet(url.toString());
  }

  private static void processParams(StringBuilder url, Map<String, String> params) {
    if (params != null && params.size() > 0) {
      url.append("?");
      boolean andit = false;
      for (String key : params.keySet()) {
        if (andit) {
          url.append("&");
        }
        andit = true;
        url.append(key);
        url.append("=");
        url.append(params.get(key));
      }
    }
  }
  
  public static String getCustomersCount(String env) {
    StringBuilder url = new StringBuilder(Utilities.getApplicationProperty(env));
    url.append("/admin/customers/count.json");
    return RestHttpClient.processGet(url.toString());
  }

  public static String createVariantMetaField(String env, long productId, long variantId, String jsondata) {
    StringBuilder url = new StringBuilder(Utilities.getApplicationProperty(env));
    url.append(String.format("/admin/products/%s/variants/%s/metafields.json", productId, variantId));
    return RestHttpClient.processPost(url.toString(), jsondata);
  }

  public static String createProductMetaField(String env, long productId, String jsondata) {
    StringBuilder url = new StringBuilder(Utilities.getApplicationProperty(env));
    url.append(String.format("/admin/products/%s/metafields.json", productId));
    return RestHttpClient.processPost(url.toString(), jsondata);
  }

  public static String getVariantMetaField(String env, long productId, long variantId) {
    StringBuilder url = new StringBuilder(Utilities.getApplicationProperty(env));
    url.append(String.format("/admin/products/%s/variants/%s/metafields.json", productId, variantId));
    return RestHttpClient.processGet(url.toString());
  }

  public static String getOrder(String env, long orderId) {
    StringBuilder url = new StringBuilder(Utilities.getApplicationProperty(env));
    url.append(String.format("/admin/orders/%s.json", orderId));
    return RestHttpClient.processGet(url.toString());
  }

  public static String createProduct(String env, String json) {
    StringBuilder url = new StringBuilder(Utilities.getApplicationProperty(env));
    url.append(String.format("/admin/products.json", ""));
    return RestHttpClient.processPost(url.toString(), json);
  }

  public static String createProductVariant(String env, long productId, String jsondata) {
    StringBuilder url = new StringBuilder(Utilities.getApplicationProperty(env));
    url.append(String.format("/admin/products/%s/variants.json", productId));
    return RestHttpClient.processPost(url.toString(), jsondata);
  }

  public static void deleteMetaField(String env, long productId, long metaid) {
    StringBuilder url = new StringBuilder(Utilities.getApplicationProperty(env));
    url.append(String.format("/admin/products/%d/metafields/%d.json", productId, metaid));
    RestHttpClient.processDelete(url.toString());
  }

  public static String getCollects(String env, int limit, int page, Map<String, String> params) {
    StringBuilder sb = new StringBuilder(Utilities.getApplicationProperty(env));
    StringBuilder url = new StringBuilder(String.format("/admin/custom_collections.json?limit=%d&page=%d&", limit, page));
    GateWay.processParams(url, params);
    sb.append(url);
    return RestHttpClient.processGet(sb.toString());
  }

  private static String getProductUrl(String env, long productId, Map<String, String> params) {
    StringBuilder sb = new StringBuilder(Utilities.getApplicationProperty(env));
    sb.append(String.format("/admin/products/%d.json", productId));
    GateWay.processParams(sb, params);
    return sb.toString();
  }

  public static String updateProduct(String env, long productId, String data) {
    String retval = null;
    String url = getProductUrl(env, productId, null);
    retval = RestHttpClient.processPut(url, data);
    return retval;
  }

  public static String getProduct(String env, long productId, Map<String, String> params) {
    String url = getProductUrl(env, productId, params);
    String so = RestHttpClient.processGet(url);
    return so;
  }

  public static void main(String[] args) throws Exception {
    /*Long id = 10376781898l;
    Document product = new Document();
    Document _id = new Document();
    _id.put(Constants.Product_Id, id);
    _id.put(Constants.Remote_Host, "localhost");
    product.put(Constants._Id, _id);
    MongoDBJDBC.updateProductIP(product);
    RequestsHandler.register(id);*/
    //Document id = new Document(Constants._Id, 8199345863L);
    //MongoDBJDBC.getProductLastUpdate(id);
  }

  public static String getProductMetafields(String env, long productId) {
    StringBuilder sb = new StringBuilder(Utilities.getApplicationProperty(env));
    sb.append(String.format("/admin/products/%d/metafields.json", productId));
    return RestHttpClient.processGet(sb.toString());
  }
  
  public static Document getProductMetafieldsPloy(String env, long productId) {
    Document metas = null;
    StringBuilder sb = new StringBuilder(Utilities.getApplicationProperty(env));
    sb.append(String.format("/admin/products/%d/metafields.json", productId));
    String json = RestHttpClient.processGet(sb.toString());
    metas = productMetafields(json);
    return metas;
  }

  public static String updateVariant(String env, long variantId, String data) {
    String retval = null;
    String url = getVariantUrl(env, variantId, null);
    retval = RestHttpClient.processPut(url, data);
    return retval;
  }

  public static String updateMetafield(String env, long id, String data) {
    String retval = null;
    String url = getMetafieldUrl(env, id, null);
    retval = RestHttpClient.processPut(url, data);
    return retval;
  }

  private static String getVariantUrl(String env, long variantId, Map<String, String> params) {
    StringBuilder sb = new StringBuilder(Utilities.getApplicationProperty(env));
    sb.append(String.format("/admin/variants/%d.json", variantId));
    GateWay.processParams(sb, params);
    return sb.toString();
  }

  private static String getMetafieldUrl(String env, long id, Map<String, String> params) {
    StringBuilder sb = new StringBuilder(Utilities.getApplicationProperty(env));
    sb.append(String.format("/admin/metafields/%d.json", id));
    GateWay.processParams(sb, params);
    return sb.toString();
  }

  public static String getProduct(String env, long productId) {
    GateWay.init();
    StringBuilder url = new StringBuilder(Utilities.getApplicationProperty(env));
    url.append(String.format("/admin/products/%s.json", productId));
    return RestHttpClient.processGet(url.toString());
  }

  public static Document getProductMetafield(String env, long id, String namespace, String key) {
    String meta = GateWay.getProductMetafields(env, id);
    Document metas = Document.parse(meta);
    List<Document> metafields = (List) metas.get(Constants.Metafields);
    Document retval = null;
    for (Document metafield : metafields) {
      if (metafield.getString(Constants.Namespace).equals(namespace) && metafield.getString(Constants.Key).equals(key)) {
        retval = metafield;
        break;
      }
    }
    return retval;
  }
  
  public static Document getAllCustomers(String env, Map<String, String> params, int pageLimit, int bookLimit) throws IOException {
    final Document[] object = new Document[1];
    Document next = null;
    int page = 0;
    params.put("limit", "" + pageLimit);
    int pageMax = pageLimit;
    while (pageLimit == pageMax) {
      params.put("page", ++page + "");
      String take = GateWay.getCustomers(env, params);
      Document tempdoc = Document.parse(take);
      if (object[0] == null) {
        object[0] = tempdoc;
      } else {
        next = tempdoc;
        List<Document> docs = (List) next.get("customers");
        docs.stream().forEach(((List) object[0].get("customers"))::add);
      }
      pageLimit = ((List) tempdoc.get("customers")).size();
      if (bookLimit < 0) {
        continue;
      } else if (((List) object[0].get("customers")).size() >= bookLimit) {
        break;
      }
      //if (true)break;
    }
    return object[0];
  }

  public static String getCustomers(String env, Map<String, String> params) {
    StringBuilder url = new StringBuilder(Utilities.getApplicationProperty(env));
    if (params != null && params.containsKey("id")) {
      url.append(String.format("/admin/customers/%s.json", params.remove("id").toString()));
    } else {
      url.append(String.format("/admin/customers.json"));
    }
    GateWay.processParams(url, params);
    return RestHttpClient.processGet(url.toString());
  }
  
  public static Document productMetafields(String metastr) {
    Document meta = new Document();
    List<Document> metafields = (List) Document.parse(metastr).get("metafields");
    metafields.stream().forEach(kv -> meta.append(kv.getString("key"),
      (kv.get("value") instanceof Integer) ? kv.getInteger("value").toString() : kv.getString("value")));
    return meta;
  }
}
