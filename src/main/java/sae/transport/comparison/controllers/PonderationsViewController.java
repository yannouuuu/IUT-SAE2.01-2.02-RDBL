package sae.transport.comparison.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.stage.Stage;
import sae.transport.comparison.AppState;
import sae.transport.comparison.models.TypeCout;
import sae.transport.comparison.models.Voyageur;

import java.net.URL;
import java.util.EnumMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Contrôleur de la popup de pondérations multi-critères (ponderations-view.fxml).
 * Permet à l'utilisateur de régler les poids relatifs CO₂ / Prix / Temps
 * utilisés pour le tri personnalisé des itinéraires.
 */
public class PonderationsViewController implements Initializable {

    // ---------------------------------------------------------------
    // Champs injectés depuis le FXML
    // ---------------------------------------------------------------

    /** Slider du critère CO₂. */
    @FXML
    private Slider CO2Slider;

    /** Slider du critère Prix. */
    @FXML
    private Slider PrixSlider;

    /** Slider du critère Temps/Durée. */
    @FXML
    private Slider DureeSlider;

    /** Label affichant le pourcentage CO₂ courant. */
    @FXML
    private Label co2Label;

    /** Label affichant le pourcentage Prix courant. */
    @FXML
    private Label prixLabel;

    /** Label affichant le pourcentage Temps courant. */
    @FXML
    private Label dureeLabel;

    /** Label affichant la somme totale (doit rester à 100%). */
    @FXML
    private Label totalLabel;

    /** Bouton de validation et fermeture de la popup. */
    @FXML
    private Button AppliquerButton;

    // ---------------------------------------------------------------
    // Initialisation
    // ---------------------------------------------------------------

    /**
     * Initialise les sliders avec les préférences du voyageur courant.
     * Si aucun voyageur n'est connecté, les sliders sont à 33% chacun.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Voyageur voyageur = AppState.getInstance().getVoyageur();

        double co2  = 1.0 / 3.0;
        double prix = 1.0 / 3.0;
        double temps = 1.0 / 3.0;

        if (voyageur != null) {
            Map<TypeCout, Double> prefs = voyageur.getPreferences();
            co2  = prefs.getOrDefault(TypeCout.CO2,   co2);
            prix = prefs.getOrDefault(TypeCout.PRIX,  prix);
            temps = prefs.getOrDefault(TypeCout.TEMPS, temps);
        }

        // Configurer les sliders (0 → 100)
        CO2Slider.setMin(0);   CO2Slider.setMax(100);
        PrixSlider.setMin(0);  PrixSlider.setMax(100);
        DureeSlider.setMin(0); DureeSlider.setMax(100);

        CO2Slider.setValue(Math.round(co2  * 100));
        PrixSlider.setValue(Math.round(prix * 100));
        DureeSlider.setValue(Math.round(temps * 100));

        mettreAJourLabels();

        // Listeners sur changements
        CO2Slider.valueProperty().addListener((obs, o, n)  -> mettreAJourLabels());
        PrixSlider.valueProperty().addListener((obs, o, n) -> mettreAJourLabels());
        DureeSlider.valueProperty().addListener((obs, o, n) -> mettreAJourLabels());
    }

    // ---------------------------------------------------------------
    // Mise à jour des labels
    // ---------------------------------------------------------------

    /**
     * Met à jour les labels de pourcentage et le total affiché.
     * Le total s'affiche en rouge si la somme n'est pas 100%.
     */
    private void mettreAJourLabels() {
        int co2   = (int) Math.round(CO2Slider.getValue());
        int prix  = (int) Math.round(PrixSlider.getValue());
        int duree = (int) Math.round(DureeSlider.getValue());
        int total = co2 + prix + duree;

        co2Label.setText(co2 + "%");
        prixLabel.setText(prix + "%");
        dureeLabel.setText(duree + "%");
        totalLabel.setText("Total : " + total + "%");

        if (total != 100) {
            totalLabel.setStyle("-fx-text-fill: #e53935; -fx-font-weight: bold;");
        } else {
            totalLabel.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
        }
    }

    // ---------------------------------------------------------------
    // Handlers FXML
    // ---------------------------------------------------------------

    /**
     * Déclenché par le bouton « Appliquer ».
     * Valide que la somme vaut 100%, sauvegarde les préférences dans
     * le voyageur courant et ferme la popup.
     */
    @FXML
    private void AppliquerAction() {
        int co2   = (int) Math.round(CO2Slider.getValue());
        int prix  = (int) Math.round(PrixSlider.getValue());
        int duree = (int) Math.round(DureeSlider.getValue());

        if (co2 + prix + duree != 100) {
            totalLabel.setText("⚠ La somme doit être égale à 100 !");
            totalLabel.setStyle("-fx-text-fill: #e53935; -fx-font-weight: bold;");
            return;
        }

        Voyageur voyageur = AppState.getInstance().getVoyageur();
        if (voyageur != null) {
            Map<TypeCout, Double> prefs = new EnumMap<>(TypeCout.class);
            prefs.put(TypeCout.CO2,   co2   / 100.0);
            prefs.put(TypeCout.PRIX,  prix  / 100.0);
            prefs.put(TypeCout.TEMPS, duree / 100.0);
            voyageur.setPreferences(prefs);
        }

        fermerPopup();
    }

    /**
     * Ferme la popup sans sauvegarder les modifications.
     */
    @FXML
    private void annulerAction() {
        fermerPopup();
    }

    /**
     * Ferme la fenêtre popup courante.
     */
    private void fermerPopup() {
        ((Stage) AppliquerButton.getScene().getWindow()).close();
    }

    /**
     * Déclenché lors du survol de l'icône d'information.
     *
     * @param event l'événement souris
     */
    @FXML
    private void informationHover(javafx.scene.input.MouseDragEvent event) {
        // TODO : afficher une tooltip explicative
        event.consume();
    }
}
