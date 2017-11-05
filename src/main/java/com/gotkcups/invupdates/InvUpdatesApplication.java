package com.gotkcups.invupdates;

import com.gotkcups.io.GateWay;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
//@ComponentScan({"com.gotkcups.adhoc"})
/*public class InvUpdatesApplication {

  public static void main(String[] args) {
    //SpringApplication.run(InvUpdatesApplication.class, args);
    //GateWay.init();
    new SpringApplicationBuilder(InvUpdatesApplication.class).web(false).run(args);
  }
}*/
public class InvUpdatesApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(InvUpdatesApplication.class);
    }

    public static void main(String[] args) throws Exception {
      GateWay.init();
        //SpringApplication.run(InvUpdatesApplication.class, args);
        new SpringApplicationBuilder(InvUpdatesApplication.class).web(false).run(args);
    }

}
