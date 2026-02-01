package com.veriflow.veriflow;

import atlantafx.base.theme.PrimerDark;
import com.veriflow.veriflow.database.DatabaseManager;
import com.veriflow.veriflow.service.AuthService;
import com.veriflow.veriflow.service.ExternalApiService;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class App extends Application {

    private final DatabaseManager dbManager = new DatabaseManager();
    private final AuthService authService = new AuthService();
    private final ExternalApiService apiService = new ExternalApiService();

    private Stage primaryStage;
    private BorderPane mainLayout;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());

        stage.setTitle("Veriflow - Enterprise System");
        stage.setWidth(1000);
        stage.setHeight(700);

        showLoginScreen();

        stage.centerOnScreen();
        stage.show();
    }

    private void showLoginScreen() {
        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(380);
        card.setPadding(new Insets(40));
        card.setStyle("-fx-background-color: -color-bg-default; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 15, 0, 0, 5);");

        Text logo = new Text("Veriflow");
        logo.setFont(Font.font("Inter", FontWeight.BOLD, 36));
        logo.setFill(Color.web("#ffffff"));

        Label subtitle = new Label("Zaloguj się do panelu przedsiębiorcy");
        subtitle.setStyle("-fx-text-fill: -color-fg-muted;");

        TextField userField = new TextField();
        userField.setPromptText("Użytkownik (admin)");

        PasswordField passField = new PasswordField();
        passField.setPromptText("Hasło (admin123)");

        Button loginBtn = new Button("Zaloguj się");
        loginBtn.setDefaultButton(true);
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.getStyleClass().add("accent");

        loginBtn.setOnAction(e -> {
            String email = dbManager.getUserEmail(userField.getText(), passField.getText());
            if (email != null) {
                authService.send2FACode(email);
                show2FAScreen(email);
            } else {
                showAlert(Alert.AlertType.ERROR, "Błąd logowania", "Nieprawidłowy login lub hasło.");
            }
        });

        card.getChildren().addAll(logo, subtitle, new Separator(), userField, passField, loginBtn);

        StackPane root = new StackPane(card);
        root.setStyle("-fx-background-color: -color-bg-inset;");
        primaryStage.setScene(new Scene(root));
    }

    private void show2FAScreen(String email) {
        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(380);
        card.setPadding(new Insets(40));
        card.setStyle("-fx-background-color: -color-bg-default; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 15, 0, 0, 5);");

        Text title = new Text("Weryfikacja 2FA");
        title.setFont(Font.font("Inter", FontWeight.BOLD, 24));
        title.setFill(Color.WHITE);

        Label info = new Label("Kod został wysłany na adres:\n" + email);
        info.setWrapText(true);
        info.setStyle("-fx-text-alignment: center; -fx-text-fill: -color-fg-muted;");

        TextField codeField = new TextField();
        codeField.setPromptText("0000");
        codeField.setAlignment(Pos.CENTER);
        codeField.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-max-width: 150px;");

        Button verifyBtn = new Button("Potwierdź kod");
        verifyBtn.setDefaultButton(true);
        verifyBtn.setMaxWidth(Double.MAX_VALUE);
        verifyBtn.getStyleClass().add("success");

        verifyBtn.setOnAction(e -> {
            if (authService.verifyCode(codeField.getText())) {
                showMainDashboard();
            } else {
                showAlert(Alert.AlertType.WARNING, "Błąd", "Nieprawidłowy kod weryfikacyjny.");
            }
        });

        Label hint = new Label("(Sprawdź kod w konsoli IntelliJ)");
        hint.setStyle("-fx-font-size: 10px; -fx-text-fill: -color-fg-subtle;");

        card.getChildren().addAll(title, info, codeField, hint, verifyBtn);

        StackPane root = new StackPane(card);
        root.setStyle("-fx-background-color: -color-bg-inset;");
        primaryStage.getScene().setRoot(root);
    }

    private void showMainDashboard() {
        mainLayout = new BorderPane();

        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(260);
        sidebar.setStyle("-fx-background-color: -color-bg-subtle; -fx-border-color: transparent -color-border-muted transparent transparent;");

        Text brand = new Text("Veriflow");
        brand.setFont(Font.font("Inter", FontWeight.BOLD, 22));
        brand.setFill(Color.web("#ffffff"));

        Button btnCurrency = createMenuButton("Kursy Walut");
        Button btnNip = createMenuButton("Weryfikacja NIP");
        Button btnLogout = createMenuButton("Wyloguj się");
        btnLogout.getStyleClass().add("danger");

        btnCurrency.setOnAction(e -> mainLayout.setCenter(createCurrencyView()));
        btnNip.setOnAction(e -> mainLayout.setCenter(createNipView()));
        btnLogout.setOnAction(e -> showLoginScreen());

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        sidebar.getChildren().addAll(brand, new Separator(), btnCurrency, btnNip, spacer, btnLogout);

        mainLayout.setLeft(sidebar);
        mainLayout.setCenter(createCurrencyView());

        primaryStage.getScene().setRoot(mainLayout);
    }

    private VBox createCurrencyView() {
        VBox view = new VBox(25);
        view.setPadding(new Insets(50));
        view.setAlignment(Pos.TOP_LEFT);

        Label header = new Label("Aktualne Kursy Walut");
        header.setFont(Font.font("Inter", FontWeight.BOLD, 28));

        HBox searchBox = new HBox(10);
        TextField currencyInput = new TextField();
        currencyInput.setPromptText("Kod waluty (np. USD, EUR)");
        Button searchBtn = new Button("Pobierz kurs");
        searchBtn.setDefaultButton(true);
        searchBox.getChildren().addAll(currencyInput, searchBtn);

        VBox resultCard = new VBox(10);
        resultCard.setPadding(new Insets(20));
        resultCard.setStyle("-fx-background-color: -color-bg-subtle; -fx-background-radius: 10; -fx-border-color: -color-border-muted; -fx-border-radius: 10;");
        resultCard.setMaxWidth(400);

        Label resultLabel = new Label("---");
        resultLabel.setStyle("-fx-font-size: 40px; -fx-font-weight: bold; -fx-text-fill: -color-accent-fg;");
        Label descLabel = new Label("Średni kurs NBP");

        searchBtn.setOnAction(e -> {
            try {
                double rate = apiService.getCurrencyRate(currencyInput.getText());
                resultLabel.setText(String.format("%.4f PLN", rate));
                descLabel.setText("Średni kurs NBP dla: " + currencyInput.getText().toUpperCase());
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Błąd API", ex.getMessage());
            }
        });

        resultCard.getChildren().addAll(descLabel, resultLabel);
        view.getChildren().addAll(header, new Label("Pobieraj dane bezpośrednio z Narodowego Banku Polskiego."), searchBox, resultCard);
        return view;
    }

    private VBox createNipView() {
        VBox view = new VBox(25);
        view.setPadding(new Insets(50));
        view.setAlignment(Pos.TOP_LEFT);

        Label header = new Label("Weryfikacja Kontrahenta");
        header.setFont(Font.font("Inter", FontWeight.BOLD, 28));

        HBox searchBox = new HBox(10);
        TextField nipInput = new TextField();
        nipInput.setPromptText("Wpisz numer NIP");
        Button verifyBtn = new Button("Weryfikuj firmę");
        verifyBtn.setDefaultButton(true);
        searchBox.getChildren().addAll(nipInput, verifyBtn);

        TextArea resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setPrefHeight(150);
        resultArea.setMaxWidth(500);
        resultArea.setPromptText("Tu pojawią się wyniki weryfikacji...");

        verifyBtn.setOnAction(e -> {
            String result = apiService.verifyNip(nipInput.getText());
            resultArea.setText(result);
        });

        view.getChildren().addAll(header, new Label("Sprawdź poprawność numeru NIP (algorytm sumy kontrolnej)."), searchBox, resultArea);
        return view;
    }

    private Button createMenuButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.BASELINE_LEFT);
        btn.setPadding(new Insets(12, 15, 12, 15));
        btn.getStyleClass().add("flat");
        btn.setFont(Font.font("Inter", 14));
        return btn;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.initOwner(primaryStage);
        alert.showAndWait();
    }
}