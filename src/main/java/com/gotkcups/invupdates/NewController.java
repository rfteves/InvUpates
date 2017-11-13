/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.invupdates;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author rfteves
 */
@Controller
public class NewController {
  
  @RequestMapping("/url")
  public String page(Model model) {
    model.addAttribute("attribute", "value");
    return "view.name";
  }
  
}
