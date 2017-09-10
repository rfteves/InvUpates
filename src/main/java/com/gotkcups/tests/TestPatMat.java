/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.tests;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author rfteves
 */
public class TestPatMat {

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    String charseq = "9.5w";
    Pattern numeric = Pattern.compile("[0-9\\.]+");
    Pattern numletter = Pattern.compile("[0-9\\.[a-z]+", Pattern.CASE_INSENSITIVE);
    Pattern letteric = Pattern.compile("[a-z]+", Pattern.CASE_INSENSITIVE);
    Matcher m = numeric.matcher(charseq);
    System.out.println(m.matches());
  }
  
}
