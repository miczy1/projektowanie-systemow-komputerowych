package com.veriflow.veriflow.service;

import java.util.Random;

public class AuthService {

    private String currentCode;

    public void send2FACode(String email) {
        // Generowanie losowego kodu 4-cyfrowego
        Random rand = new Random();
        int code = 1000 + rand.nextInt(9000);
        this.currentCode = String.valueOf(code);

        // SYMULACJA WYSYŁKI MAILA/SMS
        // W prawdziwej aplikacji użyłbyś JavaMailSender
        System.out.println("\n====================================");
        System.out.println(" [Veriflow Security] 2FA CODE: " + this.currentCode);
        System.out.println(" Sent to: " + email);
        System.out.println("====================================\n");
    }

    public boolean verifyCode(String inputCode) {
        return inputCode != null && inputCode.equals(this.currentCode);
    }
}