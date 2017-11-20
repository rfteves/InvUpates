/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.googlefeed;

import com.google.api.services.content.model.ProductTax;
import com.gotkcups.data.Constants;
import com.gotkcups.io.RestHelper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 *
 * @author rfteves
 */
@Component
public class UpdateStateTaxes extends Task {

  @Autowired
  private RestHelper restHelper;
  private List<ProductTax> taxes;

  /**
   * @return the taxes
   */
  public List<ProductTax> getTaxes() {
    return taxes;
  }

  @Override
  public void process(String... args) throws Exception {
    taxes = new ArrayList<>();
    ProductTax tax = new ProductTax();
    tax.setCountry("US");
    tax.setRate(0.0);
    tax.setTaxShip(Boolean.FALSE);
    taxes.add(tax);
    String countries = restHelper.getCountries();
    Document d = Document.parse(countries);
    List<Document> c = (List) d.get(Constants.Countries);
    List<Document> p = (List) c.get(0).get(Constants.Provinces);
    p.stream().forEach(new Consumer<Document>() {
      @Override
      public void accept(Document ar) {
        double rate = ar.getDouble(Constants.Tax) * 100;
        if (rate != 0) {
          rate = BigDecimal.valueOf(rate).setScale(2, RoundingMode.HALF_UP).doubleValue();
        }
        ProductTax tax = new ProductTax();
        tax.setCountry(Constants.US);
        tax.setRate(rate);
        tax.setRegion(ar.getString(Constants.Code));
        tax.setTaxShip(rate != 0);
        getTaxes().add(tax);
      }
    });
  }
}
