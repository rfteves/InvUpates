/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.tests;

import com.gotkcups.data.Constants;
import com.gotkcups.io.GateWay;
import java.util.List;
import org.bson.Document;

/**
 *
 * @author Ricardo
 */
public class TestCountries {

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    String countries = GateWay.getCountries(Constants.Production);
    Document d = Document.parse(countries);
    List<Document>c = (List)d.get(Constants.Countries);
    List<Document>p = (List)c.get(0).get(Constants.Provinces);
    //List<Document>c = (List)c.get(Constants.Countries);
    int kkk=0;
  }
  
}
