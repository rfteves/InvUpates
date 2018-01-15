/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.data;

import com.gotkcups.configs.MainConfiguration;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Accumulators.max;
import static com.mongodb.client.model.Aggregates.group;
import static com.mongodb.client.model.Aggregates.match;
import static com.mongodb.client.model.Aggregates.unwind;
import static com.mongodb.client.model.Filters.eq;
import com.mongodb.client.model.PushOptions;
import com.mongodb.client.model.UpdateOptions;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.inc;
import static com.mongodb.client.model.Updates.pushEach;
import com.mongodb.client.result.UpdateResult;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import org.bson.BsonDateTime;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 *
 * @author rfteves
 */

@Service
public class MongoDBJDBC {
  
  @Autowired
  private MainConfiguration config;
  /*public static void main(String args[]) {

    try {
      Collection<Document> docs = getDocuments("productinfos");
      System.out.println(docs.size());
    } catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
    }
  }

  public static void addDocuments(String name, Collection<Document> docs) {
    MongoDatabase database = getDatabase("gotkcups");
    MongoCollection<Document> coll = database.getCollection(name);
    docs.stream().forEach(coll::insertOne);
  }

  public static void addDocument(String name, Document doc) {
    MongoDatabase database = getDatabase("gotkcups");
    MongoCollection<Document> coll = database.getCollection(name);
    coll.insertOne(doc);
  }

  private static UpdateOptions opts = new UpdateOptions();

  static {
    opts.upsert(true);
  }

  public static void upsertDocument(String name, Bson bson, Document doc) {
    MongoDatabase database = getDatabase("gotkcups");
    MongoCollection<Document> coll = database.getCollection(name);
    coll.replaceOne(bson, doc, opts);
  }

  public static Collection<Document> getDocuments(String name) {
    Collection<Document> INFOS = new ArrayList<>();
    MongoDatabase database = getDatabase("gotkcups");
    MongoCollection<Document> coll = database.getCollection(name);
    Bson bson = Filters.eq("instock", false);
    coll.find().filter(bson).forEach(new Consumer<Document>() {
      @Override
      public void accept(Document doc) {
        INFOS.add(doc);
      }
    });
    return INFOS;
  }

  public static Collection<Document> filterCollection(String collectionName, Bson bson) {
    Collection<Document> INFOS = new ArrayList<>();
    MongoDatabase database = getDatabase("gotkcups");
    MongoCollection<Document> coll = database.getCollection(collectionName);
    coll.find().filter(bson).forEach(new Consumer<Document>() {
      @Override
      public void accept(Document doc) {
        INFOS.add(doc);
      }
    });
    return INFOS;
  }

  public static void dropCollection(String name) {
    MongoDatabase database = getDatabase("gotkcups");
    database.getCollection(name).drop();
  }*/
  private final static Logger log = LoggerFactory.getLogger(MongoDBJDBC.class);
  static Map<String, MongoDatabase> DATABASES = new HashMap<>();

  public MongoDatabase getDatabase(String name) {
    // To connect to mongodb server
    MongoDatabase database = null;
    if (!DATABASES.containsKey(name)) {
      MongoCredential credential = MongoCredential.createCredential(config.mongodbUser, "admin", config.mongodbPassword.toCharArray());
      MongoClientOptions options = MongoClientOptions.builder().sslEnabled(false).build();
      MongoClient client = new MongoClient(new ServerAddress("teves.us", 27017),
        Arrays.asList(credential), options);
      synchronized (DATABASES) {
        if (!DATABASES.containsKey(name)) {
          database = client.getDatabase(name);
          DATABASES.put(name, database);
        } else {
          database = DATABASES.get(name);
        }
      }
    } else {
      database = DATABASES.get(name);
    }
    return database;
  }

  public UpdateResult updateProductIP(Document product) {
    log.debug("Updating product id " + product.get(Constants._Id));
    UpdateResult result = null;
    try {
      MongoDatabase database = getDatabase(Constants.Table_GotKcups);
      MongoCollection<Document> products = database.getCollection(Constants.Collection_Product_IP);
      Calendar now = Calendar.getInstance(TimeZone.getTimeZone("America/Los_Angeles"));
      BsonDateTime laUsaTimeNow = new BsonDateTime(now.getTimeInMillis());
      result = products.updateOne(eq(Constants._Id, product.get(Constants._Id)),
        combine(inc(Constants.Visits, 1),
          pushEach(Constants.Last_Update, Arrays.asList(laUsaTimeNow), new PushOptions().slice(10))),
        new UpdateOptions().upsert(true));
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      return result;
    }
  }

  public Calendar getProductLastUpdate(Document product) {
    Calendar now = Calendar.getInstance(TimeZone.getTimeZone("America/Los_Angeles"));
    MongoDatabase database = getDatabase(Constants.Table_GotKcups);
    MongoCollection<Document> products = database.getCollection(Constants.Collection_Product_IP);
    AggregateIterable<Document> res = products.aggregate(Arrays.asList(match(eq("_id.product_id",
      product.get(Constants._Id))), unwind("$lastUpdate"),
      group("product_id", max("maxdate", "$lastUpdate"))));
    Document first = res.first();
    if (first != null) {
      now.setTime(first.getDate("maxdate"));
    } else {
      now = null;
    }
    return now;
  }

  public UpdateResult updateVariantIP(Document variant, StringBuilder message) {
    log.debug("Updating variant id " + variant.get(Constants.Id));
    UpdateResult result = null;
    try {
      MongoDatabase database = getDatabase(Constants.Table_GotKcups);
      MongoCollection<Document> variants = database.getCollection(Constants.Collection_Variant_IP);
      Calendar now = Calendar.getInstance(TimeZone.getTimeZone("America/Los_Angeles"));
      BsonDateTime laUsaTimeNow = new BsonDateTime(now.getTimeInMillis());
      result = variants.updateOne(eq(Constants._Id, variant.get(Constants.Id)),
        combine(inc(Constants.Visits, 1),
          pushEach(Constants.Last_Update, Arrays.asList(laUsaTimeNow), new PushOptions().slice(10)),
          pushEach(Constants.Changes, Arrays.asList(message.toString()), new PushOptions().slice(10))),
        new UpdateOptions().upsert(true));
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      log.debug("Updating done, variant id " + variant.get(Constants.Id));
      return result;
    }
  }
}
