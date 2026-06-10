package sae.transport.comparison.controllers;

import fr.ulille.but.sae_s2_2026.AlgorithmeKPCC;
import fr.ulille.but.sae_s2_2026.Connexion;
import fr.ulille.but.sae_s2_2026.Lieu;
import fr.ulille.but.sae_s2_2026.MultiGrapheOrienteValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import sae.transport.comparison.AppState;
import sae.transport.comparison.exceptions.DonneesInvalidesException;
import sae.transport.comparison.models.Cout;
import sae.transport.comparison.models.Plateforme;
import sae.transport.comparison.models.Trajet;
import sae.transport.comparison.models.TypeCout;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Contrôleur de la vue d'accueil (home-view.fxml).
 * Gère la barre de recherche départ/arrivée, l'import CSV par glisser-déposer
 * ou par clic sur la zone (FileChooser), ainsi que la transition animée vers app-view.
 * Utilise {@link AppState} pour partager la plateforme avec les autres controllers.
 */
public class HomeViewController implements Initializable {

    // ---------------------------------------------------------------
    // Champs injectés depuis le FXML
    // ---------------------------------------------------------------

    /** Bouton rond en haut à droite — accès au compte utilisateur. */
    @FXML
    private Button compteButton;

    /** Bouton rond dans la barre de recherche — changement de thème visuel. */
    @FXML
    private Button themeButton;

    /** Conteneur principal de la barre de recherche. */
    @FXML
    private HBox searchBar;

    /** Conteneur interne des deux ComboBox départ/arrivée. */
    @FXML
    private HBox comboBoxHBox;

    /** ComboBox de sélection de la ville de départ. */
    @FXML
    private ComboBox<String> departComboBox;

    /** ComboBox de sélection de la ville d'arrivée. */
    @FXML
    private ComboBox<String> arriverComboBox;

    /** Bouton de lancement de la recherche d'itinéraire. */
    @FXML
    private Button rechercherButton;

    /** VBox regroupant la zone de dépôt CSV et le bouton de création. */
    @FXML
    private VBox lowVBox;

    /**
     * Label servant de zone de glisser-déposer et de clic pour importer un fichier CSV.
     * Réagit aux événements {@code onDragDropped}, {@code onDragEntered} et au clic souris.
     */
    @FXML
    private Label glisserDeposerWidget;

    /** Bouton permettant de créer un nouveau fichier CSV. */
    @FXML
    private Button creerCSVButton;

    /** HBox conteneur du bouton de pondérations, en bas à droite. */
    @FXML
    private HBox ponderationButton;

    /** Bouton d'accès aux pondérations multi-critères. */
    @FXML
    private Button ponderationsButton;

    // ---------------------------------------------------------------
    // Initialisation
    // ---------------------------------------------------------------

    /**
     * Appelé automatiquement par JavaFX après le chargement du FXML.
     * Peuple les ComboBox depuis la plateforme partagée dans {@link AppState}
     * et configure le drag-and-drop.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurerDragOver();
        // Si la plateforme contient déjà des données (retour en arrière), repeupler
        rafraichirComboBox();
    }

    // ---------------------------------------------------------------
    // Configuration interne
    // ---------------------------------------------------------------

    /**
     * Configure l'acceptation des fichiers pendant le survol (onDragOver).
     * Nécessaire pour que JavaFX autorise le dépôt.
     */
    private void configurerDragOver() {
        glisserDeposerWidget.setOnDragOver(event -> {
            if (event.getGestureSource() != glisserDeposerWidget
                    && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });
    }

    /**
     * Remet le widget dans son état visuel par défaut (texte + style d'origine).
     */
    private void restaurerApparenceWidget() {
        glisserDeposerWidget.setText(
            "Glissez-déposez votre premier CSV pour utiliser le comparateur"
        );
        glisserDeposerWidget.setStyle("");
    }

    /**
     * Ouvre un FileChooser pour sélectionner un fichier CSV,
     * puis charge le fichier si l'utilisateur valide son choix.
     */
    private void ouvrirFileChooser() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Importer un fichier CSV");
        fileChooser.getExtensionFilters().add(
            new javafx.stage.FileChooser.ExtensionFilter("Fichiers CSV", "*.csv")
        );

        File fichier = fileChooser.showOpenDialog(
            glisserDeposerWidget.getScene().getWindow()
        );

        if (fichier != null) {
            chargerCSV(fichier);
        }
    }

