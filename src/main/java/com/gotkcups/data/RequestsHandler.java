/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.data;

import com.gotkcups.page.DocumentProcessor;
import java.util.ArrayList;
import java.util.List;
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

  private final Log log = LogFactory.getLog(RequestsHandler.class);
  private final static List<Document> REQUESTS = new ArrayList<>();
  private static RequestsHandler HANDLER;

  public static void register(Document vendors) {
    if (HANDLER == null || !HANDLER.isAlive()) {
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
      while (!REQUESTS.isEmpty()) {
        try {
          this.wait();
        } catch (InterruptedException ex) {
          Logger.getLogger(RequestsHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
      REQUESTS.add(vendors);
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
        this.notifyAll();
      }
      DocumentProcessor.accept(vendors);
    }
  }
}
