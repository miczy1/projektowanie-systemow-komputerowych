# Veriflow - Enterprise Verification System

**Veriflow** to nowoczesna aplikacja desktopowa typu ERP (Enterprise Resource Planning), służąca do weryfikacji kontrahentów (NIP) oraz analizy rynków walutowych.

Projekt stanowi przykład profesjonalnej implementacji w języku Java, łącząc nowoczesny interfejs graficzny (JavaFX) z architekturą rozproszoną opartą na asynchronicznym przetwarzaniu komunikatów (JMS) oraz konteneryzacji (Docker).

---

## 🛠 Stos Technologiczny

Projekt został zrealizowany przy użyciu następujących technologii i bibliotek:

* **Język:** Java 17+ (LTS)
* **Interfejs Użytkownika:** JavaFX 21 + **AtlantaFX** (Primer Dark Theme - styl GitHub)
* **Message Broker:** Apache ActiveMQ 5.17.6 (uruchamiany jako kontener Docker)
* **Komunikacja:** JMS (Java Message Service) - model Producer/Consumer
* **Baza Danych:** H2 Database (Embedded/File-based)
* **Zarządzanie Projektem:** Maven
* **Integracje Zewnętrzne:**
    * NBP Web API (REST/JSON) - pobieranie kursów walut
    * Algorytmika walidacji sumy kontrolnej numerów NIP

---

## 🏛 Architektura Systemu

Aplikacja realizuje wzorzec **Composite View** (główny widok zarządza podwidokami) oraz wykorzystuje **przetwarzanie asynchroniczne** dla zadań długotrwałych (np. wysyłka kodów 2FA), co zapobiega blokowaniu interfejsu użytkownika.

### Schemat Komunikacji Asynchronicznej (2FA Flow)

Poniższy diagram przedstawia przepływ danych podczas logowania z weryfikacją dwuetapową:

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
```                                 
# 🚀 Instrukcja Uruchomienia Systemu

Aby poprawnie uruchomić system, wykonaj poniższe kroki **w podanej kolejności**.

---

## 1. Wymagania wstępne

Upewnij się, że masz zainstalowane:

* **Java JDK 17 lub nowsze** (zalecane **JDK 21**)
* **Docker Desktop** (do uruchomienia brokera wiadomości)
* **Maven** (do budowania projektu)

---

## 2. Uruchomienie Brokera Wiadomości (Docker)

Aplikacja wymaga działającego serwera **ActiveMQ**, który należy uruchomić jako kontener Docker.

Otwórz terminal (PowerShell / Bash) i wykonaj polecenie:

```bash
docker run -d --name activemq \
  -p 61616:61616 \
  -p 8161:8161 \
  apache/activemq-classic
```

### Używane porty

* **61616** – port TCP dla komunikacji **JMS** (wykorzystywany przez aplikację Java)
* **8161** – panel administratora ActiveMQ

Panel administracyjny dostępny pod adresem:

```
http://localhost:8161
```

Dane logowania:

* **login:** admin
* **hasło:** admin

---

## 3. Kompilacja Projektu

W głównym katalogu projektu (tam, gdzie znajduje się plik `pom.xml`) uruchom:

```bash
mvn clean install
```

Polecenie pobierze wszystkie zależności i zbuduje aplikację.

---

## 4. Uruchomienie Aplikacji (WAŻNE ⚠️)

Ze względu na specyfikę modułów **Java 9+** oraz **JavaFX**, aplikację należy uruchamiać **wyłącznie** przez klasę pomocniczą **Launcher**.

> ❗ **BARDZO WAŻNE:**
> **Nie uruchamiaj bezpośrednio klasy `App.java`!**

### Metoda A: IntelliJ IDEA (zalecana)

1. Rozwiń strukturę projektu:

   ```
   src/main/java/com/veriflow
   ```
2. Odszukaj plik **Launcher.java**
3. Kliknij prawym przyciskiem myszy (lub zielony trójkąt obok metody `main`)
4. Wybierz **Run 'Launcher.main()'**

### Metoda B: Maven (terminal)

Będąc w katalogu projektu, wykonaj:

```bash
mvn javafx:run
```

---

## 🔑 Scenariusz Testowy (Instrukcja Obsługi)

### Informacje ogólne

* Baza danych **H2 (plikowa)** tworzona jest automatycznie
* Lokalizacja: **katalog domowy użytkownika**
* Tworzenie następuje przy **pierwszym uruchomieniu aplikacji**

---

### Krok 1: Logowanie

1. Uruchom aplikację
2. Wpisz dane:

    * **Login:** `admin`
    * **Hasło:** `admin123`
3. Kliknij **„Zaloguj się”**

---

### Krok 2: Weryfikacja 2FA (Test JMS)

1. Po zalogowaniu aplikacja wysyła **asynchroniczne żądanie JMS** do ActiveMQ
2. Sprawdź konsolę IDE (**Run / Output**)
3. Zobaczysz log symulujący SMS:

```
📥 [JMS Consumer] Odebrano:
EMAIL=admin@veriflow.com; CODE=4521
```

4. Przepisz kod (np. `4521`) do aplikacji
5. Zatwierdź

---

### Krok 3: Funkcjonalności Główne

#### 💱 Kursy Walut

1. Wybierz z menu **„Kursy Walut”**
2. Wpisz kod waluty (np. `EUR`, `USD`, `CHF`)
3. Kliknij **„Pobierz kurs”**

➡ Dane pobierane są **na żywo z API NBP**

---

#### 🧾 Weryfikacja NIP

1. Wybierz z menu **„Weryfikacja NIP”**
2. Wpisz przykładowy numer NIP, np.:

   ```
   5252674798
   ```
3. Kliknij **„Weryfikuj”**

➡ System sprawdzi poprawność **cyfry kontrolnej**

---

## ⚠️ Rozwiązywanie Problemów (Troubleshooting)

| Problem                                 | Możliwa przyczyna                               | Rozwiązanie                                                                     |
| --------------------------------------- | ----------------------------------------------- | ------------------------------------------------------------------------------- |
| **Connection Refused (JMS)**            | Kontener Docker nie działa lub port jest zajęty | Sprawdź `docker ps`. Jeśli kontener nie działa, uruchom `docker start activemq` |
| **JavaFX Runtime Components Missing**   | Uruchomiono klasę `App.java`                    | Uruchom **Launcher.java** – to kluczowe                                         |
| **ClassNotFoundException: ActiveMQ**    | Zła wersja zależności w `pom.xml`               | Wymagana wersja **5.17.6+** (obsługa `jakarta.jms` / `javax.jms`)               |
| **Błędy SLF4J / Log4j**                 | Brak zależności logowania                       | Wykonaj `mvn clean install` (wymagane `log4j-core` i `log4j-slf4j-impl`)        |
| **Aplikacja zawiesza się przy starcie** | Konflikt modułów Java                           | Usuń plik `src/main/java/module-info.java`, jeśli został wygenerowany           |

---

✅ Po wykonaniu wszystkich kroków aplikacja powinna działać poprawnie.
