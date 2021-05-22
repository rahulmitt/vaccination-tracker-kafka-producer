package com.rahulmitt.interviewpedia.kafka.vaccination;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VaccinationTrackerApplication {
    public static void main(String[] args) {
        SpringApplication.run(VaccinationTrackerApplication.class);
    }
}
