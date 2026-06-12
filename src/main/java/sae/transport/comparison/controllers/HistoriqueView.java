package sae.transport.comparison.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import sae.transport.comparison.AppState;
import sae.transport.comparison.models.*;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

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

    @FXML
    private Button annulerButton;

    /** ListView affichant les voyages de l'historique. */
    @FXML
    private ListView<Voyage> historiqueListView;

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

        // L'historique est déjà chargé en mémoire à la connexion
        List<Voyage> voyages = state.getVoyageur().getHistorique();
        mettreAJourStats(voyages);

        configurerListView();
    }

    private void configurerListView() {
        historiqueListView.setCellFactory(list -> new ListCell<Voyage>() {
            protected void updateItem(Voyage item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent; -fx-padding: 5;");
                } else {
                    setText(null);
                    setStyle(null);

                    double totalPrix = item.getCoutTotal(TypeCout.PRIX);
                    double totalTemps = item.getCoutTotal(TypeCout.TEMPS);
                    double totalCO2 = item.getCoutTotal(TypeCout.CO2);

                    VBox leftBox = new VBox(5);
                    leftBox.setAlignment(Pos.CENTER_LEFT);
                    
                    String titleStr;
                    String detailsStr;
                    
                    if (item.getTrajets().size() == 1) {
                        titleStr = item.getVilleDepart() + " → " + item.getVilleArrivee();
                        detailsStr = "Mode : " + item.getTrajets().get(0).getModalite();
                    } else {
                        List<String> etapes = new ArrayList<>();
                        for (int i = 0; i < item.getTrajets().size() - 1; i++) {
                            etapes.add(item.getTrajets().get(i).getArrivee().toString());
                        }
                        titleStr = item.getVilleDepart() + " → " + item.getVilleArrivee();
                        detailsStr = "Via " + String.join(", ", etapes) + " • " + (item.getTrajets().size() - 1) + " correspondance(s)";
                    }
                    
                    Label titleLabel = new Label(titleStr);
                    titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");
                    titleLabel.setWrapText(true);

                    Label detailsLabel = new Label(detailsStr);
                    detailsLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #777;");
                    detailsLabel.setWrapText(true);

                    leftBox.setMaxWidth(Double.MAX_VALUE);
                    HBox.setHgrow(leftBox, javafx.scene.layout.Priority.ALWAYS);
                    leftBox.getChildren().addAll(titleLabel, detailsLabel);

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

                    HBox rightBox = new HBox(15);
                    rightBox.setAlignment(Pos.CENTER_RIGHT);

                    Label co2Label = new Label(String.format("%.1f kg CO₂", totalCO2));
                    co2Label.setStyle("-fx-font-size: 12px; -fx-text-fill: #10b981; -fx-background-color: #d1fae5; -fx-padding: 2 6; -fx-background-radius: 8;");

                    long h = (long) (totalTemps / 60);
                    long m = (long) (totalTemps % 60);
                    String tempsStr = h > 0 ? h + "h" + String.format("%02d", m) : m + "mn";
                    Label tempsLabel = new Label(tempsStr);
                    tempsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6366f1; -fx-background-color: #e0e7ff; -fx-padding: 2 6; -fx-background-radius: 8;");
                    
                    Label prixLabel = new Label(String.format("%.2f €", totalPrix));
                    prixLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #475569;");

                    rightBox.getChildren().addAll(co2Label, tempsLabel, prixLabel);

                    HBox rootBox = new HBox(leftBox, spacer, rightBox);
                    rootBox.setAlignment(Pos.CENTER);
                    rootBox.setPadding(new Insets(10));
                    rootBox.prefWidthProperty().bind(list.widthProperty().subtract(45));
                    
                    setGraphic(rootBox);
                }
            }
        });
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

        historiqueListView.getItems().setAll(voyages);

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
        AppState.getInstance().getVoyageur().viderHistorique();
        mettreAJourStats(List.of());
    }

    /**
     * Déclenché par le bouton « Annuler » / retour.
     * Navigue vers la vue d'accueil.
     */
    @FXML
    private void annulerAction() {
        AppState.getInstance().naviguerVers(
            AppState.getInstance().getPreviousFxml()
        );
    }
}
