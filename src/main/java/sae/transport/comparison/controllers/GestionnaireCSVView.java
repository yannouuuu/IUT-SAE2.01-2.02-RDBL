package sae.transport.comparison.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Contrôleur de la popup du gestionnaire de CSV (gestionnaire-csv-view.fxml).
 * Permet à terme d'afficher et modifier un fichier CSV existant.
 */
public class GestionnaireCSVView implements Initializable {

    // ---------------------------------------------------------------
    // Champs injectés depuis le FXML
    // ---------------------------------------------------------------

    /** Bouton de fermeture / annulation. */
    @FXML
    private Button annulerButton;

    // ---------------------------------------------------------------
    // Initialisation
    // ---------------------------------------------------------------

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // TODO : charger et afficher le tableau des trajets du CSV courant
    }

    // ---------------------------------------------------------------
    // Handlers FXML
    // ---------------------------------------------------------------

    /**
     * Déclenché par le bouton « Annuler ».
     * Ferme la popup sans sauvegarder.
     */
    @FXML
    private void annulerAction() {
        ((Stage) annulerButton.getScene().getWindow()).close();
    }
}
