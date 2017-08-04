/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.data;

import com.gotkcups.io.RestHttpClient;
import com.gotkcups.io.Utilities;
import com.gotkcups.page.DocumentProcessor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.Document;

/**
 *
 * @author Ricardo
 */
public class RequestsHandler extends Thread {

    private final static Log log = LogFactory.getLog(RequestsHandler.class);
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

    public static void main(String[] args) throws Exception {
        log.debug("ccccc");
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
                    }
                }
            }
        }
        //Document vendors = Document.parse(s);
        //register(vendors);
    }
}
