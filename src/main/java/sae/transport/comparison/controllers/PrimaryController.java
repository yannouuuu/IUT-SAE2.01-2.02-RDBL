package sae.transport.comparison.controllers;

import java.io.IOException;
import javafx.fxml.FXML;
import sae.transport.comparison.AppFX;

public class PrimaryController {

    @FXML
    private void switchToSecondary() throws IOException {
        AppFX.setRoot("secondary");
    }
}
