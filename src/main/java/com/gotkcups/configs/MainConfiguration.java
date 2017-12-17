/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.configs;

import com.google.api.services.content.model.Product;
import com.gotkcups.sendmail.SendMail;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 *
 * @author rfteves
 */
@Configuration
public class MainConfiguration {

  @Value("${mail.smtp.host}")
  private String smtpHost;

  @Value("${mail.smtp.username}")
  private String smtpUsername;

  @Value("${mail.smtp.password}")
  private String smtpPassword;

  @Value("${mail.smtp.port}")
  private String smtpPort;

  @Value("${gk.timeformat}")
  private String timeformat;

  @Bean
  public AtomicLong counter() {
    return new AtomicLong();
  }

  @Bean
  public SendMail sendMail() {
    SendMail sendMail = new SendMail();
    return new SendMail();
  }

  @Bean
  public SendMailService sendMailService() {
    System.getProperties().setProperty("mail.smtp.host", smtpHost);
    System.getProperties().setProperty("mail.username", smtpUsername);
    System.getProperties().setProperty("mail.password", smtpPassword);
    System.getProperties().setProperty("mail.smtp.port", smtpPort);
    System.getProperties().put("mail.smtp.auth", "true");
    System.getProperties().put("mail.smtp.starttls.enable", "true");
    SendMailService sms = new SendMailService();
    sms.start();
    return sms;
  }

  @Bean
  public SimpleDateFormat getDateConverter() {
    SimpleDateFormat dateConverter = new SimpleDateFormat(timeformat);
    return dateConverter;
  }

  @Bean
  @Scope("prototype")
  public Map<Long, Product> googleModelProductMap() {
    Map<Long, Product> products = new LinkedHashMap<>();
    return products;
  }

  @Bean
  @Scope("prototype")
  public List<Document> documentList() {
    List<Document> list = new ArrayList<>();
    return list;
  }

  @Bean
  @Scope("prototype")
  public Map<String, String> stringMap() {
    return new HashMap<>();
  }

  @Bean
  @Scope("prototype")
  public Map<String, Integer> stringIntMap() {
    return new HashMap<>();
  }

  @Bean
  @Scope("prototype")
  public Date getCurrentDate(int days, boolean midnight) {
    Calendar c = Calendar.getInstance();
    if (midnight) {
      c.set(Calendar.HOUR_OF_DAY, 23);
      c.set(Calendar.MINUTE, 59);
      c.set(Calendar.SECOND, 59);
    } else {
      c.set(Calendar.HOUR_OF_DAY, 0);
      c.set(Calendar.MINUTE, 0);
      c.set(Calendar.SECOND, 1);
    }
    if (days != 0) {
      c.add(Calendar.DATE, days);
    }
    return c.getTime();
  }

  @Bean
  @Scope("prototype")
  public long uniqeTimeInMillis() {
    synchronized (this) {
      try {
        this.wait(1);
      } catch (InterruptedException ex) {
      }
    }
    return System.currentTimeMillis();
  }
}
