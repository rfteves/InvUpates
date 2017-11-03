/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.adhoc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author Ricardo
 */
@Configuration
public abstract class AbstractCLR implements CommandLineRunner {

  @Autowired
  ApplicationContext context;

  @Override
  public void run(String... args) throws Exception {
    System.out.println("Abstract CLR run");
    process(args);
  }

  public abstract void process(String... args) throws Exception;
}
