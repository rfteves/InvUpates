/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.adhoc;

import com.gotkcups.io.GateWay;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bson.Document;

/**
 *
 * @author Ricardo
 */
public class CustomerList {

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) throws IOException {
    Map<String, String> params = new HashMap<>();
    params.put("fields", "id,email");
    Document resp = GateWay.getAllCustomers("prod", params, 50, -1);
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
