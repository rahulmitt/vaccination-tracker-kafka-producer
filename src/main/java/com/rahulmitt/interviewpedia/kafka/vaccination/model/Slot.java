package com.rahulmitt.interviewpedia.kafka.vaccination.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Slot {
    private String centerName;
    private String address;
    private String state;
    private String district;
    private String blockName;
    private int pinCode;
    private String feeType;
    private String date;
    private int availableCapacity;
    private int minAge;
}
