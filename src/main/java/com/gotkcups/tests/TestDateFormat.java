/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.tests;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 *
 * @author Ricardo
 */
public class TestDateFormat {

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {//Sun, 20 Sep 2037 01:28:23 -0000
    SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss -0000");
    System.out.println(sdf.format(new Date()));
  }
  
}
