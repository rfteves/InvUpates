/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.data;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
            this.notify();
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
            DocumentProcessor.process(vendors);
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
}
