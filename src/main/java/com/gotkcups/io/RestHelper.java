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
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

/**
 *
 * @author rfteves
 */
@Component
public class RestHelper {

  @Value("${env}")
  private String env;

  public Document getAllProducts(Map<String, String> params) throws IOException {
    return getAllProducts(params, 50, -1);
  }

  public Document getAllProducts(Map<String, String> params, int pageLimit, int bookLimit) throws IOException {
    final Document[] object = new Document[1];
    Document next = null;
    int page = 0;
    params.put("limit", "" + pageLimit);
    int pageMax = pageLimit;
    while (pageLimit == pageMax) {
      params.put("page", ++page + "");
      String take = this.getProducts(params);
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

  public String getProductVariant(String variant_id) {
    StringBuilder sb = new StringBuilder(env);
    sb.append(String.format("/admin/variants/%s.json", variant_id));
    return RestHttpClient.processGet(sb.toString());
  }

  public String getCollects(String env) {
    StringBuilder sb = new StringBuilder(env);
    sb.append("/admin/collects.json");
    return RestHttpClient.processGet(sb.toString());
  }

  public String getProducts(Map<String, String> params) {
    StringBuilder url = new StringBuilder(env);
    if (params != null && params.containsKey("id")) {
      url.append(String.format("/admin/products/%s.json", params.remove("id").toString()));
    } else {
      url.append(String.format("/admin/products.json"));
    }
    this.processParams(url, params);
    return RestHttpClient.processGet(url.toString());
  }

  private void processParams(StringBuilder url, Map<String, String> params) {
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

  public String getCustomersCount(String env) {
    StringBuilder url = new StringBuilder(env);
    url.append("/admin/customers/count.json");
    return RestHttpClient.processGet(url.toString());
  }

  public String createVariantMetaField(long productId, long variantId, String jsondata) {
    StringBuilder url = new StringBuilder(env);
    url.append(String.format("/admin/products/%s/variants/%s/metafields.json", productId, variantId));
    return RestHttpClient.processPost(url.toString(), jsondata);
  }

  public String createProductMetaField(long productId, String jsondata) {
    StringBuilder url = new StringBuilder(env);
    url.append(String.format("/admin/products/%s/metafields.json", productId));
    return RestHttpClient.processPost(url.toString(), jsondata);
  }

  public String getVariantMetaField(long productId, long variantId) {
    StringBuilder url = new StringBuilder(env);
    url.append(String.format("/admin/products/%s/variants/%s/metafields.json", productId, variantId));
    return RestHttpClient.processGet(url.toString());
  }

  public String getOrder(long orderId) {
    StringBuilder url = new StringBuilder(env);
    url.append(String.format("/admin/orders/%s.json", orderId));
    return RestHttpClient.processGet(url.toString());
  }

  public String createProduct(String json) {
    StringBuilder url = new StringBuilder(env);
    url.append(String.format("/admin/products.json", ""));
    return RestHttpClient.processPost(url.toString(), json);
  }

  public String createProductVariant(long productId, String jsondata) {
    StringBuilder url = new StringBuilder(env);
    url.append(String.format("/admin/products/%s/variants.json", productId));
    return RestHttpClient.processPost(url.toString(), jsondata);
  }

  public void deleteMetaField(long productId, long metaid) {
    StringBuilder url = new StringBuilder(env);
    url.append(String.format("/admin/products/%d/metafields/%d.json", productId, metaid));
    RestHttpClient.processDelete(url.toString());
  }

  public String getCollects(int limit, int page, Map<String, String> params) {
    StringBuilder sb = new StringBuilder(env);
    StringBuilder url = new StringBuilder(String.format("/admin/custom_collections.json?limit=%d&page=%d&", limit, page));
    this.processParams(url, params);
    sb.append(url);
    return RestHttpClient.processGet(sb.toString());
  }

  private String getProductUrl(long productId, Map<String, String> params) {
    StringBuilder sb = new StringBuilder(env);
    sb.append(String.format("/admin/products/%d.json", productId));
    this.processParams(sb, params);
    return sb.toString();
  }

  public String updateProduct(long productId, String data) {
    String retval = null;
    String url = getProductUrl(productId, null);
    retval = RestHttpClient.processPut(url, data);
    return retval;
  }

  public String getProduct(long productId, Map<String, String> params) {
    String url = getProductUrl(productId, params);
    String so = RestHttpClient.processGet(url);
    return so;
  }

  public Document getProductMetafields(long productId) {
    return getProductMetafields(productId, false);
  }

  public Document getProductMetafields(long productId, boolean raw) {
    Document metas = null;
    StringBuilder sb = new StringBuilder(env);
    sb.append(String.format("/admin/products/%d/metafields.json", productId));
    String json = RestHttpClient.processGet(sb.toString());
    if (raw) {
      return Document.parse(json);
    } else {
      metas = productMetafields(json);
      return metas;
    }
  }

  public String updateVariant(long variantId, String data) {
    String retval = null;
    String url = getVariantUrl(variantId, null);
    retval = RestHttpClient.processPut(url, data);
    return retval;
  }

  public String updateMetafield(long id, String data) {
    String retval = null;
    String url = this.getMetafieldUrl(id, null);
    retval = RestHttpClient.processPut(url, data);
    return retval;
  }

  private String getVariantUrl(long variantId, Map<String, String> params) {
    StringBuilder sb = new StringBuilder(env);
    sb.append(String.format("/admin/variants/%d.json", variantId));
    this.processParams(sb, params);
    return sb.toString();
  }

  private String getMetafieldUrl(long id, Map<String, String> params) {
    StringBuilder sb = new StringBuilder(env);
    sb.append(String.format("/admin/metafields/%d.json", id));
    this.processParams(sb, params);
    return sb.toString();
  }

  public Document getProduct(long productId) {
    StringBuilder url = new StringBuilder(env);
    url.append(String.format("/admin/products/%s.json", productId));
    return Document.parse(RestHttpClient.processGet(url.toString()));
  }

  public String getCountries() {
    StringBuilder url = new StringBuilder(env);
    url.append("/admin/countries.json");
    return RestHttpClient.processGet(url.toString());
  }

  public Document getProductMetafield(long id, String namespace, String key) {
    Document metas = getProductMetafields(id);
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

  public Document getAllCustomers(Map<String, String> params, int pageLimit, int bookLimit) throws IOException {
    final Document[] object = new Document[1];
    Document next = null;
    int page = 0;
    params.put("limit", "" + pageLimit);
    int pageMax = pageLimit;
    while (pageLimit == pageMax) {
      params.put("page", ++page + "");
      String take = this.getCustomers(params);
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

  public String getCustomers(Map<String, String> params) {
    StringBuilder url = new StringBuilder(env);
    if (params != null && params.containsKey("id")) {
      url.append(String.format("/admin/customers/%s.json", params.remove("id").toString()));
    } else {
      url.append(String.format("/admin/customers.json"));
    }
    this.processParams(url, params);
    return RestHttpClient.processGet(url.toString());
  }

  public Document productMetafields(String metastr) {
    Document meta = new Document();
    List<Document> metafields = (List) Document.parse(metastr).get("metafields");
    metafields.stream().forEach(kv -> meta.append(kv.getString("key"),
      (kv.get("value") instanceof Integer) ? kv.getInteger("value").toString() : kv.getString("value")));
    return meta;
  }

  public Document getAllCollects(Map<String, String> params, int pageLimit, int bookLimit) throws IOException {
    final Document[] object = new Document[1];
    Document next = null;
    int page = 0;
    params.put("limit", "" + pageLimit);
    int pageMax = pageLimit;
    while (pageLimit == pageMax) {
      params.put("page", ++page + "");
      String take = this.getCollects(params);
      Document tempdoc = Document.parse(take);
      if (object[0] == null) {
        object[0] = tempdoc;
      } else {
        next = tempdoc;
        List<Document> docs = (List) next.get("collects");
        docs.stream().forEach(((List) object[0].get("collects"))::add);
      }
      pageLimit = ((List) tempdoc.get("collects")).size();
      if (bookLimit < 0) {
        continue;
      } else if (((List) object[0].get("collects")).size() >= bookLimit) {
        break;
      }
      //if (true)break;
    }
    return object[0];
  }

  public String getCollects(Map<String, String> params) {
    StringBuilder sb = new StringBuilder(env);
    StringBuilder url = new StringBuilder("/admin/collects.json");
    this.processParams(url, params);
    sb.append(url);
    return RestHttpClient.processGet(sb.toString());
  }

  public Document getAllOrders(Map<String, String> params, int pageLimit, int bookLimit) throws IOException {
    final Document[] object = new Document[1];
    Document next = null;
    int page = 0;
    params.put("limit", "" + pageLimit);
    int pageMax = pageLimit;
    while (pageLimit == pageMax) {
      params.put("page", ++page + "");
      String take = this.getOrders(params);
      Document tempdoc = Document.parse(take);
      if (object[0] == null) {
        object[0] = tempdoc;
      } else {
        next = tempdoc;
        List<Document> docs = (List) next.get("orders");
        docs.stream().forEach(((List) object[0].get("orders"))::add);
      }
      pageLimit = ((List) tempdoc.get("orders")).size();
      if (bookLimit < 0) {
        continue;
      } else if (((List) object[0].get("orders")).size() >= bookLimit) {
        break;
      }
    }
    return object[0];
  }

  public String getOrders(Map<String, String> params) {
    StringBuilder url = new StringBuilder(env);
    if (params != null && params.containsKey("id")) {
      url.append(String.format("/admin/orders/%s.json", params.remove("id").toString()));
    } else {
      url.append(String.format("/admin/orders.json"));
    }
    this.processParams(url, params);
    return RestHttpClient.processGet(url.toString());
  }
}
