module com.veriflow {
    requires javafx.controls;
    requires javafx.fxml;
    requires atlantafx.base;
    requires java.sql;
    requires org.json;
    requires java.net.http;
    requires java.mail;

    opens com.veriflow.veriflow to javafx.fxml;
    exports com.veriflow.veriflow;
}