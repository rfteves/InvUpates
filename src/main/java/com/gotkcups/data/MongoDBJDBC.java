/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.data;


import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.bson.Document;
import org.bson.conversions.Bson;

/**
 *
 * @author rfteves
 */
public class MongoDBJDBC {

  public static void main(String args[]) {

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
  }

  static Map<String, MongoDatabase> DATABASES = new HashMap<>();

  public static MongoDatabase getDatabase(String name) {
    // To connect to mongodb server
    MongoDatabase database = null;
    if (!DATABASES.containsKey(name)) {
      MongoCredential credential = MongoCredential.createCredential("username", "admin", "password".toCharArray());
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
}
