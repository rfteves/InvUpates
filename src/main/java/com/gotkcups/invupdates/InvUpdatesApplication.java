package com.gotkcups.invupdates;

import com.gotkcups.io.GateWay;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

@SpringBootApplication
/*public class InvUpdatesApplication {

  public static void main(String[] args) {
    SpringApplication.run(InvUpdatesApplication.class, args);
  }
  
  
}*/
public class InvUpdatesApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(InvUpdatesApplication.class);
    }

    public static void main(String[] args) throws Exception {
      GateWay.init();
        SpringApplication.run(InvUpdatesApplication.class, args);
    }

}
