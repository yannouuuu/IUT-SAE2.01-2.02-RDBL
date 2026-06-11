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

    /** Label affichant le nombre de trajets enregistrés dans l'historique. */
    @FXML
    private Label statsTrajetsLabel;

    /** Label affichant le total des dépenses cumulées. */
    @FXML
    private Label statsPrixLabel;

    // ---------------------------------------------------------------
    // Initialisation
    // ---------------------------------------------------------------

    /**
     * Affiche le nom du voyageur courant dans le label de compte.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (AppState.getInstance().getVoyageur() != null) {
            sae.transport.comparison.models.Voyageur v = AppState.getInstance().getVoyageur();
            nomCompteLabel.setText(v.getNom());
            
            int nbTrajets = v.getHistorique().size();
            statsTrajetsLabel.setText(nbTrajets + (nbTrajets > 1 ? " trajets" : " trajet"));
            
            double totalPrix = v.getTotalHistorique(sae.transport.comparison.models.TypeCout.PRIX);
            statsPrixLabel.setText(String.format("%.2f €", totalPrix));
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
     * Ferme la fenêtre popup courante.
     */
    private void fermerPopup() {
        ((Stage) historiqueButton.getScene().getWindow()).close();
    }
}
