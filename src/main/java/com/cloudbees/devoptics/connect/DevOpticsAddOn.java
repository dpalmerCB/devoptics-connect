package com.cloudbees.devoptics.connect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DevOpticsAddOn {

    public static void main(String[] args) throws Exception {
        new SpringApplication(DevOpticsAddOn.class).run(args);
    }
}
