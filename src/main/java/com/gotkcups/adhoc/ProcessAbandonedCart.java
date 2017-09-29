/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.adhoc;

import com.gotkcups.forms.Login;
import com.gotkcups.io.Utilities;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.NameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author ricardo
 */
public class ProcessAbandonedCart {

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) throws Exception {
    Map<String, String> params = new HashMap<>();
    params.put("login", Utilities.getApplicationProperty("gotkcups.user"));
    params.put("password", Utilities.getApplicationProperty("gotkcups.password"));
    Login login = new Login();
    String html = login.sendGet("https://gotkcups.myshopify.com/admin/auth/login");
    List<NameValuePair> postParams = login.getFormParams(html, params);
    org.bson.Document cookies = login.sendPost("https://gotkcups.myshopify.com/admin/auth/login", postParams);
    html = login.sendGet("https://gotkcups.myshopify.com/admin/checkouts");
    Document abandons = Jsoup.parse(html);
    Elements notsent = abandons.getElementsByClass("badge--status-warning");
    notsent.stream().filter(element-> element.ownText().contains("Not Sent")).forEach(element->{
      System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
      //System.out.println(element.parentNode().parentNode().outerHtml());
      //System.out.println(element.parentNode().parentNode().attr("data-bind-class"));
      String bind = element.parentNode().parentNode().attr("data-bind-class");
      int start = bind.indexOf("[") + 1;
      int end = bind.indexOf("]");
      String cartid = bind.substring(start, end);
      try {
        processCart(login, cartid);
      } catch (Exception ex) {
        Logger.getLogger(ProcessAbandonedCart.class.getName()).log(Level.SEVERE, null, ex);
      }
    });
    int k = 0;
  }

  private final static void processCart(Login login, String cartid) throws Exception {
    String url = String.format("https://gotkcups.myshopify.com/admin/checkouts/%s", cartid);
    String html = login.sendGet(url);
    int j = 0;
  }
}
