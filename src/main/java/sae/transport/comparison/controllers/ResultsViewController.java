package sae.transport.comparison.controllers;

import fr.ulille.but.sae_s2_2026.ModaliteTransport;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import sae.transport.comparison.AppState;
import sae.transport.comparison.models.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Contrôleur de la vue des résultats de recherche (app-view.fxml).
 * Affiche la liste des trajets correspondant au trajet départ → arrivée choisi,
 * permet de trier, filtrer par modalité, consulter le détail et sauvegarder dans l'historique.
 */
public class ResultsViewController implements Initializable {

    // ---------------------------------------------------------------
    // Champs injectés depuis le FXML
    // ---------------------------------------------------------------

    /** Bouton de retour à l'accueil. */
    @FXML
    private Button accueilButton;

    /** Bouton d'accès au compte utilisateur. */
    @FXML
    private Button compteButton;

    /** Bouton de changement de thème visuel. */
    @FXML
    private Button themeButton;

    /** ComboBox de sélection de la ville de départ. */
    @FXML
    private ComboBox<String> departComboBox;

    /** ComboBox de sélection de la ville d'arrivée. */
    @FXML
    private ComboBox<String> arriverComboBox;

    /** Bouton de lancement de la recherche. */
    @FXML
    private Button rechercherButton;

    /** Bouton de tri « le moins coûteux ». */
    @FXML
    private Button leMoinsCouteuxButton;

    /** Bouton de tri « le plus écologique ». */
    @FXML
    private Button lePlusEcoloButton;

    /** Bouton de tri « le plus rapide ». */
    @FXML
    private Button lePlusRapideButton;

    /** ComboBox de filtrage par modalité de transport. */
    @FXML
    private ComboBox<ModaliteTransport> filtreTransportComboBox;

    /** ListView affichant les trajets trouvés. */
    @FXML
    private ListView<Trajet> itinerairesListView;

    /** VBox du panneau de détails (côté droit). */
    @FXML
    private VBox detailsVBox;

    /** HBox conteneur du bouton pondérations. */
    @FXML
    private javafx.scene.layout.HBox ponderationButton;

    /** Bouton d'accès aux pondérations multi-critères. */
    @FXML
    private Button ponderationsButton;

    // ---------------------------------------------------------------
    // État interne
    // ---------------------------------------------------------------

    /** Liste complète des résultats pour le trajet courant (avant filtre modalité). */
    private List<Trajet> resultatsActuels = new ArrayList<>();

    // ---------------------------------------------------------------
    // Initialisation
    // ---------------------------------------------------------------

    /**
     * Appelé automatiquement après le chargement du FXML.
     * Peuple les ComboBox, pré-sélectionne les villes depuis AppState,
     * configure la ListView et lance la première recherche.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        AppState state = AppState.getInstance();

        // --- Peupler les ComboBox villes ---
        for (Ville ville : state.getPlateforme().getVilles()) {
            departComboBox.getItems().add(ville.getNom());
            arriverComboBox.getItems().add(ville.getNom());
        }

        // --- Pré-sélectionner depuis AppState ---
        if (state.getVilleDepart() != null) {
            departComboBox.setValue(state.getVilleDepart());
        }
        if (state.getVilleArrivee() != null) {
            arriverComboBox.setValue(state.getVilleArrivee());
        }

        // --- Peupler le filtre transport ---
        filtreTransportComboBox.getItems().add(null); // option "Tous"
        filtreTransportComboBox.getItems().addAll(ModaliteTransport.values());
        filtreTransportComboBox.setPromptText("Tous");
        filtreTransportComboBox.setOnAction(e -> filtrerModaliteAction());

        // --- Configurer la ListView ---
        itinerairesListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Trajet trajet, boolean empty) {
                super.updateItem(trajet, empty);
                if (empty || trajet == null) {
                    setText(null);
                } else {
                    setText(String.format(
                        "%s → %s  [%s]  %.2f€  %.0fmin  %.2fkg CO₂",
                        trajet.getDepart().toString(),
                        trajet.getArrivee().toString(),
                        trajet.getModalite().name(),
                        trajet.getCout().getValeur(TypeCout.PRIX),
                        trajet.getCout().getValeur(TypeCout.TEMPS),
                        trajet.getCout().getValeur(TypeCout.CO2)
                    ));
                }
            }
        });

        // --- Sélection → afficher détails ---
        itinerairesListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> {
                if (newVal != null) {
                    afficherDetails(newVal);
                }
            }
        );

        // --- Lancer la première recherche ---
        lancerRecherche();
    }

    // ---------------------------------------------------------------
    // Logique de recherche / tri / filtre
    // ---------------------------------------------------------------

    /**
     * Filtre les trajets de la plateforme selon les villes départ et arrivée,
     * stocke le résultat dans {@link #resultatsActuels} et peuple la ListView.
     */
    private void lancerRecherche() {
        String depart  = departComboBox.getValue();
        String arrivee = arriverComboBox.getValue();

        if (depart == null || arrivee == null) {
            return;
        }

        resultatsActuels = AppState.getInstance().getPlateforme().getTrajets().stream()
            .filter(t -> t.getDepart().toString().equals(depart)
                      && t.getArrivee().toString().equals(arrivee))
            .collect(Collectors.toList());

        afficherResultats(resultatsActuels);
    }

