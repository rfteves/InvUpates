/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.invupdates;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ricardo
 */
public class ProductInfo {
  private long id;
  private final List<VariantInfo>variants = new ArrayList<>();
  

  /**
   * @return the variants
   */
  public List<VariantInfo> getVariants() {
    return variants;
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
}
