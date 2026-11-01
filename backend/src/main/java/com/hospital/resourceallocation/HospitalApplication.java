package com.hospital.resourceallocation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Government Hospital Resource Allocation System (GHRAS)
 * Main Spring Boot Application Entry Point
 */
@SpringBootApplication
@EnableTransactionManagement
public class HospitalApplication {

    public static void main(String[] args) {
        SpringApplication.run(HospitalApplication.class, args);
        System.out.println("======================================================================");
        System.out.println(" GHRAS - Government Hospital Resource Allocation System Started");
        System.out.println(" REST API Server running on: http://localhost:8080");
        System.out.println("======================================================================");
    }
}
