package com.gymmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the Gym Membership Management System.
 *
 * This enterprise application provides full lifecycle management for a gym:
 * member onboarding, plans, payments, attendance (QR check-in/out),
 * trainers, equipment, notifications and analytics reporting.
 */
@SpringBootApplication
@EnableScheduling
public class GymManagementSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(GymManagementSystemApplication.class, args);
    }
}
