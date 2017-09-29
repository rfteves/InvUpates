/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.tools;

import com.gotkcups.io.RestHttpClient;
import com.gotkcups.io.Utilities;
import com.gotkcups.sendmail.SendMail;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ricardo
 */
public class CheckHello {

  static {
    System.getProperties().setProperty("mail.smtp.host", Utilities.getApplicationProperty("mail.smtp.host"));
    System.getProperties().setProperty("mail.username", Utilities.getApplicationProperty("mail.username"));
    System.getProperties().setProperty("mail.password", Utilities.getApplicationProperty("mail.password"));
    System.getProperties().setProperty("mail.smtp.port", Utilities.getApplicationProperty("mail.smtp.port"));
    System.getProperties().put("mail.smtp.auth", "true");
    System.getProperties().put("mail.smtp.starttls.enable", "true");
  }
  public static void main(String[] args) {
    try {
      SendMail sendEmail = new SendMail("ricardo.teves@gotkcups.com", "ricardo.teves@gotkcups.com",
        "ricardo.teves@gotkcups.com", "Check Hello", "RestHttpService /inv/hello not responding.");
      sendEmail.send();
    } catch (Exception ex1) {
      Logger.getLogger(CheckHello.class.getName()).log(Level.SEVERE, null, ex1);
    }
  }
}
