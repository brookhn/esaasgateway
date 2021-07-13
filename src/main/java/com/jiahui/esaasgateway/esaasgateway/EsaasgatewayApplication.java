package com.jiahui.esaasgateway.esaasgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@EnableDiscoveryClient
public class EsaasgatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(EsaasgatewayApplication.class, args);
//        ConfigurableApplicationContext applicationContext = SpringApplication.run(EsaasgatewayApplication.class, args);
//        String userName = applicationContext.getEnvironment().getProperty("user.name");
//        String userAge = applicationContext.getEnvironment().getProperty("user.age");
//        System.err.println("user name :"+userName+"; age: "+userAge);
    }

}
