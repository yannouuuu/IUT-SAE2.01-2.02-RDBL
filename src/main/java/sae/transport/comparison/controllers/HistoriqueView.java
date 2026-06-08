package sae.transport.comparison.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import sae.transport.comparison.AppState;
import sae.transport.comparison.models.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Contrôleur de la vue historique (historique-view.fxml).
 * Charge l'historique du voyageur connecté, calcule et affiche
 * les statistiques agrégées (dépenses, CO₂, temps).
 */
public class HistoriqueView implements Initializable {

    // ---------------------------------------------------------------
    // Champs injectés depuis le FXML
    // ---------------------------------------------------------------

    /** Label affichant le total des dépenses en €. */
    @FXML
    private Label totalDepensesLabel;

    /** Label affichant le total des émissions CO₂ en kg. */
    @FXML
    private Label totalCO2Label;

    /** Label affichant le temps de trajet total. */
    @FXML
    private Label totalTempsLabel;

    /** Label affichant le CO₂ moyen par heure de trajet. */
    @FXML
    private Label moyenneCO2Label;

    /** Bouton d'effacement de tout l'historique. */
    @FXML
    private Button effacerHistoriqueButton;

    /** Bouton de retour à l'accueil. */
    @FXML
    private Button annulerButton;

    // ---------------------------------------------------------------
    // État interne
    // ---------------------------------------------------------------

    /** Chemin vers le fichier de persistance de l'historique. */
    private String cheminFichier;

    // ---------------------------------------------------------------
    // Initialisation
    // ---------------------------------------------------------------

    /**
     * Charge l'historique du voyageur courant et met à jour les statistiques affichées.
     * Si aucun voyageur n'est connecté, affiche des valeurs nulles.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        AppState state = AppState.getInstance();

        if (state.getVoyageur() == null) {
            mettreAJourStats(List.of());
            effacerHistoriqueButton.setDisable(true);
            return;
        }

        cheminFichier = System.getProperty("user.home") + File.separator
            + ".sae-transport" + File.separator
            + state.getVoyageur().getNom() + ".ser";

        HistoriqueManager manager = new HistoriqueManager(cheminFichier);
        try {
            List<Voyage> voyages = manager.charger();
            // Synchronise l'historique en mémoire du voyageur
            voyages.forEach(state.getVoyageur()::ajouterVoyage);
            mettreAJourStats(voyages);
        } catch (IOException e) {
            mettreAJourStats(List.of());
        }
    }

    // ---------------------------------------------------------------
    // Calcul et affichage des stats
    // ---------------------------------------------------------------

    /**
     * Calcule les totaux et met à jour les labels de statistiques.
     *
     * @param voyages la liste des voyages à agréger
     */
    private void mettreAJourStats(List<Voyage> voyages) {
        double totalPrix  = 0;
        double totalCO2   = 0;
        double totalTemps = 0;

        for (Voyage voyage : voyages) {
            totalPrix  += voyage.getCoutTotal(TypeCout.PRIX);
            totalCO2   += voyage.getCoutTotal(TypeCout.CO2);
            totalTemps += voyage.getCoutTotal(TypeCout.TEMPS);
        }

        totalDepensesLabel.setText(String.format("%.2f€", totalPrix));
        totalCO2Label.setText(String.format("%.2f kg de CO₂", totalCO2));

        long heures  = (long) (totalTemps / 60);
        long minutes = (long) (totalTemps % 60);
        totalTempsLabel.setText(heures + "h et " + minutes + "mn");

        double moyenneCO2 = (totalTemps > 0) ? (totalCO2 / (totalTemps / 60.0)) : 0.0;
        moyenneCO2Label.setText(String.format("%.2f kg de CO₂ par heure de trajet !", moyenneCO2));
    }

    // ---------------------------------------------------------------
    // Handlers FXML
    // ---------------------------------------------------------------

    /**
     * Déclenché par le bouton « Effacer l'historique ».
     * Supprime le fichier de persistance et réinitialise les statistiques affichées.
     */
    @FXML
    private void effacerHistoriqueAction() {
        if (cheminFichier != null) {
            new File(cheminFichier).delete();
        }
        mettreAJourStats(List.of());
    }

    /**
     * Déclenché par le bouton « Annuler » / retour.
     * Navigue vers la vue d'accueil.
     */
    @FXML
    private void annulerAction() {
        AppState.getInstance().naviguerVers(
            "/sae/transport/comparison/fxml/home-view.fxml"
        );
    }
}