    /**
     * Charge un fichier CSV dans la plateforme partagée ({@link AppState})
     * et peuple les ComboBox.
     *
     * @param fichier le fichier CSV à charger
     */
    private void chargerCSV(File fichier) {
        Plateforme plateforme = AppState.getInstance().getPlateforme();
        try {
            plateforme.chargerDepuisCSV(fichier.getAbsolutePath());
            rafraichirComboBox();
            glisserDeposerWidget.setText("Fichier \"" + fichier.getName()
                + "\" chargé !");
            glisserDeposerWidget.setStyle(
                "-fx-background-color: #e8f5e9;" +
                "-fx-border-color: #4caf50;" +
                "-fx-text-fill: #1b5e20;"
            );
            AppState.getInstance().setPlateforme(plateforme);
        } catch (DonneesInvalidesException e) {
            glisserDeposerWidget.setText("Erreur : " + e.getMessage());
            glisserDeposerWidget.setStyle("");
        }
    }

    /**
     * Met à jour les ComboBox départ/arrivée à partir des villes
     * actuellement chargées dans la plateforme partagée.
     */
    private void rafraichirComboBox() {
        departComboBox.getItems().clear();
        arriverComboBox.getItems().clear();

        for (var ville : AppState.getInstance().getPlateforme().getVilles()) {
            departComboBox.getItems().add(ville.getNom());
            arriverComboBox.getItems().add(ville.getNom());
        }
    }

    // ---------------------------------------------------------------
    // Handlers FXML
    // ---------------------------------------------------------------

    /**
     * Déclenché lorsque le curseur quitte la zone de dépôt ({@code onDragExited}).
     * Restaure l'apparence normale du widget.
     *
     * @param event l'événement de sortie
     */
    @FXML
    private void dragExitedAction(DragEvent event) {
        restaurerApparenceWidget();
        event.consume();
    }

    /**
     * Déclenché par un clic souris sur la zone d'import ({@code onMouseClicked}).
     * Ouvre un FileChooser pour importer un CSV sans glisser-déposer.
     *
     * @param event l'événement souris
     */
    @FXML
    private void clicZoneImportAction(MouseEvent event) {
        ouvrirFileChooser();
    }

    /**
     * Déclenché lorsqu'un fichier entre dans la zone de dépôt ({@code onDragEntered}).
     * Agrandit légèrement le widget et lui applique une teinte rosée.
     *
     * @param event l'événement de survol
     */
    @FXML
    private void colorChangedAction(DragEvent event) {
        if (event.getGestureSource() != glisserDeposerWidget
                && event.getDragboard().hasFiles()) {
            glisserDeposerWidget.setText("Déposez votre fichier CSV ici ↓");
            glisserDeposerWidget.setStyle(
                "-fx-background-color: #fce4ec;" +
                "-fx-border-color: #e91e63;" +
                "-fx-scale-x: 1.03;" +
                "-fx-scale-y: 1.03;" +
                "-fx-text-fill: #880e4f;" +
                "-fx-transition: all 0.2s;"
            );
        }
        event.consume();
    }

    /**
     * Déclenché lorsqu'un fichier est déposé sur la zone ({@code onDragDropped}).
     * Charge le premier fichier CSV valide du {@link Dragboard}.
     *
     * @param event l'événement de dépôt
     */
    @FXML
    private void glisserDeposerAction(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;

        if (db.hasFiles()) {
            for (File fichier : db.getFiles()) {
                if (fichier.getName().toLowerCase().endsWith(".csv")) {
                    chargerCSV(fichier);
                    success = true;
                    break;
                }
            }
        }

        if (!success) {
            restaurerApparenceWidget();
        }

        event.setDropCompleted(success);
        event.consume();
    }

