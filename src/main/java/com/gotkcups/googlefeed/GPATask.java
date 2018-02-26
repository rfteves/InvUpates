/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.googlefeed;

import java.util.Arrays;
import jersey.repackaged.com.google.common.base.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 *
 * @author rfteves
 */
@Component
@Profile("gpatask")
public class GPATask implements CommandLineRunner {

  @Autowired
  private UpdateMetafields metas;

  @Autowired
  private UpdatePLA plas;

  @Autowired
  private UpdateStateTaxes taxes;

  @Override
  public void run(String... strings) throws Exception {
    boolean meta = Optional.of(Arrays.asList(strings).stream().filter(arg -> arg.equals("-meta")).findFirst().isPresent()).orNull();
    if (meta) {
      metas.process(strings);
    } else {
      taxes.process(strings);
      plas.process(strings);
    }
  }
}
