# Veriflow - Enterprise Verification System

**Veriflow** to nowoczesna aplikacja desktopowa typu ERP, zaprojektowana do weryfikacji kontrahentów (NIP) oraz analizy rynków walutowych. Projekt demonstruje wykorzystanie architektury asynchronicznej z użyciem kolejkowania wiadomości (JMS), nowoczesnego interfejsu JavaFX (AtlantaFX) oraz integracji z zewnętrznymi systemami REST API.

---

## 🛠 Stos Technologiczny

Projekt został zrealizowany przy użyciu następujących technologii i bibliotek:

* **Java SDK:** 17+ (Zgodność z LTS)
* **UI Framework:** JavaFX 21 + **AtlantaFX** (Primer Dark Theme)
* **Message Broker:** Apache ActiveMQ 5.17.6 (uruchamiany w Dockerze)
* **Komunikacja:** JMS (Java Message Service) - wzorzec Producer/Consumer
* **Baza Danych:** H2 Database (Embedded/File-based)
* **Build Tool:** Maven
* **Integracje:**
    * NBP Web API (REST/JSON)
    * Algorytmika walidacji sumy kontrolnej NIP

---

## 🏛 Architektura Systemu

Aplikacja działa w oparciu o architekturę modułową, separującą warstwę prezentacji od logiki biznesowej i usług integracyjnych.

### Schemat Komunikacji Asynchronicznej (2FA Flow)

Kluczowym elementem projektu jest implementacja mechanizmu **JMS** do obsługi procesów długotrwałych (symulacja wysyłki kodów autoryzacyjnych), aby nie blokować głównego wątku interfejsu (UI Thread).

```text
+----------------+          +-------------------+          +---------------------+
|   JavaFX UI    |          |    AuthService    |          |  ActiveMQ (Docker)  |
| (Wątek główny) |          | (Logika biznesowa)|          | (TCP: 61616)        |
+-------+--------+          +---------+---------+          +----------+----------+
        |                             |                               |
        | 1. Kliknięcie "Zaloguj"     |                               |
        +---------------------------->|                               |
        |                             | 2. Wyślij wiadomość (Producer)|
        |                             +------------------------------>|
        |                             |                               | [Kolejka: veriflow.2fa.queue]
        |                             |                               |
        | <--- UI NIE JEST ZABLOKOWANE                                |
        |                             |                               |
                                                                      |
                                           +--------------------------+----------+
                                           |                                     |
                                           | 3. Pobierz wiadomość (Consumer)     |
                                           v                                     |
                                 +---------+---------+                           |
                                 |    JmsConsumer    |                           |
                                 | (Wątek w tle)     |                           |
                                 +---------+---------+                           |
                                           |                                     |
                                           | 4. Symulacja wysyłki (Thread.sleep) |
                                           v                                     |
                                 +---------+---------+                           |
                                 |  Log w Konsoli    | <-------------------------+
                                 |  "KOD: XXXX"      |
                                 +-------------------+