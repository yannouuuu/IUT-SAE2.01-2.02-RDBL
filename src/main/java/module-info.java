module sae.transport.comparison {
    requires javafx.controls;
    requires javafx.fxml;
    requires sae.s2;

    opens sae.transport.comparison to javafx.fxml;
    exports sae.transport.comparison;
    exports sae.transport.comparison.controllers;
    exports sae.transport.comparison.models;
    opens sae.transport.comparison.controllers to javafx.fxml;
}
