/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.tests;

import com.gotkcups.data.RequestsHandler;

/**
 *
 * @author Ricardo
 */
public class TestRegisterLongId {

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    RequestsHandler.register(123789082647L, true);
    System.exit(0);
  }
  
}
