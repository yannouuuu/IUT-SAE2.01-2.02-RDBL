package sae.transport.comparison.controllers;

import fr.ulille.but.sae_s2_2026.ModaliteTransport;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import sae.transport.comparison.AppState;
import sae.transport.comparison.exceptions.DonneesInvalidesException;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Contrôleur de la popup de création de fichier CSV (creer-csv-view.fxml).
 * Permet de saisir des trajets ligne par ligne, puis de générer le fichier CSV
 * et de le charger automatiquement dans la plateforme.
 */
public class CreerCSVView implements Initializable {

    // ---------------------------------------------------------------
    // Champs injectés depuis le FXML
    // ---------------------------------------------------------------

    /** Champ de saisie du nom du fichier CSV à générer. */
    @FXML
    private TextField nomFichierTextField;

    /** VBox qui reçoit dynamiquement les lignes de saisie des trajets. */
    @FXML
    private VBox trajetsContainer;

    /** Bouton pour ajouter une ligne de trajet. */
    @FXML
    private Button ajouterUnTrajetButton;

    /** Bouton de génération du CSV et chargement dans la plateforme. */
    @FXML
    private Button genererCSVButton;

    /** Bouton d'annulation — ferme sans sauvegarder. */
    @FXML
    private Button annulerButton;

    // ---------------------------------------------------------------
    // État interne
    // ---------------------------------------------------------------

    /** Liste des lignes de saisie présentes dans le formulaire. */
    private final List<HBox> lignesTrajets = new ArrayList<>();

    // ---------------------------------------------------------------
    // Initialisation
    // ---------------------------------------------------------------

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Ajouter une première ligne vide au démarrage
        ajouterLigne();
    }

    // ---------------------------------------------------------------
    // Gestion des lignes
    // ---------------------------------------------------------------

    /**
     * Ajoute une ligne de saisie avec 6 champs (départ, arrivée, modalité, prix, CO₂, durée)
     * et un bouton de suppression.
     */
    private void ajouterLigne() {
        HBox ligne = new HBox(5);
        ligne.setPadding(new Insets(2, 0, 2, 0));

        TextField depart    = creerChamp("Départ",    80);
        TextField arrivee   = creerChamp("Arrivée",   80);
        ComboBox<String> mod = new ComboBox<>();
        mod.setPrefWidth(75);
        mod.setPromptText("Mode");
        for (ModaliteTransport m : ModaliteTransport.values()) {
            mod.getItems().add(m.name());
        }
        TextField prix  = creerChamp("Prix (€)",   60);
        TextField co2   = creerChamp("CO₂ (kg)",   60);
        TextField temps = creerChamp("Durée (min)", 70);

        Button supprimer = new Button("✕");
        supprimer.setStyle("-fx-font-size: 9px;");
        supprimer.setOnAction(e -> {
            trajetsContainer.getChildren().remove(ligne);
            lignesTrajets.remove(ligne);
        });

        ligne.getChildren().addAll(depart, arrivee, mod, prix, co2, temps, supprimer);
        ligne.setUserData(new TextField[]{depart, arrivee, prix, co2, temps});
        // On stocke la ComboBox séparément dans le tag
        ligne.setId("trajet-" + lignesTrajets.size());

        lignesTrajets.add(ligne);
        trajetsContainer.getChildren().add(ligne);
    }

    /**
     * Crée un TextField avec les dimensions et le promptText donnés.
     */
    private TextField creerChamp(String prompt, double largeur) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setPrefWidth(largeur);
        return tf;
    }

    // ---------------------------------------------------------------
    // Handlers FXML
    // ---------------------------------------------------------------

    /**
     * Déclenché par le bouton « + Ajouter un trajet ».
     * Ajoute une nouvelle ligne de saisie vide.
     */
    @FXML
    private void ajouterUnTrajetAction() {
        ajouterLigne();
    }

    /**
     * Déclenché par le bouton « Générer le fichier CSV complet ».
     * Valide les données, crée le fichier CSV, le charge dans la plateforme
     * et ferme la popup.
     */
    @FXML
    private void genererCSVAction() {
        String nomFichier = nomFichierTextField.getText().trim();
        if (nomFichier.isEmpty()) {
            nomFichierTextField.setStyle("-fx-border-color: #e53935;");
            return;
        }
        if (!nomFichier.endsWith(".csv")) {
            nomFichier += ".csv";
        }

        // Choisir l'emplacement via FileChooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le fichier CSV");
        fileChooser.setInitialFileName(nomFichier);
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv")
        );
        File fichier = fileChooser.showSaveDialog(annulerButton.getScene().getWindow());
        if (fichier == null) {
            return;
        }

        // Construire les lignes CSV
        List<String> lignesCSV = new ArrayList<>();
        boolean erreur = false;

        for (HBox ligne : lignesTrajets) {
            var enfants = ligne.getChildren();
            // enfants : depart(TF), arrivee(TF), mod(CB), prix(TF), co2(TF), temps(TF), btn(Btn)
            String depart  = ((TextField)  enfants.get(0)).getText().trim();
            String arrivee = ((TextField)  enfants.get(1)).getText().trim();
            String mod     = ((ComboBox<?>) enfants.get(2)).getValue() != null
                             ? enfants.get(2).toString() : "";
            // Récupérer la valeur de la ComboBox proprement
            @SuppressWarnings("unchecked")
            ComboBox<String> comboBox = (ComboBox<String>) enfants.get(2);
            String modalite = comboBox.getValue() != null ? comboBox.getValue() : "";

            String prixStr  = ((TextField) enfants.get(3)).getText().trim();
            String co2Str   = ((TextField) enfants.get(4)).getText().trim();
            String tempsStr = ((TextField) enfants.get(5)).getText().trim();

            if (depart.isEmpty() || arrivee.isEmpty() || modalite.isEmpty()
                    || prixStr.isEmpty() || co2Str.isEmpty() || tempsStr.isEmpty()) {
                marquerErreurLigne(ligne);
                erreur = true;
                continue;
            }

            try {
                double prixVal  = Double.parseDouble(prixStr);
                double co2Val   = Double.parseDouble(co2Str);
                double tempsVal = Double.parseDouble(tempsStr);
                if (prixVal < 0 || co2Val < 0 || tempsVal < 0) {
                    marquerErreurLigne(ligne);
                    erreur = true;
                    continue;
                }
                lignesCSV.add(depart + ";" + arrivee + ";" + modalite + ";"
                    + prixStr + ";" + co2Str + ";" + tempsStr);
            } catch (NumberFormatException e) {
                marquerErreurLigne(ligne);
                erreur = true;
            }
        }

        if (erreur || lignesCSV.isEmpty()) {
            return;
        }

        // Écrire le fichier
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fichier))) {
            for (String ligne : lignesCSV) {
                writer.write(ligne);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // Charger dans la plateforme
        try {
            AppState.getInstance().getPlateforme().chargerDepuisCSV(fichier.getAbsolutePath());
        } catch (DonneesInvalidesException e) {
            e.printStackTrace();
        }

        fermerPopup();
    }

    /**
     * Marque visuellement une ligne en erreur (bordure rouge).
     *
     * @param ligne la HBox à marquer
     */
    private void marquerErreurLigne(HBox ligne) {
        ligne.setStyle("-fx-border-color: #e53935; -fx-border-width: 1;");
    }

    /**
     * Déclenché par le bouton « Annuler ».
     * Ferme la popup sans générer de fichier.
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
