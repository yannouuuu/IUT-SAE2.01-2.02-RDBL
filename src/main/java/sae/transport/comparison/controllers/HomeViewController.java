package sae.transport.comparison.controllers;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import sae.transport.comparison.AppFX;
import sae.transport.comparison.models.Plateforme;
import sae.transport.comparison.exceptions.DonneesInvalidesException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Contrôleur de la vue d'accueil (home-view.fxml).
 * Gère la barre de recherche départ/arrivée, l'import CSV par glisser-déposer
 * ou par clic sur la zone (FileChooser), ainsi que la transition animée vers app-view.
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
    // Modèle
    // ---------------------------------------------------------------

    /** Plateforme partagée — contient les villes et les trajets chargés. */
    private Plateforme plateforme;

    // ---------------------------------------------------------------
    // Initialisation
    // ---------------------------------------------------------------

    /**
     * Appelé automatiquement par JavaFX après le chargement du FXML.
     * Initialise le modèle, configure le drag-and-drop et le clic sur la zone d'import.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        plateforme = new Plateforme();
        configurerDragOver();
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
     * Ne navigue pas : l'utilisateur clique ensuite sur « Rechercher ».
     */
    private void ouvrirFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Importer un fichier CSV");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv")
        );

        File fichier = fileChooser.showOpenDialog(
            glisserDeposerWidget.getScene().getWindow()
        );

        if (fichier != null) {
            chargerCSV(fichier);
        }
    }

    /**
     * Charge un fichier CSV dans la plateforme et peuple les ComboBox.
     * Ne navigue PAS : l'utilisateur choisit ensuite départ/arrivée
     * et clique sur « Rechercher » pour passer à app-view.
     *
     * @param fichier le fichier CSV à charger
     */
    private void chargerCSV(File fichier) {
        try {
            plateforme.chargerDepuisCSV(fichier.getAbsolutePath());
            rafraichirComboBox();
            glisserDeposerWidget.setText("Fichier chargé : " + fichier.getName()
                + " — choisissez départ et arrivée, puis cliquez Rechercher");
            glisserDeposerWidget.setStyle(
                "-fx-background-color: #e8f5e9;" +
                "-fx-border-color: #4caf50;" +
                "-fx-text-fill: #1b5e20;"
            );
        } catch (DonneesInvalidesException e) {
            glisserDeposerWidget.setText("Erreur : " + e.getMessage());
            glisserDeposerWidget.setStyle("");
        }
    }

    /**
     * Met à jour les ComboBox départ/arrivée à partir des villes
     * actuellement chargées dans la plateforme.
     */
    private void rafraichirComboBox() {
        departComboBox.getItems().clear();
        arriverComboBox.getItems().clear();

        for (var ville : plateforme.getVilles()) {
            departComboBox.getItems().add(ville.getNom());
            arriverComboBox.getItems().add(ville.getNom());
        }
    }

    /**
     * Effectue la transition fade (opaque → transparent → nouvelle scène)
     * depuis la vue d'accueil vers {@code app-view.fxml}.
     * L'animation dure 400 ms sur la racine de la scène courante.
     */
    private void naviguerVersAppView() {
        Parent root = glisserDeposerWidget.getScene().getRoot();

        FadeTransition fadeOut = new FadeTransition(Duration.millis(400), root);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        fadeOut.setOnFinished(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                        "/sae/transport/comparison/fxml/app-view.fxml"
                    )
                );
                Parent appRoot = loader.load();
                appRoot.setOpacity(0.0);

                AppFX.getScene().setRoot(appRoot);

                FadeTransition fadeIn = new FadeTransition(Duration.millis(400), appRoot);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();

            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        fadeOut.play();
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
     * Déclenché par un clic souris sur {@link #glisserDeposerWidget} ({@code onMouseClicked}).
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
     * Agrandit légèrement le widget et lui applique une teinte rosée pour indiquer
     * que le dépôt est possible.
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
     * Déclenché lorsqu'un fichier est déposé sur {@link #glisserDeposerWidget} ({@code onDragDropped}).
     * Charge le premier fichier CSV valide trouvé dans le {@link Dragboard},
     * puis navigue vers {@code app-view.fxml} avec une animation fade.
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
     * puis déclenche la transition fade vers app-view.fxml.
     */
    @FXML
    private void rechercherAction() {
        if (plateforme.getVilles().isEmpty()) {
            glisserDeposerWidget.setText(
                "⚠️Importez d'abord un fichier CSV avant de rechercher."
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
                "⚠️Sélectionnez une ville de départ et d'arrivée."
            );
            glisserDeposerWidget.setStyle(
                "-fx-background-color: #fff3e0;" +
                "-fx-border-color: #ff9800;" +
                "-fx-text-fill: #e65100;"
            );
            return;
        }

        naviguerVersAppView();
    }

    /**
     * Déclenché par le bouton « Créer un fichier CSV ».
     * Ouvre un sélecteur de fichier pour choisir l'emplacement du nouveau CSV.
     */
    @FXML
    private void creerCSVAction() {
        // TODO : ouvrir la vue de création/édition de CSV (CreerCSVView)
    }

    /**
     * Déclenché par le bouton « Compte ».
     * Ouvre ou navigue vers la vue de gestion du compte utilisateur.
     */
    @FXML
    private void compteAction() {
        // TODO : naviguer vers la vue compte
    }

    /**
     * Déclenché par le bouton « Thème ».
     * Bascule entre le thème clair et sombre de l'application.
     */
    @FXML
    private void themeAction() {
        // TODO : basculer le thème (clair / sombre)
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
     * Ouvre la vue de configuration des pondérations multi-critères.
     */
    @FXML
    private void ponderationsAction() {
        // TODO : naviguer vers la vue de pondérations
    }

    // ---------------------------------------------------------------
    // Accesseurs
    // ---------------------------------------------------------------

    /**
     * Retourne la plateforme courante (utilisée par d'autres contrôleurs).
     *
     * @return la plateforme
     */
    public Plateforme getPlateforme() {
        return plateforme;
    }
}
