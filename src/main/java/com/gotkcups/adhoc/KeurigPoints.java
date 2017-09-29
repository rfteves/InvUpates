/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.adhoc;

import com.gotkcups.forms.Login;
import com.gotkcups.io.Utilities;
import java.util.HashMap;
import java.util.Arrays;
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
public class KeurigPoints {

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) throws Exception {
    String[]prefixes = {"client", "buyer"};
    Arrays.asList(prefixes).stream().forEach(prefix->{
      try {
        KeurigPoints.userPrefix(prefix);
      } catch (Exception ex) {
        Logger.getLogger(KeurigPoints.class.getName()).log(Level.SEVERE, null, ex);
      }
    });
  }
  
  private static void userPrefix(String prefix) throws Exception {
    
    Map<String,String> params = new HashMap<>();
    params.put("j_password", Utilities.getApplicationProperty("keurig.password"));
    Login login = new Login();
    for (int i = 1; i < 25; i++) {
      params.put("j_username", String.format("%s%s@acwnn.org", prefix, "" + i));
      String html = login.sendGet("https://www.keurig.com/login");
      List<NameValuePair> postParams = login.getFormParams(html, params);
      org.bson.Document cookies = login.sendPost("https://www.keurig.com/j_spring_security_check", postParams);
      if (cookies.getInteger("response-code") == 302) {
        // Nice we are in.
        html = login.sendGet("https://www.keurig.com/my-account");
        Document doc = Jsoup.parse(html);
        Elements points = doc.getElementsByClass("points-holder");
        if (points.size() == 1) {
          Elements span = points.get(0).getElementsByTag("span");
          System.out.print(params.get("j_username"));
          System.out.print(",");
          span.stream().filter(k-> k.attr("class").equals("number")).map(k-> k.ownText()).forEach(System.out::println);
        }
      }
      login.sendGet("https://www.keurig.com/logout");
    }
  }
  
}
