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
        double co2, prix, temps;

        if(AppState.getInstance().getVoyageur() == null){
            co2 = 33.0;
            prix = 33.0;
            temps = 34.0;
        }else{
            co2  = AppState.getInstance().getVoyageur().getPreferences().get(TypeCout.CO2);
            co2 = co2 * 100;
            prix = AppState.getInstance().getVoyageur().getPreferences().get(TypeCout.PRIX)*100;
            temps = AppState.getInstance().getVoyageur().getPreferences().get(TypeCout.TEMPS)*100;
        }

        // Configurer les sliders (0 → 100)
        CO2Slider.setMin(0);   CO2Slider.setMax(100);
        PrixSlider.setMin(0);  PrixSlider.setMax(100);
        DureeSlider.setMin(0); DureeSlider.setMax(100);

        CO2Slider.setValue(Math.round(co2));
        PrixSlider.setValue(Math.round(prix));
        DureeSlider.setValue(Math.round(temps));

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
        this.AppliquerButton.setText("Appliquer");
        this.AppliquerButton.setDisable(false);
        int co2   = (int) Math.round(CO2Slider.getValue());
        int prix  = (int) Math.round(PrixSlider.getValue());
        int duree = (int) Math.round(DureeSlider.getValue());

        co2Label.setText(co2 + "%");
        prixLabel.setText(prix + "%");
        dureeLabel.setText(duree + "%");
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

        if (co2 + prix + duree > 100){
            double surplus = (co2 + prix + duree) - 100;
            if(co2 < surplus / 3) {
                surplus -= CO2Slider.getValue();
                CO2Slider.setValue(0);
                co2Label.setText(Math.round(CO2Slider.getValue()) + "%");
                PrixSlider.setValue(PrixSlider.getValue() - surplus/2);
                prixLabel.setText(Math.round(PrixSlider.getValue()) + "%");
                DureeSlider.setValue(DureeSlider.getValue() - surplus/2);
                dureeLabel.setText(Math.round(DureeSlider.getValue()) + "%");
            }else if(prix < surplus / 3) {
                surplus -= PrixSlider.getValue();
                CO2Slider.setValue(CO2Slider.getValue() - surplus/2);
                co2Label.setText(Math.round(CO2Slider.getValue()) + "%");
                PrixSlider.setValue(0);
                prixLabel.setText(Math.round(PrixSlider.getValue()) + "%");
                DureeSlider.setValue(DureeSlider.getValue() - surplus/2);
                dureeLabel.setText(Math.round(DureeSlider.getValue()) + "%");
            }else if(duree < surplus / 3) {
                surplus -= DureeSlider.getValue();
                CO2Slider.setValue(CO2Slider.getValue() - surplus/2);
                co2Label.setText(Math.round(CO2Slider.getValue()) + "%");
                PrixSlider.setValue(PrixSlider.getValue() - surplus/2);
                prixLabel.setText(Math.round(PrixSlider.getValue()) + "%");
                DureeSlider.setValue(0);
                dureeLabel.setText(Math.round(DureeSlider.getValue()) + "%");
            }else {
                CO2Slider.setValue(CO2Slider.getValue() - surplus/3);
                co2Label.setText(Math.round(CO2Slider.getValue()) + "%");
                PrixSlider.setValue(PrixSlider.getValue() - surplus/3);
                prixLabel.setText(Math.round(PrixSlider.getValue()) + "%");
                DureeSlider.setValue(DureeSlider.getValue() - surplus/3);
                dureeLabel.setText(Math.round(DureeSlider.getValue()) + "%");
            }
        }else if (co2 + prix + duree < 100){
            double sousplus = 100 - (co2 + prix + duree);
            CO2Slider.setValue(CO2Slider.getValue() + sousplus/3);
            co2Label.setText(Math.round(CO2Slider.getValue()) + "%");
            PrixSlider.setValue(PrixSlider.getValue() + sousplus/3);
            prixLabel.setText(Math.round(PrixSlider.getValue()) + "%");
            DureeSlider.setValue(DureeSlider.getValue() + sousplus/3);
            dureeLabel.setText(Math.round(DureeSlider.getValue()) + "%");
        }


        Voyageur voyageur = AppState.getInstance().getVoyageur();
        Map<TypeCout, Double> prefs = new EnumMap<>(TypeCout.class);
        prefs.put(TypeCout.CO2, CO2Slider.getValue());
        prefs.put(TypeCout.PRIX, PrixSlider.getValue());
        prefs.put(TypeCout.TEMPS, DureeSlider.getValue());
        if (voyageur == null) {
            voyageur = new Voyageur(prefs);
        }
        voyageur.setPreferences(prefs);
        AppState.getInstance().setVoyageur(voyageur);

        this.AppliquerButton.setText("Enregistré");
        this.AppliquerButton.setDisable(true);
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