    /**
     * Peuple la ListView avec la liste de trajets fournie.
     *
     * @param trajets la liste à afficher
     */
    private void afficherResultats(List<Trajet> trajets) {
        itinerairesListView.getItems().setAll(trajets);
        detailsVBox.getChildren().clear();
        if (trajets.isEmpty()) {
            Label aucun = new Label("Aucun trajet trouvé pour cet itinéraire.");
            detailsVBox.getChildren().add(aucun);
        }
    }

    /**
     * Affiche le détail d'un trajet dans le panneau de droite.
     *
     * @param trajet le trajet sélectionné
     */
    private void afficherDetails(Trajet trajet) {
        detailsVBox.getChildren().clear();

        detailsVBox.getChildren().addAll(
            creerLabelDetail("Départ :",     trajet.getDepart().toString()),
            creerLabelDetail("Arrivée :",    trajet.getArrivee().toString()),
            creerLabelDetail("Transport :",  trajet.getModalite().name()),
            creerLabelDetail("Prix :",       String.format("%.2f €", trajet.getCout().getValeur(TypeCout.PRIX))),
            creerLabelDetail("Durée :",      String.format("%.0f min", trajet.getCout().getValeur(TypeCout.TEMPS))),
            creerLabelDetail("CO₂ :",        String.format("%.2f kg", trajet.getCout().getValeur(TypeCout.CO2)))
        );

        // Bouton « Ajouter à l'historique »
        Button ajouterBtn = new Button("+ Ajouter à l'historique");
        ajouterBtn.setOnAction(e -> ajouterHistoriqueAction(trajet));
        VBox.setMargin(ajouterBtn, new javafx.geometry.Insets(15, 0, 0, 0));
        detailsVBox.getChildren().add(ajouterBtn);
    }

    /**
     * Crée un conteneur label « clé : valeur » pour le panneau de détails.
     */
    private javafx.scene.layout.HBox creerLabelDetail(String cle, String valeur) {
        Label lblCle   = new Label(cle);
        lblCle.setStyle("-fx-font-weight: bold;");
        Label lblVal   = new Label(valeur);
        javafx.scene.layout.HBox hbox = new javafx.scene.layout.HBox(8, lblCle, lblVal);
        VBox.setMargin(hbox, new javafx.geometry.Insets(4, 0, 0, 0));
        return hbox;
    }

    /**
     * Ajoute le trajet sélectionné à l'historique du voyageur courant.
     * Sans-op si aucun voyageur n'est connecté.
     *
     * @param trajet le trajet à enregistrer
     */
    private void ajouterHistoriqueAction(Trajet trajet) {
        AppState state = AppState.getInstance();
        if (state.getVoyageur() == null) {
            afficherErreurDetails("Connectez-vous pour sauvegarder dans l'historique.");
            return;
        }

        List<Trajet> liste = new ArrayList<>();
        liste.add(trajet);
        Voyage voyage = new Voyage(liste);
        state.getVoyageur().ajouterVoyage(voyage);

        // Persistance
        String cheminFichier = System.getProperty("user.home") + File.separator
            + ".sae-transport" + File.separator
            + state.getVoyageur().getNom() + ".ser";

        new File(cheminFichier).getParentFile().mkdirs();
        HistoriqueManager manager = new HistoriqueManager(cheminFichier);
        try {
            manager.ajouterEtSauvegarder(voyage);
        } catch (IOException e) {
            afficherErreurDetails("Erreur lors de la sauvegarde : " + e.getMessage());
        }
    }

