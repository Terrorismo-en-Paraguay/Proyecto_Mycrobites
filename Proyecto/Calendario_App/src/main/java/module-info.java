module org.example.calendario_app {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires java.sql;
    requires static lombok;

    opens org.example.calendario_app to javafx.fxml;

    exports org.example.calendario_app;
}