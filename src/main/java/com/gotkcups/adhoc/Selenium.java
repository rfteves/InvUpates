/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.adhoc;

import com.gotkcups.data.Constants;
import com.gotkcups.io.GateWay;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.bson.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 *
 * @author rfteves
 */
@Component
@Profile("selene")
public class Selenium extends AbstractCLR {
  @Value("${srm.send_mail}")
  private String srmuser;
  
  @Override
  public void process(String... args) throws Exception {
    System.setProperty("webdriver.chrome.driver", "c:/Users/ricardo/JavaLibraries/chromedriver_win32/chromedriver.exe");
    ChromeOptions options = new ChromeOptions();
    options.addArguments("disable-infobars");
    WebDriver driver = new ChromeDriver(options);
    System.out.println("YYYYYYYYYYYYYYYYYYY " + srmuser);
    Map<String, String> params = new HashMap<>();
    params.put("fields", "id,title,variants");
    Set<Document> sorted = new TreeSet<>();
    Document resp = GateWay.getAllProducts("prod", params, 150, -1);
    List<Document> products = (List) resp.get("products");
    for (Document product : products) {
      if (!(product.getLong("id") == 292957913111L
        || product.getLong("id") == 9760556810993399l
        || product.getLong("id") == 933507564170339999l)) {
        continue;
      }
      String metas = GateWay.getProductMetafields("prod", product.getLong(Constants.Id));
      //Document metafieds = (List)Document.parse(metas).get(Constants.Metafields);
      driver.get("https://www.costco.com/LogonForm");
      driver.findElement(By.id("logonId")).sendKeys("evelyn1968@teves.us");
      driver.findElement(By.id("logonPassword")).sendKeys("Nji9Bhu8");
      driver.findElement(By.xpath(".//*[contains(@class,'tabable')]")).click();
      driver.findElement(By.className("primary")).click();
    }

  }

}
