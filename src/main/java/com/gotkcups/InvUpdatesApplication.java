package com.gotkcups;

import com.gotkcups.io.GateWay;
import java.util.Arrays;
import jersey.repackaged.com.google.common.base.Optional;
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
    boolean nowebapp = Optional.of(Arrays.asList(args).stream().filter(arg->arg.equals("-nowebapp")).findFirst().isPresent()).orNull();
    new SpringApplicationBuilder(InvUpdatesApplication.class).web(!nowebapp).run(args);
  }
}*/
public class InvUpdatesApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(InvUpdatesApplication.class);
    }

    public static void main(String[] args) throws Exception {
      //args = new String[0];
      boolean nowebapp = Optional.of(Arrays.asList(args).stream().filter(arg->arg.equals("-nowebapp")).findFirst().isPresent()).orNull();
      
      //Arrays.asList(args).stream().filter(arg->arg.equals("-nowebapp")).forEach(nowebapp=true;);
      //GateWay.init();
        //SpringApplication.run(InvUpdatesApplication.class, args);
        new SpringApplicationBuilder(InvUpdatesApplication.class).web(!nowebapp).run(args);
    }
}
