package sae.transport.comparison.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ToggleButton;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
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

    /** Chemin de la feuille CSS thème sombre. */
    private static final String CSS_SOMBRE =
        "/sae/transport/comparison/css/dark.css";

    // ---------------------------------------------------------------
    // Champs injectés depuis le FXML
    // ---------------------------------------------------------------

    /** Toggle pour activer/désactiver le mode sombre. */
    @FXML
    private ToggleButton modeSombreButton;

    /** Sélecteur de thème/couleur. */
    @FXML
    private ColorPicker themeColorPicker;

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
//        boolean sombreActif = AppFX.getScene().getStylesheets().stream()
//            .anyMatch(css -> css.contains("dark"));
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
     * Déclenché par le ToggleButton « Mode sombre ».
     * Ajoute ou retire la feuille CSS de thème sombre sur la scène principale.
     */
    @FXML
    private void modeSombreAction() {
        boolean actif = modeSombreButton.isSelected();
        modeSombreButton.setText(actif ? "ON" : "OFF");

        var stylesheets = AppFX.getScene().getStylesheets();
        String cssUrl = getClass().getResource(CSS_SOMBRE) != null
            ? getClass().getResource(CSS_SOMBRE).toExternalForm()
            : null;

//        if (cssUrl == null) {
//            // CSS dark.css non trouvé — fonctionnalité à implémenter
//            return;
//        }
//
//        if (actif) {
//            if (!stylesheets.contains(cssUrl)) {
//                stylesheets.add(cssUrl);
//            }
//        } else {
//            stylesheets.remove(cssUrl);
//        }
        AppState.getInstance().setDarkMode(actif);
        AppState.getInstance().appliquerTheme(sae.transport.comparison.AppFX.getScene().getRoot(), AppState.getInstance().getThemeColor(), actif);
    }

    @FXML
    private void resetAction() {
        AppState.getInstance().setDarkMode(false);
        AppState.getInstance().setThemeColor(new Color(0.7607, 0.2901, 0.9607, 1));
        AppState.getInstance().appliquerTheme(AppFX.getScene().getRoot(), AppState.getInstance().getThemeColor(), AppState.getInstance().getDarkMode());
    }
}
