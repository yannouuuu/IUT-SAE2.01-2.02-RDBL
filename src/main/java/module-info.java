module sae.transport.comparison {
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive sae.s2;
    requires java.xml;
    requires java.desktop;
    requires java.prefs;

    opens sae.transport.comparison to javafx.fxml;
    opens sae.transport.comparison.controllers to javafx.fxml;
    exports sae.transport.comparison;
    exports sae.transport.comparison.controllers;
    exports sae.transport.comparison.models;
    exports sae.transport.comparison.exceptions;
}
