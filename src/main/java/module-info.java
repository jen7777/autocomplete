module com.example.simple {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.example.simple to javafx.fxml;
    exports com.example.simple;
}