package com.wesolve;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement(order = 0)
public class WeSolveApplication {

  public static void main(String[] args) {
    SpringApplication.run(WeSolveApplication.class, args);
  }
}
