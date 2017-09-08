/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.data;

import org.bson.Document;

/**
 *
 * @author Ricardo
 */
public class QDocument extends Document implements Comparable<QDocument> {

  @Override
  public int compareTo(QDocument foreign) {
    return (foreign.getQty().compareTo(this.getQty()) * 20) + this.getOptions().compareTo(foreign.getOptions());
  }
  
  
  private StringBuilder builder = new StringBuilder();
  private String getOptions() {
    builder.setLength(0);
    builder.append(this.getString("option1"));
    if (this.getString("option2") != null) {
      builder.append(this.getString("option2"));
    }
    if (this.getString("option3") != null) {
      builder.append(this.getString("option3"));
    }
    return builder.toString();
  }
  
  private String getQty() {
    return String.format("%08d", this.getInteger("inventory_quantity"));
  }
  
  public String toString() {
    return String.format("%08d-%s-%d", this.getInteger("inventory_quantity"), this.getOptions(), this.getInteger("position"));
  }
  
}
