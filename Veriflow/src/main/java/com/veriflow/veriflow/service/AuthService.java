package com.veriflow.veriflow.service;

import java.util.Random;

public class AuthService {

    private String currentCode;
    private JmsService jmsService;

    public void setJmsService(JmsService jmsService) {
        this.jmsService = jmsService;
    }

    public void send2FACode(String email) {
        Random rand = new Random();
        int code = 1000 + rand.nextInt(9000);
        this.currentCode = String.valueOf(code);

        String messagePayload = "EMAIL=" + email + ";CODE=" + this.currentCode;

        if (jmsService != null) {
            jmsService.sendMessage(messagePayload);
        } else {
            System.err.println("Błąd: JmsService nie jest podłączony!");
        }
    }

    public boolean verifyCode(String inputCode) {
        return inputCode != null && inputCode.equals(this.currentCode);
    }
}