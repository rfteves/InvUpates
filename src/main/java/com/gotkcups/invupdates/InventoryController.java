/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.invupdates;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author ricardo
 */
@RestController
@RequestMapping("/inv")
public class InventoryController {
  
  /*@RequestMapping(method = GET)
  public List<Object> list() {
    return null;
  }
  
  @RequestMapping(value = "/{id}", method = GET)
  public Object get(@PathVariable String id) {
    return null;
  }
  
  @RequestMapping(value = "/{id}", method = PUT)
  public ResponseEntity<?> put(@PathVariable String id, @RequestBody Object input) {
    return null;
  }
  
  @RequestMapping(value = "/{id}", method = POST)
  public ResponseEntity<?> post(@PathVariable String id, @RequestBody Object input) {
    return null;
  }
  
  @RequestMapping(value = "/{id}", method = DELETE)
  public ResponseEntity<Object> delete(@PathVariable String id) {
    return null;
  }*/

  @RequestMapping("/hello")
  public Hello greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
    return new Hello(counter.incrementAndGet(),
            String.format(template, name));
  }
  private static final String template = "Hello, %s!";
  private final AtomicLong counter = new AtomicLong();
  
  
  
  @RequestMapping("/info")
  public void update(@RequestParam(value = "json", defaultValue = "{\"vendors\": []") String json) {
    System.out.println("json is" + json);
  }
  
}
