/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.data;

import org.bson.Document;

/**
 *
 * @author rfteves
 */
public class JDocument extends Document implements Comparable<JDocument> {

  @Override
  public int compareTo(JDocument o) {
    String foreign = String.format("%s.%s.%s", o.getLong("product_id").toString(), o.getLong("id").toString(), o.getString("sku"));
    String own = String.format("%s.%s.%s", this.getLong("product_id").toString(), this.getLong("id").toString(), o.getString("sku"));
    return own.compareTo(foreign);
  }
  
}
