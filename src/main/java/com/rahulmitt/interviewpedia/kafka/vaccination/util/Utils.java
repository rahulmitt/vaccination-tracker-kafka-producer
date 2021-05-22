package com.rahulmitt.interviewpedia.kafka.vaccination.util;

import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;

public class Utils {

    public static void halt(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }

    public static MultiValueMap<String, String> getHeaders() {
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("accept", "*/*");
        headers.add("accept-language", "en-US,en;q=0.9,hi;q=0.8");
        headers.add("dnt", "1");
        headers.add("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.93 Safari/537.36");
        return headers;
    }
}
