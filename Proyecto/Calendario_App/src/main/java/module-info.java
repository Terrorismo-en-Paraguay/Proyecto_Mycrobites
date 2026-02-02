module org.example.calendario_app {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires java.sql;
    requires java.prefs;
    requires jakarta.mail;
    requires static lombok;

    opens org.example.calendario_app to javafx.fxml;

    exports org.example.calendario_app;
    exports org.example.calendario_app.model;

    opens org.example.calendario_app.model to javafx.fxml;
}