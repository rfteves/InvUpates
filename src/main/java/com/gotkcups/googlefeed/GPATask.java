/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.googlefeed;

import com.google.api.services.content.ShoppingContent;
import com.google.api.services.content.model.Product;
import com.google.api.services.content.model.ProductsCustomBatchRequest;
import com.google.api.services.content.model.ProductsCustomBatchRequestEntry;
import com.google.api.services.content.model.ProductsCustomBatchResponse;
import com.gotkcups.data.Constants;
import com.gotkcups.io.RestHelper;
import com.gotkcups.io.Utilities;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 *
 * @author rfteves
 */
@Component
@Profile("gpa")
public class GPATask implements CommandLineRunner {

  @Autowired
  private UpdateMetafields metas;
  
  @Autowired
  private UpdatePLA plas;
  
  @Autowired
  private UpdateStateTaxes taxes;

  @Override
  public void run(String... strings) throws Exception {
    taxes.process(strings);
    //metas.process(strings);
    plas.process(strings);
  }
}
