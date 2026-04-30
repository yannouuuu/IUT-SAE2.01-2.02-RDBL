package sae.transport.comparison.controllers;

import java.io.IOException;
import javafx.fxml.FXML;
import sae.transport.comparison.AppFX;

public class SecondaryController {

    @FXML
    private void switchToPrimary() throws IOException {
        AppFX.setRoot("primary");
    }
}