/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.data;

import com.gotkcups.io.RestHttpClient;
import com.gotkcups.io.Utilities;
import com.gotkcups.page.DocumentProcessor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.bson.Document;

/**
 *
 * @author Ricardo
 */
public class RequestsHandler extends Thread {

    private static List<Document> REQUESTS = new ArrayList<>();
    private static RequestsHandler HANDLER;
    private boolean removing, processing;

    public static void register(Document vendors) {
        if (HANDLER == null) {
            synchronized (REQUESTS) {
                if (HANDLER == null) {
                    HANDLER = new RequestsHandler();
                    HANDLER.start();
                }
            }
        }
        HANDLER.add(vendors);
    }

    private void add(Document vendors) {
        synchronized (this) {
            while (removing) {
                try {
                    this.wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(RequestsHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            REQUESTS.add(vendors);
            removing = true;
            this.notifyAll();
        }
    }

    public void run() {
        Document vendors = null;
        while (true) {
            synchronized (this) {
                while (REQUESTS.isEmpty()) {
                    try {
                        this.wait();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(RequestsHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                vendors = REQUESTS.remove(0);
                removing = false;
                this.notify();
            }
            DocumentProcessor.accept(vendors);
            synchronized (this) {
                while (DocumentProcessor.isProcessing()) {
                    try {
                        this.wait(100);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(RequestsHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    removing = false;
                    this.notify();
                }
                removing = false;
                this.notify();
            }
        }
    }
    private static final LogManager logManager = LogManager.getLogManager();

    static {
        try {
            LogManager.getLogManager().reset();
            logManager.readConfiguration(new FileInputStream("./logging.properties"));
            java.util.logging.Logger.getLogger("org.apache.http.wire").setLevel(java.util.logging.Level.WARNING);
            java.util.logging.Logger.getLogger("org.apache.http.headers").setLevel(java.util.logging.Level.WARNING);
            System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
            System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
            System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire", "ERROR");
            System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http", "ERROR");
            System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.headers", "ERROR");
        } catch (IOException ex) {
            Logger.getLogger(RequestsHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(RequestsHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    static {
        try {
            org.apache.log4j.LogManager.resetConfiguration();
            PropertyConfigurator.configure("./log4j.properties");
        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.getLogger(RequestsHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static final Logger LOGGER = Logger.getLogger("confLogger");

    public static void main(String[] args) throws Exception {
        LOGGER.fine("Fine message logged");
        System.out.println();
        Map<String, String> params = new HashMap<>();
        params.put("fields", "id,variants");
        Document resp = Utilities.getAllProducts("prod", params, 50, 150);
        List<Document> products = (List) resp.get("products");
        for (Document product : products) {
            List<Document> variants = (List) product.get("variants");
            for (Document variant : variants) {
                if (!variant.getString("sku").toLowerCase().endsWith("s")) {
                    continue;
                }
                String meta = RestHttpClient.getVariantMetaField("prod", product.getLong("id"), variant.getLong("id"));
                Document metas = Document.parse(meta);
                List<Document> metafields = (List) metas.get("metafields");
                for (Document metafield : metafields) {
                    if (metafield.getString("namespace").equals("inventory") && metafield.getString("key").equals("vendor")) {
                        String value = metafield.getString("value");
                        Document values = Document.parse(value);
                        register(values);
                        System.out.println();
                    }
                }
            }
        }
        //Document vendors = Document.parse(s);
        //register(vendors);
    }
}
