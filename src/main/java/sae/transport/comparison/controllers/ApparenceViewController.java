package sae.transport.comparison.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ToggleButton;
import javafx.scene.paint.Color;
import sae.transport.comparison.AppFX;
import sae.transport.comparison.AppState;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Contrôleur de la popup d'apparence (apparence-view.fxml).
 * Permet de basculer entre le thème sombre et clair,
 * et de choisir une couleur d'accent.
 */
public class ApparenceViewController implements Initializable {


    // ---------------------------------------------------------------
    // Champs injectés depuis le FXML
    // ---------------------------------------------------------------

    /** Toggle pour activer/désactiver le mode sombre. */
    @FXML
    private ToggleButton modeSombreButton;

    /** Sélecteur de thème/couleur. */
    @FXML
    private ColorPicker themeColorPicker;

    /** Permet de revenir aux settings initiaux. */
    @FXML
    private Button resetButton;

    // ---------------------------------------------------------------
    // Initialisation
    // ---------------------------------------------------------------

    /**
     * Synchronise l'état du ToggleButton avec le thème actuellement appliqué.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        boolean sombreActif = AppState.getInstance().getDarkMode();
        modeSombreButton.setSelected(sombreActif);
        modeSombreButton.setText(sombreActif ? "ON" : "OFF");

        themeColorPicker.setValue(sae.transport.comparison.AppState.getInstance().getThemeColor());
        themeColorPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            sae.transport.comparison.AppState state = sae.transport.comparison.AppState.getInstance();
            state.setThemeColor(newVal);
            if (sae.transport.comparison.AppFX.getScene() != null && sae.transport.comparison.AppFX.getScene().getRoot() != null) {
                state.appliquerTheme(sae.transport.comparison.AppFX.getScene().getRoot(), state.getThemeColor(), AppState.getInstance().getDarkMode());
            }
        });
    }

    // ---------------------------------------------------------------
    // Handlers FXML
    // ---------------------------------------------------------------

    /**
     * Déclenché par le ToggleButton "Mode sombre".
     * Ajoute un effet "différence" sur root, lui permettant d'avoir les couleurs opposées.
     */
    @FXML
    private void modeSombreAction() {
        boolean actif = modeSombreButton.isSelected();
        modeSombreButton.setText(actif ? "ON" : "OFF");

        AppState.getInstance().setDarkMode(actif);
        AppState.getInstance().appliquerTheme(sae.transport.comparison.AppFX.getScene().getRoot(), AppState.getInstance().getThemeColor(), actif);
    }

    /** Déclanchée par le bouton "reset".
     * Permet de revenir aux settings initiaux.
     */
    @FXML
    private void resetAction() {
        AppState.getInstance().setDarkMode(false);
        AppState.getInstance().setThemeColor(new Color(0.7607, 0.2901, 0.9607, 1));
        AppState.getInstance().appliquerTheme(AppFX.getScene().getRoot(), AppState.getInstance().getThemeColor(), AppState.getInstance().getDarkMode());
    }
}
