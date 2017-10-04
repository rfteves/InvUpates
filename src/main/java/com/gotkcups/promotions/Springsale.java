/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.promotions;

import com.gotkcups.data.Constants;
import com.gotkcups.io.GateWay;
import java.util.HashMap;
import java.util.Map;
import org.bson.Document;

/**
 *
 * @author Ricardo
 */
public class Springsale {

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) throws Exception {
    // 
    Map<String, String> params = new HashMap<>();
    params.put("collection_id", "6732382231");
    //Document resp = GateWay.getAllCustomCollection("prod", 6732382231L, params, 150, -1);
    Document collects = GateWay.getAllCollects(Constants.Production, params, 150, -1);
    int y=0;
  }
  
}
