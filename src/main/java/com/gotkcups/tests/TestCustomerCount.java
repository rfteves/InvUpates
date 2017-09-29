/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.tests;

import com.gotkcups.data.Constants;
import com.gotkcups.io.GateWay;

/**
 *
 * @author ricardo
 */
public class TestCustomerCount {

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    System.out.println(GateWay.getCustomersCount(Constants.Production));
  }
  
}
