/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.data;

/**
 *
 * @author ricardo
 */
public class VariantInfo {
  private double compare_at_price;
  private double price;
  private int inventory_quantity;
  private long id;
  private long productId;

  /**
   * @return the compare_at_price
   */
  public double getCompare_at_price() {
    return compare_at_price;
  }

  /**
   * @param compare_at_price the compare_at_price to set
   */
  public void setCompare_at_price(double compare_at_price) {
    this.compare_at_price = compare_at_price;
  }

  /**
   * @return the price
   */
  public double getPrice() {
    return price;
  }

  /**
   * @param price the price to set
   */
  public void setPrice(double price) {
    this.price = price;
  }

  /**
   * @return the inventory_quantity
   */
  public int getInventory_quantity() {
    return inventory_quantity;
  }

  /**
   * @param inventory_quantity the inventory_quantity to set
   */
  public void setInventory_quantity(int inventory_quantity) {
    this.inventory_quantity = inventory_quantity;
  }

  /**
   * @return the id
   */
  public long getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(long id) {
    this.id = id;
  }

  /**
   * @return the productId
   */
  public long getProductId() {
    return productId;
  }

  /**
   * @param productId the productId to set
   */
  public void setProductId(long productId) {
    this.productId = productId;
  }
}
