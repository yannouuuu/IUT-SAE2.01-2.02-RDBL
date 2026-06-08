package sae.transport.comparison.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;
import sae.transport.comparison.AppFX;

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

    /** Bouton d'ouverture du sélecteur de thème/couleur. */
    @FXML
    private Button themeButton;

    // ---------------------------------------------------------------
    // Initialisation
    // ---------------------------------------------------------------

    /**
     * Synchronise l'état du ToggleButton avec le thème actuellement appliqué.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        boolean sombreActif = AppFX.getScene().getStylesheets().stream()
            .anyMatch(css -> css.contains("dark"));
        modeSombreButton.setSelected(sombreActif);
        modeSombreButton.setText(sombreActif ? "ON" : "OFF");
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

        if (cssUrl == null) {
            // CSS dark.css non trouvé — fonctionnalité à implémenter
            return;
        }

        if (actif) {
            if (!stylesheets.contains(cssUrl)) {
                stylesheets.add(cssUrl);
            }
        } else {
            stylesheets.remove(cssUrl);
        }
    }

    /**
     * Déclenché par le bouton « Thème ».
     * TODO : ouvrir un sélecteur de couleur d'accent.
     */
    @FXML
    private void themeAction() {
        // TODO : implémenter un ColorPicker et appliquer la couleur d'accent via CSS
    }
}
