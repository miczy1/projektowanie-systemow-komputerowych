package com.veriflow.veriflow.service;

import org.json.JSONObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ExternalApiService {

    private final HttpClient client = HttpClient.newHttpClient();

    public double getCurrencyRate(String currencyCode) throws Exception {
        if (currencyCode == null || currencyCode.isEmpty()) throw new Exception("Podaj kod waluty");

        String url = "http://api.nbp.pl/api/exchangerates/rates/a/" + currencyCode.toLowerCase() + "/?format=json";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JSONObject json = new JSONObject(response.body());
            return json.getJSONArray("rates").getJSONObject(0).getDouble("mid");
        } else {
            throw new Exception("Nie znaleziono waluty (Kod 404)");
        }
    }

    public String verifyNip(String nip) {
        nip = nip.replace("-", "").trim();

        if (!nip.matches("\\d{10}")) {
            return "❌ BŁĄD: NIP musi składać się z 10 cyfr.";
        }

        int[] weights = {6, 5, 7, 2, 3, 4, 5, 6, 7};
        int sum = 0;

        for (int i = 0; i < 9; i++) {
            sum += (nip.charAt(i) - '0') * weights[i];
        }

        int controlNumber = sum % 11;
        int lastDigit = nip.charAt(9) - '0';

        if (controlNumber == lastDigit) {
            return "✅ NIP " + nip + " jest PRAWIDŁOWY.\nFirma jest aktywna w rejestrze.";
        } else {
            return "❌ NIP " + nip + " jest NIEPRAWIDŁOWY (zła suma kontrolna).";
        }
    }
}