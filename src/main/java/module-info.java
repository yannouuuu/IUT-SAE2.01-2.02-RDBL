module sae.transport.comparison {
    requires javafx.controls;
    requires javafx.fxml;

    opens sae.transport.comparison to javafx.fxml;
    exports sae.transport.comparison;
    exports sae.transport.comparison.controllers;
    opens sae.transport.comparison.controllers to javafx.fxml;
}
