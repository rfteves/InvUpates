/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.adhoc;

import com.gotkcups.data.Constants;
import com.gotkcups.io.GateWay;
import com.gotkcups.io.Utilities;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.Document;

/**
 *
 * @author Ricardo
 */
public class BingIt {

  private final static Log log = LogFactory.getLog(BingIt.class);

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    log.info("Begin BingIt");
    try {
      StringBuilder sb = createAds();
      ftp(sb, args[0]);
    } catch (Throwable ex) {
      Logger.getLogger(BingIt.class.getName()).log(Level.SEVERE, null, ex);
      log.error("Error BingIt", ex);
    }
  }

  private static String[] GOOGLE_SHOPPING_FIELDS = {
    "product.id",
    "product.handle",
    "product.title",
    "global.description",
    "link",
    "image_link",
    "additional_image_link",
    "availability",
    "price",
    "category",
    "vendor",
    "adwords_grouping",
    "adwords_labels",
    "product_type",
    "age_group",
    "condition",
    "gender",
    "gtin",
    "size",
    "color"
  };

  private static String SEPARATOR = "\t";
  private static String NEW_LINE = "\n";
  private static String BLANK_VALUE = "";

  private static StringBuilder createAds() throws IOException, Exception {
    Map<String, String> params = new HashMap<>();
    params.put("fields", "id,title,options,product_type,vendor,handle,title,variants,images");
    Set<Document> sorted = new TreeSet<>();
    Document resp = GateWay.getAllProducts("prod", params, 50, -1);
    List<Document> products = (List) resp.get("products");
    StringBuilder sb = new StringBuilder();
    sb.append("id");
    sb.append(SEPARATOR);
    sb.append("title");
    sb.append(SEPARATOR);
    sb.append("brand");
    sb.append(SEPARATOR);
    sb.append("link");
    sb.append(SEPARATOR);
    sb.append("price");
    sb.append(SEPARATOR);
    sb.append("description");
    sb.append(SEPARATOR);
    sb.append("image_link");
    sb.append(SEPARATOR);
    sb.append("gtin");
    sb.append(SEPARATOR);
    sb.append("availability");
    sb.append(SEPARATOR);
    sb.append("condition");
    sb.append(SEPARATOR);
    sb.append("product_type");
    sb.append(SEPARATOR);
    sb.append("google_product_category");
    sb.append(SEPARATOR);
    sb.append("gender");
    sb.append(SEPARATOR);
    sb.append("age_group");
    sb.append(SEPARATOR);
    //sb.append("adwords_grouping");
    //sb.append(SEPARATOR);
    //sb.append("adwords_labels");
    //sb.append(SEPARATOR);
    sb.append("color");
    sb.append(SEPARATOR);
    sb.append("size");
    sb.append(SEPARATOR);
    sb.append("custom_label_0");
    sb.append(SEPARATOR);
    sb.append("custom_label_1");
    sb.append(SEPARATOR);
    sb.append("custom_label_2");
    sb.append(SEPARATOR);
    sb.append("custom_label_3");
    sb.append(SEPARATOR);
    sb.append("custom_label_4");
    sb.append(NEW_LINE);
    for (Document product : products) {
      String metastr = GateWay.getProductMetafields("prod", product.getLong("id"));
      Document metas = metaploy(metastr);
      List<Document> variants = (List) product.get("variants");
      List<Document> options = (List) product.get("options");
      Document images = imageploy(product);
      System.out.println("processing " + product.getString("title"));
      for (Document variant : variants) {
        if (variant.getInteger("inventory_quantity") == 0) {
          continue;
        }
        sb.append("shopify_US_");
        sb.append(product.getLong("id").toString());
        sb.append("_");
        sb.append(variant.getLong("id").toString());
        sb.append(SEPARATOR);
        sb.append(metas.getOrDefault("title_tag", product.getString("title")));
        sb.append(SEPARATOR);
        sb.append(product.getString(Constants.Vendor));
        sb.append(SEPARATOR);
        sb.append("https://www.gotkcups.com/products/");
        sb.append(product.getString("handle"));
        sb.append(SEPARATOR);
        sb.append(variant.getString("price"));
        sb.append(SEPARATOR);
        sb.append(metas.getOrDefault("description_tag", product.getString("title")));
        sb.append(SEPARATOR);
        if (images.size() == 1 || variant.getLong("image_id") == null) {
          sb.append(((Document) ((List) product.get("images")).get(0)).getString("src"));
        } else {
          sb.append(images.getString(variant.getLong("image_id").toString()));
        }
        sb.append(SEPARATOR);
        sb.append(variant.getString("barcode"));
        sb.append(SEPARATOR);
        sb.append("In Stock");
        sb.append(SEPARATOR);
        sb.append("New");
        sb.append(SEPARATOR);
        sb.append(product.getString("product_type"));
        sb.append(SEPARATOR);
        sb.append(metas.getString("google_product_type"));
        sb.append(SEPARATOR);
        sb.append(metas.getOrDefault("gender", BLANK_VALUE));
        sb.append(SEPARATOR);
        sb.append(metas.getOrDefault("age_group", BLANK_VALUE));
        sb.append(SEPARATOR);
        //sb.append(metas.getOrDefault("adwords_grouping", BLANK_VALUE));
        //sb.append(SEPARATOR);
        //sb.append(metas.getOrDefault("adwords_labels", BLANK_VALUE));
        //sb.append(SEPARATOR);
        if (options.size() == 0) {
          sb.append(BLANK_VALUE);
          sb.append(SEPARATOR);
          sb.append(BLANK_VALUE);
          sb.append(SEPARATOR);
        } else if (options.size() == 1 && options.get(0).getString("name").toLowerCase().contains("color")) {
          sb.append(variant.getString("option1"));
          sb.append(SEPARATOR);
          sb.append(BLANK_VALUE);
          sb.append(SEPARATOR);
        } else if (options.size() == 1 && options.get(0).getString("name").toLowerCase().contains("color") == false) {
          sb.append(BLANK_VALUE);
          sb.append(SEPARATOR);
          sb.append(variant.getString("option1"));
          sb.append(SEPARATOR);
        } else if (options.size() == 2 && options.get(0).getString("name").toLowerCase().contains("color")) {
          sb.append(variant.getString("option1"));
          sb.append(SEPARATOR);
          sb.append(variant.getString("option2"));
          sb.append(SEPARATOR);
        } else if (options.size() == 2 && options.get(0).getString("name").toLowerCase().contains("color") == false) {
          sb.append(variant.getString("option2"));
          sb.append(SEPARATOR);
          sb.append(variant.getString("option1"));
          sb.append(SEPARATOR);
        }
        sb.append(metas.getOrDefault("custom_label_0", BLANK_VALUE));
        sb.append(SEPARATOR);
        sb.append(metas.getOrDefault("custom_label_1", BLANK_VALUE));
        sb.append(SEPARATOR);
        sb.append(metas.getOrDefault("custom_label_2", BLANK_VALUE));
        sb.append(SEPARATOR);
        sb.append(metas.getOrDefault("custom_label_3", BLANK_VALUE));
        sb.append(SEPARATOR);
        sb.append(metas.getOrDefault("custom_label_4", BLANK_VALUE));
        sb.append(NEW_LINE);
      }
    }
    return sb;
  }

  private static Document metaploy(String metastr) {
    Document meta = new Document();
    List<Document> metafields = (List) Document.parse(metastr).get("metafields");
    metafields.stream().forEach(kv -> meta.append(kv.getString("key"),
      (kv.get("value") instanceof Integer) ? kv.getInteger("value").toString() : kv.getString("value")));
    return meta;
  }

  private static Document imageploy(Document product) {
    Document meta = new Document();
    List<Document> images = (List) product.get("images");
    images.stream().forEach(kv -> meta.append(kv.getLong("id").toString(), kv.getString("src")));
    return meta;
  }

  public static final String CATALOG = "gotkcups.catalog.txt";

  private static void ftp(StringBuilder data, String filename) {
    Session session = null;
    Channel channel = null;
    try {
      ByteArrayInputStream bis = new ByteArrayInputStream("teves.us\n".getBytes());
      JSch ssh = new JSch();
      ssh.setKnownHosts(bis);
      java.util.Properties config = new java.util.Properties();
      config.put("StrictHostKeyChecking", "no");
      session = ssh.getSession(Utilities.getApplicationProperty("sftp.username"), "teves.us", 22);
      session.setPassword(Utilities.getApplicationProperty("sftp.password"));
      session.setConfig(config);
      session.connect();
      channel = session.openChannel("sftp");
      channel.connect();
      ChannelSftp sftp = (ChannelSftp) channel;
      sftp.put(new ByteArrayInputStream(data.toString().getBytes()),
        String.format("/var/www/tools.gotkcups.com/webapps/%s.txt", filename));
    } catch (JSchException e) {
      e.printStackTrace();
    } catch (SftpException e) {
      e.printStackTrace();
    } finally {
      if (channel != null) {
        channel.disconnect();
      }
      if (session != null) {
        session.disconnect();
      }
    }
  }
}
