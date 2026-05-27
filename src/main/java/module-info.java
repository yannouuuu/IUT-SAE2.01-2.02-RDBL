module sae.transport.comparison {
    requires javafx.controls;
    requires javafx.fxml;
    requires sae.s2;

    opens sae.transport.comparison to javafx.fxml;
    opens sae.transport.comparison.controllers to javafx.fxml;
    exports sae.transport.comparison;
    exports sae.transport.comparison.controllers;
    exports sae.transport.comparison.models;
    exports sae.transport.comparison.exceptions;
}
