package com.rahulmitt.interviewpedia.kafka.vaccination.controller;

import com.google.gson.Gson;
import com.rahulmitt.interviewpedia.kafka.vaccination.model.Center;
import com.rahulmitt.interviewpedia.kafka.vaccination.model.Centers;
import com.rahulmitt.interviewpedia.kafka.vaccination.model.Session;
import com.rahulmitt.interviewpedia.kafka.vaccination.model.Slot;
import com.rahulmitt.interviewpedia.kafka.vaccination.util.Beeper;
import com.rahulmitt.interviewpedia.kafka.vaccination.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@RestController
public class CowinTrackerController {
    private static final DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final RestTemplate restTemplate;

    @Autowired
    private KafkaTemplate<Integer, Slot> kafkaTemplate;

    private static final String TOPIC = "first_topic";

//    private final Integer[] pinCodes = {560017, 560037, 560066};
    private final Integer[] pinCodes = {560037};

    @Value("${covin.api.url}")
    private String baseUrl;

    private final Set<Slot> publishedSlots = new HashSet<>();

    public CowinTrackerController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Scheduled(fixedRateString = "${fixedRate:5000}")
    public void pollForAvailableSlots() {
        LocalDate[] dates = {LocalDate.now(), LocalDate.now().plusDays(1)};

        for (int pinCode : pinCodes) {
            for(LocalDate d : dates) {
                List<Slot> availableSlots = checkAvailableSlot(pinCode, d.format(format));

                if (availableSlots.size() > 0) publishToKafka(availableSlots);
//                else log.info("{} : No slots available for {}", pinCode, d.format(format));
                Utils.halt(5000);
            }
//            System.out.println();
        }
//        log.info("------------------------------------------");
        System.out.print(".");
    }

    private List<Slot> checkAvailableSlot(int pinCode, String date) {
        HttpEntity<String> entity = new HttpEntity<>("parameters", Utils.getHeaders());
        String url = String.format(baseUrl, pinCode, date);

        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        Gson gson = new Gson();
        Centers centers = gson.fromJson(responseEntity.getBody(), Centers.class);

        List<Slot> availableSlots = new ArrayList<>();
        List<Slot> unavailableSlots = new ArrayList<>();
        for (Center center : centers.getCenters()) {
            for (Session session : center.getSessions()) {
                Slot slot = new Slot(
                        center.getName(),
                        center.getAddress(),
                        center.getState_name(),
                        center.getDistrict_name(),
                        center.getBlock_name(),
                        center.getPincode(),
                        center.getFee_type(),
                        session.getDate(),
                        session.getAvailable_capacity(),
                        session.getMin_age_limit()
                );

                if (session.hasAvailability()) {
                    availableSlots.add(slot);
                } else {
                    unavailableSlots.add(slot);
                }
            }
        }

        return availableSlots;
    }

    private void publishToKafka(List<Slot> availableSlots) {
        for (Slot slot : availableSlots) {
            if (publishedSlots.contains(slot)) continue;
            publishedSlots.add(slot);
            Beeper.beep(800, 100);

            int key = slot.getPinCode();
            ProducerRecord<Integer, Slot> record = new ProducerRecord<>(TOPIC, key, slot);
            ListenableFuture<SendResult<Integer, Slot>> future = kafkaTemplate.send(record);
            future.addCallback(new ListenableFutureCallback<SendResult<Integer, Slot>>() {
                @Override
                public void onFailure(Throwable throwable) {
                    log.error("unable to send message= {}", record, throwable);
                }

                @Override
                public void onSuccess(SendResult<Integer, Slot> integerSlotSendResult) {
                    RecordMetadata recordMetadata = integerSlotSendResult.getRecordMetadata();
                    log.info(
                            "\nMinAge: {}, Pincode: {}, Center Name: {} :: Received metadata :: Topic: {}, Partition: {}, Offset: {}, Timestamp: {}",
                            slot.getMinAge(), slot.getPinCode(), slot.getCenterName(),
                            recordMetadata.topic(),
                            recordMetadata.partition(),
                            recordMetadata.offset(),
                            recordMetadata.timestamp()
                    );
                }
            });
        }
    }
}
