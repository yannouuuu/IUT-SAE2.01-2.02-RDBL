package sae.transport.comparison.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import sae.transport.comparison.AppState;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Contrôleur de la popup profil connecté (compte-connecté-view.fxml).
 * Affiche le nom du voyageur et permet d'accéder à l'historique,
 * au gestionnaire de CSV ou de se déconnecter.
 */
public class CompteConnectedView implements Initializable {

    // ---------------------------------------------------------------
    // Champs injectés depuis le FXML
    // ---------------------------------------------------------------

    /** Label affichant le nom (identifiant) du voyageur connecté. */
    @FXML
    private Label nomCompteLabel;

    /** Bouton de navigation vers la vue historique. */
    @FXML
    private Button historiqueButton;

    /** Bouton de déconnexion. */
    @FXML
    private Button seDeconnecterButton;

    /** Bouton d'ouverture du gestionnaire de CSV. */
    @FXML
    private Button gestionnaireCSVButton;

    // ---------------------------------------------------------------
    // Initialisation
    // ---------------------------------------------------------------

    /**
     * Affiche le nom du voyageur courant dans le label de compte.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (AppState.getInstance().getVoyageur() != null) {
            nomCompteLabel.setText(AppState.getInstance().getVoyageur().getNom());
        }
    }

    // ---------------------------------------------------------------
    // Handlers FXML
    // ---------------------------------------------------------------

    /**
     * Déclenché par le bouton « Historique ».
     * Ferme la popup et navigue vers la vue historique.
     */
    @FXML
    private void historiqueAction() {
        fermerPopup();
        AppState.getInstance().naviguerVers(
            "/sae/transport/comparison/fxml/historique-view.fxml"
        );
    }

    /**
     * Déclenché par le bouton « Se déconnecter ».
     * Réinitialise le voyageur courant dans AppState et ferme la popup.
     */
    @FXML
    private void seDeconnecterAction() {
        AppState.getInstance().setVoyageur(null);
        fermerPopup();
    }

    /**
     * Déclenché par le bouton « Gestionnaire de CSV ».
     * Ouvre la popup du gestionnaire CSV.
     */
    @FXML
    private void gestionnaireCSVAction() {
        fermerPopup();
        AppState.getInstance().ouvrirPopup(
            "/sae/transport/comparison/fxml/gestionnaire-csv-view.fxml"
        );
    }

    /**
     * Ferme la fenêtre popup courante.
     */
    private void fermerPopup() {
        ((Stage) historiqueButton.getScene().getWindow()).close();
    }
}