    /**
     * Déclenché par le bouton « Rechercher ».
     * Vérifie qu'un CSV est chargé et que départ/arrivée sont renseignés,
     * stocke la sélection dans {@link AppState} et navigue vers app-view.
     */
    @FXML
    private void rechercherAction() {
        if (AppState.getInstance().getPlateforme().getVilles().isEmpty()) {
            glisserDeposerWidget.setText(
                "Importez d'abord un fichier CSV avant de rechercher."
            );
            glisserDeposerWidget.setStyle(
                "-fx-background-color: #fff3e0;" +
                "-fx-border-color: #ff9800;" +
                "-fx-text-fill: #e65100;"
            );
            return;
        }

        String depart  = departComboBox.getValue();
        String arrivee = arriverComboBox.getValue();

        if (depart == null || arrivee == null || depart.isEmpty() || arrivee.isEmpty()) {
            glisserDeposerWidget.setText(
                "Sélectionnez une ville de départ et d'arrivée."
            );
            glisserDeposerWidget.setStyle(
                "-fx-background-color: #fff3e0;" +
                "-fx-border-color: #ff9800;" +
                "-fx-text-fill: #e65100;"
            );
            return;
        }

        // Transmettre la sélection à AppState avant navigation
        AppState.getInstance().setVilleDepart(AppState.getInstance().getPlateforme().getVille(depart));
        AppState.getInstance().setVilleArrivee(AppState.getInstance().getPlateforme().getVille(arrivee));

        MultiGrapheOrienteValue m = new MultiGrapheOrienteValue();
        for(Lieu l:AppState.getInstance().getPlateforme().getVilles()){
            m.ajouterSommet(l);
        }

        for(Trajet t:AppState.getInstance().getPlateforme().getTrajets()){
            m.ajouterArete(t,
                    t.getCout().getValeur(TypeCout.CO2)*
                    AppState.getInstance().getVoyageur().getPreferences().get(TypeCout.CO2) +
                    t.getCout().getValeur(TypeCout.TEMPS)*
                    AppState.getInstance().getVoyageur().getPreferences().get(TypeCout.TEMPS) +
                    t.getCout().getValeur(TypeCout.PRIX)*
                    AppState.getInstance().getVoyageur().getPreferences().get(TypeCout.PRIX));
        }
        AppState.getInstance().setMultiGraphe(AlgorithmeKPCC.kpcc(m, AppState.getInstance().getVilleDepart(), AppState.getInstance().getVilleArrivee(), 10));


        AppState.getInstance().naviguerVers(
            "/sae/transport/comparison/fxml/app-view.fxml"
        );
    }

    /**
     * Déclenché par le bouton « Créer un fichier CSV ».
     * Ouvre la popup de création/édition de CSV.
     */
    @FXML
    private void creerCSVAction() {
        AppState.getInstance().ouvrirPopup(
            "/sae/transport/comparison/fxml/creer-csv-view.fxml"
        );
    }

    /**
     * Déclenché par le bouton « Compte ».
     * Ouvre la popup de connexion ou de profil selon l'état du voyageur.
     */
    @FXML
    private void compteAction() {
        AppState state = AppState.getInstance();
        String fxml = state.getVoyageur() == null
            ? "/sae/transport/comparison/fxml/compte-not-connected-view.fxml"
            : "/sae/transport/comparison/fxml/compte-connecté-view.fxml";
        state.ouvrirPopup(fxml);
    }

    /**
     * Déclenché par le bouton « Thème ».
     * Ouvre la popup de configuration de l'apparence.
     */
    @FXML
    private void themeAction() {
        AppState.getInstance().ouvrirPopup(
            "/sae/transport/comparison/fxml/apparence-view.fxml"
        );
    }

    /**
     * Déclenché lorsqu'un drag est détecté sur {@link #ponderationButton} ({@code onDragDetected}).
     *
     * @param event l'événement souris détectant le drag
     */
    @FXML
    private void ponderationAction(MouseEvent event) {
        event.consume();
    }

    /**
     * Déclenché par le bouton « Pondérations ».
     * Ouvre la popup de configuration des pondérations multi-critères.
     */
    @FXML
    private void ponderationsAction() {
        AppState.getInstance().ouvrirPopup(
            "/sae/transport/comparison/fxml/ponderations-view.fxml"
        );
    }

    // ---------------------------------------------------------------
    // Accesseurs (rétro-compatibilité)
    // ---------------------------------------------------------------

    /**
     * Retourne la plateforme courante via AppState.
     * Conservé pour rétro-compatibilité.
     *
     * @return la plateforme partagée
     */
    public Plateforme getPlateforme() {
        return AppState.getInstance().getPlateforme();
    }
}
