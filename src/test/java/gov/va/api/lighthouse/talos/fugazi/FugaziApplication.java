package gov.va.api.lighthouse.talos.fugazi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class FugaziApplication {

  /** The required main for Spring Boot applications. */
  public static void main(String[] args) {
    SpringApplication.run(FugaziApplication.class, args);
  }
}
