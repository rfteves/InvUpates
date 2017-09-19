/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.tools;

import com.gotkcups.io.RestHttpClient;
import com.gotkcups.sendmail.SendMail;

/**
 *
 * @author Ricardo
 */
public class CheckHello {

    public static void main(String[] args) {
        try {
            String retval = RestHttpClient.processGet("http://tools.gotkcups.com/inv/hello");
            System.out.println(retval);
        } catch (Exception ex) {
            SendMail sendEmail = new SendMail("ricardo@drapers.com", "ricardo@drapers.com",
                "ricardo@drapers.com", "Check Hello", "RestHttpService /inv/hello not responding.");
        }
    }
}
