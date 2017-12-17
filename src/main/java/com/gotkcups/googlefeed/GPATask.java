/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.googlefeed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 *
 * @author rfteves
 */
@Component
@Profile("prod")
public class GPATask implements CommandLineRunner {

  @Autowired
  private UpdateMetafields metas;
  
  @Autowired
  private UpdatePLA plas;
  
  @Autowired
  private UpdateStateTaxes taxes;

  @Override
  public void run(String... strings) throws Exception {
    //taxes.process(strings);
    //metas.process(strings);
    //plas.process(strings);
  }
}
