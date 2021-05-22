package com.rahulmitt.interviewpedia.kafka.vaccination.model;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Session {
    private String date;
    private int available_capacity;
    private int min_age_limit;
    private String vaccine;
    private String[] slots;

    public boolean hasAvailability() {
        return available_capacity > 0;
    }
}
