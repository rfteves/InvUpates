/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.tests;

import com.gotkcups.data.KeurigAnchor;
import com.gotkcups.page.KeurigRewards;
import java.util.Arrays;

/**
 *
 * @author Ricardo
 */
public class TestKeurigRewards {

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) throws Exception {
    String[]skus = {"5000068926", "5000081025", "5000082878", "5000195850", "5000068939", "5000068926"};
    Arrays.asList(skus).stream().forEach(sku->{
      KeurigAnchor a = KeurigRewards.getKeurigAnchor(sku);
      System.out.println(a.getDataCode() + ":" + a.getDataPurchasable());
    });
  }
  
}
