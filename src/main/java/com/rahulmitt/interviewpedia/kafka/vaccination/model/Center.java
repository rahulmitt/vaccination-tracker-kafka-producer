package com.rahulmitt.interviewpedia.kafka.vaccination.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Center {
    private String name;
    private String address;
    private String state_name;
    private String district_name;
    private String block_name;
    private int pincode;
    private String fee_type;
    private Session[] sessions;
}
