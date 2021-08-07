package com.mslinksya.pets.io.data.model;

import java.util.StringJoiner;

public class RegistrationModel {
    private final String wifi_ssid;
    private final String wifi_pass;
    private final String request_id;
    
    private static final String delimiter = ";";

    public RegistrationModel(String wifi_ssid, String wifi_pass, String request_id) {
        this.wifi_ssid = escapeString(wifi_ssid);
        this.wifi_pass = escapeString(wifi_pass);
        this.request_id = escapeString(request_id);
    }

    private String escapeString(String s) {
        return s.replaceAll(delimiter, "\\" + delimiter);
    }

    public String getFormattedData() {
        return new StringJoiner(delimiter)
                .add(wifi_ssid)
                .add(wifi_pass)
                .add(request_id)
                .toString();
    }
}