    /**
     * Affiche un message d'erreur dans le panneau de détails.
     *
     * @param message le message à afficher
     */
    private void afficherErreurDetails(String message) {
        detailsVBox.getChildren().clear();
        Label erreur = new Label(message);
        erreur.setStyle("-fx-text-fill: #e53935;");
        detailsVBox.getChildren().add(erreur);
    }

    // ---------------------------------------------------------------
    // Handlers FXML
    // ---------------------------------------------------------------

    /**
     * Déclenché par le bouton « Rechercher ».
     * Met à jour AppState avec les villes sélectionnées et relance la recherche.
     */
    @FXML
    private void rechercherAction() {
        String depart  = departComboBox.getValue();
        String arrivee = arriverComboBox.getValue();

        if (depart == null || arrivee == null) {
            return;
        }

        AppState.getInstance().setVilleDepart(depart);
        AppState.getInstance().setVilleArrivee(arrivee);
        filtreTransportComboBox.setValue(null);
        lancerRecherche();
    }

    /**
     * Déclenché par le bouton « Le moins coûteux ».
     * Re-trie les résultats actuels par prix croissant.
     */
    @FXML
    private void leMoinsCouteuxAction() {
        List<Trajet> tries = new ArrayList<>(resultatsActuels);
        tries.sort((a, b) -> Double.compare(
            a.getCout().getValeur(TypeCout.PRIX),
            b.getCout().getValeur(TypeCout.PRIX)
        ));
        afficherResultats(tries);
    }

    /**
     * Déclenché par le bouton « Le plus écologique ».
     * Re-trie les résultats actuels par émissions CO₂ croissantes.
     */
    @FXML
    private void lePlusEcoloAction() {
        List<Trajet> tries = new ArrayList<>(resultatsActuels);
        tries.sort((a, b) -> Double.compare(
            a.getCout().getValeur(TypeCout.CO2),
            b.getCout().getValeur(TypeCout.CO2)
        ));
        afficherResultats(tries);
    }

    /**
     * Déclenché par le bouton « Le plus rapide ».
     * Re-trie les résultats actuels par durée croissante.
     */
    @FXML
    private void lePlusRapideAction() {
        List<Trajet> tries = new ArrayList<>(resultatsActuels);
        tries.sort((a, b) -> Double.compare(
            a.getCout().getValeur(TypeCout.TEMPS),
            b.getCout().getValeur(TypeCout.TEMPS)
        ));
        afficherResultats(tries);
    }

    /**
     * Déclenché par le changement de valeur dans {@link #filtreTransportComboBox}.
     * Filtre la liste affichée selon la modalité sélectionnée ({@code null} = tous).
     */
    @FXML
    private void filtrerModaliteAction() {
        ModaliteTransport modalite = filtreTransportComboBox.getValue();
        if (modalite == null) {
            afficherResultats(resultatsActuels);
        } else {
            List<Trajet> filtres = resultatsActuels.stream()
                .filter(t -> t.getModalite() == modalite)
                .collect(Collectors.toList());
            afficherResultats(filtres);
        }
    }

    /**
     * Déclenché par le bouton retour accueil.
     * Navigue vers la vue d'accueil avec animation fade.
     */
    @FXML
    private void accueilAction() {
        AppState.getInstance().naviguerVers(
            "/sae/transport/comparison/fxml/home-view.fxml"
        );
    }

    /**
     * Déclenché par le bouton compte.
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
     * Déclenché par le bouton thème.
     * Ouvre la popup de configuration de l'apparence.
     */
    @FXML
    private void themeAction() {
        AppState.getInstance().ouvrirPopup(
            "/sae/transport/comparison/fxml/apparence-view.fxml"
        );
    }

    /**
     * Déclenché lorsqu'un drag est détecté sur le conteneur de pondérations.
     *
     * @param event l'événement souris
     */
    @FXML
    private void ponderationAction(javafx.scene.input.MouseEvent event) {
        event.consume();
    }

    /**
     * Déclenché par le bouton pondérations.
     * Ouvre la popup de configuration des pondérations multi-critères.
     */
    @FXML
    private void ponderationsAction() {
        AppState.getInstance().ouvrirPopup(
            "/sae/transport/comparison/fxml/ponderations-view.fxml"
        );
    }
}
