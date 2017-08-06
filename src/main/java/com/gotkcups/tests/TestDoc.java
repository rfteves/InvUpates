/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.tests;

import org.bson.Document;

/**
 *
 * @author rfteves
 */
public class TestDoc {

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    String options = "{\"value\":[[]]}";
    options="{\"value\":[[{ \"pricePerUnit\" : \"\",\"unitOfMeasure\" : \"Price Per null:\",\"unpriced\" : false,\"single\":true,\"catentry\" : \"1000008633\",\"partNumber\" : \"220743\",\"manufacturerPartNumber\" : \"\",\"price\" : \"MzkuOTk=\",\"productName\" : \"12-pack Dust-Off Compressed Gas Duster\",\"productUrl\" : \"https://www.costco.com/12-pack-Dust-Off-Compressed-Gas-Duster.product.220743.html\",\"listPrice\" : \"LTEuMA==\",\"date\" : \"\",\"date2\" : \"\",\"type\" : \"0\",\"message\" : \"0\",\"inventory\" : \"IN_STOCK\",\"minQty\" : \"1\",\"maxQty\" : \"5\",\"ordinal\" : \"995291.0\",\"itemSequence\" : \"\",\"img_url\" : \"https://images.costco-static.com/ImageDelivery/imageService?profileId=12026540&amp;imageId=220743-847__1&amp;recipeName=300\",\"plainPackaging\" : \"2\",\"options\" : [],\"itemLimitOneQty\" : \"false\",\"feeRegions\" : [ ], \"feeOptions\" : [ ], \"regionPrices\" : [ ] }]]}";
    Object opts = Document.parse(options);
    //Object obj = opts.get("value");
    System.out.println();
  }
  
}
