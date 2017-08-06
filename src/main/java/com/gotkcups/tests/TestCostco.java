/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.tests;

import java.util.List;
import org.apache.commons.lang.StringEscapeUtils;
import org.bson.Document;

/**
 *
 * @author rfteves
 */
public class TestCostco {

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    Document prod = Document.parse("{\"product\":{ \"pricePerUnit\" : \"\", \"unitOfMeasure\" : \"Price Per null:\", \"catentry\" : \"980778\", \"partNumber\" : \"1103978\", \"manufacturerPartNumber\" : \" \", \"productName\" : \"Gloria Vanderbilt Ladiesâ€™ Belted Capri\", \"productUrl\" : \"https://www.costco.com/Gloria-Vanderbilt-Ladies%e2%80%99-Belted-Capri.product.1103978.html\", \"price\" : \"OS45Nw==\", \"listPrice\" : \"LTEuMDA=\", \"date\" : \"\", \"date2\" : \"\", \"type\" : \"0\", \"message\" : \"0\", \"inventory\" : \"IN_STOCK\", \"minQty\" : \"1\", \"maxQty\" : \"9999\", \"ordinal\" : \"408.0\", \"itemSequence\" : \"1.2\", \"plainPackaging\" : \"2\", \"parent_img_url\" : \"https://images.costco-static.com/ImageDelivery/imageService?profileId=12026540&amp;imageId=100314040-847__1&amp;recipeName=300\", \"img_url\" : \"https://images.costco-static.com/ImageDelivery/imageService?profileId=12026540&amp;imageId=100314040-847_blue_1&amp;recipeName=300\", \"options\" : [\"7000000000000000095\", \"7000000000000140157\"], \"itemLimitOneQty\" : \"false\", \"feeRegions\" : [], \"feeOptions\" : [], \"regionPrices\" : [] }}");
    Document product = (Document) prod.get("product");
    Document options = Document.parse("{ \"options\" : [[{ \"n\" : \"Color\", \"s\" : \"1\", \"i\" : \"100314040\", \"v\" : { \"7000000000000000095\" : \"Blue\", \"7000000000000000053\" : \"White\", \"7000000000000000468\" : \"Orange\" }, \"vimage\" : { \"7000000000000000095\" : \"Blue\", \"7000000000000000053\" : \"White\", \"7000000000000000468\" : \"Orange\" } }, { \"n\" : \"Women&#039;s Size\", \"s\" : \"0\", \"i\" : \"100314040\", \"v\" : { \"7000000000000140061\" : \"12\", \"7000000000000229008\" : \"18W\", \"7000000000000140062\" : \"14\", \"7000000000000242011\" : \"24W\", \"7000000000000229009\" : \"16W\", \"7000000000000229010\" : \"20W\", \"7000000000000139508\" : \"18\", \"7000000000000140157\" : \"6\", \"7000000000000139509\" : \"16\", \"7000000000000140158\" : \"8\", \"7000000000000229012\" : \"22W\", \"7000000000000140159\" : \"10\" }, \"vimage\" : { \"7000000000000140061\" : \"Women&#039;s Size\", \"7000000000000229008\" : \"Women&#039;s Size\", \"7000000000000140062\" : \"Women&#039;s Size\", \"7000000000000242011\" : \"Women&#039;s Size\", \"7000000000000229009\" : \"Women&#039;s Size\", \"7000000000000229010\" : \"Women&#039;s Size\", \"7000000000000139508\" : \"Women&#039;s Size\", \"7000000000000140157\" : \"Women&#039;s Size\", \"7000000000000139509\" : \"Women&#039;s Size\", \"7000000000000140158\" : \"Women&#039;s Size\", \"7000000000000229012\" : \"Women&#039;s Size\", \"7000000000000140159\" : \"Women&#039;s Size\" } }]] }");
    List<Document> opts = (List) ((List) options.get("options")).get(0);
    for (Document option : opts) {
      String key = StringEscapeUtils.unescapeHtml(option.getString("n"));
      String value = null;
      Document v = (Document) option.get("v");
      List<String> productOptions = (List) product.get("options");
      for (String productOption : productOptions) {
        if (v.containsKey(productOption)) {
          value = v.getString(productOption);
          break;
        }
      }
      product.put(key, value);
    }
  }

}
