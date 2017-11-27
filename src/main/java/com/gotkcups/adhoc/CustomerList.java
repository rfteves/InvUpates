/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.adhoc;

import com.gotkcups.io.GateWay;
import com.gotkcups.io.RestHelper;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 *
 * @author Ricardo
 */
@Component
@Profile("check")
public class CustomerList implements CommandLineRunner {

  @Autowired
  protected RestHelper restHelper;
  
  @Override
  public void run(String... strings) throws Exception {
    Map<String, String> params = new HashMap<>();
    params.put("fields", "id,email");
    Document resp = restHelper.getAllCustomers(params, 50, -1);
    List<Document> customers = (List) resp.get("customers");
    FileOutputStream fos = new FileOutputStream("gotkcups.customers.txt");
    for (Document customer : customers) {
      System.out.println(customer.getString("email"));
      fos.write(customer.getString("email").getBytes());
      fos.write("\n".getBytes());
    }
    fos.flush();
    fos.close();
  }
  
}
