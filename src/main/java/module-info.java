module com.example.registroelettronico {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires fontawesomefx;
    requires itextpdf;
    requires jbcrypt;
    requires javafx.graphics;
    requires javafx.base;

    opens application to javafx.fxml;
    exports application;

    exports application.controller;
    opens application.controller to javafx.fxml;
    exports application.controller.prof;
    opens application.controller.prof to javafx.fxml;
    exports application.controller.studente;
    opens application.controller.studente to javafx.fxml;
    exports application.model;
    opens application.model to javafx.fxml;
    exports application.export;
    opens application.export to javafx.fxml;
    exports application.observer;
    opens application.observer to javafx.fxml;
}