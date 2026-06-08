package sae.transport.comparison.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import sae.transport.comparison.AppState;
import sae.transport.comparison.models.TypeCout;
import sae.transport.comparison.models.Voyageur;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Contrôleur de la popup de connexion (compte-not-connected-view.fxml).
 * Permet à l'utilisateur de se connecter en saisissant son identifiant (prénom.nom).
 * Crée un {@link Voyageur} et le stocke dans {@link AppState}.
 */
public class CompteNotConnectedView implements Initializable {

    // ---------------------------------------------------------------
    // Champs injectés depuis le FXML
    // ---------------------------------------------------------------

    /** Champ de saisie de l'identifiant utilisateur. */
    @FXML
    private TextField nomTextField;

    /** Bouton de validation de la connexion. */
    @FXML
    private Button seConnecterButton;

    /** Bouton d'annulation — ferme la popup sans connexion. */
    @FXML
    private Button annulerButton;

    // ---------------------------------------------------------------
    // Initialisation
    // ---------------------------------------------------------------

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Rien à initialiser : formulaire vide par défaut
    }

    // ---------------------------------------------------------------
    // Handlers FXML
    // ---------------------------------------------------------------

    /**
     * Déclenché par le bouton « Se connecter ».
     * Valide l'identifiant, crée le {@link Voyageur} et le place dans {@link AppState}.
     * Ferme la popup si la connexion réussit.
     */
    @FXML
    private void seConnecterAction() {
        String nom = nomTextField.getText().trim();

        if (nom.isEmpty()) {
            nomTextField.setStyle("-fx-border-color: #e53935;");
            nomTextField.setPromptText("⚠ Identifiant requis");
            return;
        }

        Voyageur voyageur = new Voyageur(nom, TypeCout.PRIX);
        AppState.getInstance().setVoyageur(voyageur);

        fermerPopup();
    }

    /**
     * Déclenché par le bouton « Annuler ».
     * Ferme la popup sans créer de voyageur.
     */
    @FXML
    private void annulerAction() {
        fermerPopup();
    }

    /**
     * Ferme la fenêtre popup courante.
     */
    private void fermerPopup() {
        ((Stage) annulerButton.getScene().getWindow()).close();
    }
}
