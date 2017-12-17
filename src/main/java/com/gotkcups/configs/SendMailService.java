/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.configs;

import com.gotkcups.sendmail.SendMail;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 *
 * @author rfteves
 */
@Component
public class SendMailService extends Thread {
  @Autowired
  private List<SendMail> mailList;

  @Override
  public void run() {
    SendMail mail = null;
    while (true) {
      synchronized (this) {
        while (mailList != null && mailList.isEmpty()) {
          try {
            System.out.println("wait mail " + mailList.size());
            this.wait();
          } catch (InterruptedException ex) {
          }
        }
        if (mailList != null) {
          mail = mailList.remove(0);
        }
      }
      if (mail != null) {
        try {
          System.out.println("send mail");
          mail.send();
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    }
  }

  public void add(SendMail mail) {
    synchronized (this) {
      System.out.println("add mail");
      mailList.add(mail);
      System.out.println("add mail " + mailList.size());
      this.notifyAll();
    }

  }

  @Bean
  public List<SendMail> createMailList() {
    return new ArrayList<>();
  }
}
