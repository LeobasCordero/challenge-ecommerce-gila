package com.gila.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entrypoint class for the Gila E-Commerce Application.
 */
@SpringBootApplication
public class ECommerceApplication {

    /**
     * Start the Spring Boot application context.
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(ECommerceApplication.class, args);
    }
}
