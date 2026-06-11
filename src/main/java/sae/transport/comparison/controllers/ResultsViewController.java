package sae.transport.comparison.controllers;

import fr.ulille.but.sae_s2_2026.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import sae.transport.comparison.AppState;
import sae.transport.comparison.models.*;

import javax.xml.validation.Schema;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
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
    private ListView<Chemin> itinerairesListView;

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
    private List<Chemin> resultatsActuels = new ArrayList<>();

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
            departComboBox.setValue(state.getVilleDepart().toString());
        }
        if (state.getVilleArrivee() != null) {
            arriverComboBox.setValue(state.getVilleArrivee().toString());
        }

        // --- Peupler le filtre transport ---
        filtreTransportComboBox.getItems().add(null); // option "Tous"
        filtreTransportComboBox.getItems().addAll(ModaliteTransport.values());
        filtreTransportComboBox.setPromptText("Tous");
        //filtreTransportComboBox.setOnAction(e -> filtrerModaliteAction());

        // --- Configurer la ListView ---
//        itinerairesListView.setCellFactory(lv -> new ListCell<>() {
//            @Override
//            protected void updateItem(Trajet trajet, boolean empty) {
//                super.updateItem(trajet, empty);
//                if (empty || trajet == null) {
//                    setText(null);
//                } else {
//                    setText(String.format(
//                        "%s → %s  [%s]  %.2f€  %.0fmin  %.2fkg CO₂",
//                        trajet.getDepart().toString(),
//                        trajet.getArrivee().toString(),
//                        trajet.getModalite().name(),
//                        trajet.getCout().getValeur(TypeCout.PRIX),
//                        trajet.getCout().getValeur(TypeCout.TEMPS),
//                        trajet.getCout().getValeur(TypeCout.CO2)
//                    ));
//                }
//            }
//        });

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

        afficherResultats(AppState.getInstance().getMultiGraphe());
    }

    /**
     * Peuple la ListView avec la liste de trajets fournie.
     *
     * @param trajets la liste à afficher
     */
    private void afficherResultats(List<Chemin> trajets) {

        itinerairesListView.getItems().clear();
        itinerairesListView.setPadding(new Insets(10, 10, 10, 10));
        itinerairesListView.setCellFactory(list -> new ListCell<Chemin>() {
            protected void updateItem(Chemin item, boolean empty) {
                super.updateItem(item, empty);
                if(empty || item == null) {
                    setText(null);
                } else if(item.aretes().size() == 1){
                    setText("Trajet directe (" + item.aretes().get(0).getModalite() + ")");
                }else{
                    String texte = "Trajet indirecte : " + item.aretes().get(0).getDepart() + " --> ";
                    for(int i = 0; i < item.aretes().size()-1; i++){
                        texte += item.aretes().get(i).getArrivee() + " --> ";
                    }
                    setText(texte + item.aretes().get(item.aretes().size()-1).getArrivee() + ".");
                }
            }
        });
        itinerairesListView.getItems().addAll(trajets);
        detailsVBox.getChildren().clear();
        if (trajets.isEmpty()) {
            Label aucun = new Label("Aucun trajet trouvé pour cet itinéraire.");
            detailsVBox.getChildren().add(aucun);
        }
    }

    /**
     * Cherche au sein de la plateforme, le trajet équivalent à une connexion du multigraphe.
     *
     * @param connexion la connexion ayant un équivalent de type trajet
     * @return la conversion de la connexion vers son trajet respectif.
     */
    private Trajet retrouverTrajet(Connexion connexion){
        for(Trajet t:AppState.getInstance().getPlateforme().getTrajets()){
            if(t.getDepart() == connexion.getDepart() &&
                t.getArrivee() == connexion.getArrivee() &&
                t.getModalite() == connexion.getModalite()){
                return t;
            }
        }
        return null;
    }

    /**
     * Traduit un nombre de minutes en un String exprimant ce temps
     *
     * @param minutes le nombre de minutes
     * @return la conversion de la valeur en String
     */
    private String toHeure(double minutes){
        if(minutes > 59) {
            if(minutes % 60 < 10){
                return (int)minutes / 60 + "h0" + (int)minutes % 60;
            }
            return (int)minutes / 60 + "h" + (int)minutes % 60;
        }
        return (int)minutes + "mn";

    }

    /**
     * Affiche le détail d'un trajet dans le panneau de droite.
     *
     * @param trajet le trajet sélectionné
     */
    private void afficherDetails(Chemin trajet) {
        detailsVBox.getChildren().clear();
        detailsVBox.setPadding(new Insets(25, 25, 25, 25));
        detailsVBox.alignmentProperty().setValue(Pos.TOP_LEFT);

        double prix = 0;
        double temps = 0;
        double ecolo = 0;

        HBox totalHBox = new HBox();

        detailsVBox.getChildren().add(totalHBox);

        detailsVBox.setAlignment(Pos.TOP_LEFT);
        for(Connexion c:trajet.aretes()){
            Trajet t = retrouverTrajet(c);
            prix += t.getCout().getValeur(TypeCout.PRIX);
            temps += t.getCout().getValeur(TypeCout.TEMPS);
            ecolo += t.getCout().getValeur(TypeCout.CO2);

            Label ville = new Label("(" + c.getDepart().toString() + ")");
            ville.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
            detailsVBox.getChildren().add(ville);
            detailsVBox.getChildren().add(new Label("|"));
            detailsVBox.getChildren().add(new Label("| (" + c.getModalite() + ") : " + toHeure(t.getCout().getValeur(TypeCout.TEMPS)) + " et " + t.getCout().getValeur(TypeCout.PRIX) + "€"));
            detailsVBox.getChildren().add(new Label("|"));
        }
        Label ville = new Label("(" + trajet.aretes().get(trajet.aretes().size()-1).getArrivee().toString() + ")");
        ville.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        detailsVBox.getChildren().add(ville);

        VBox vbPrix = new VBox();
        VBox vbTemps = new VBox();
        VBox vbEcolo = new VBox();

        vbPrix.setPadding(new Insets(0, 25, 10, 25));
        vbTemps.setPadding(new Insets(0, 25, 10, 25));
        vbEcolo.setPadding(new Insets(0, 25, 10, 25));

        Label labelPrix = new Label("Prix total");
        Label labelTemps = new Label("Durée du trajet");
        Label labelEcolo = new Label("CO2 émi");

        Label labelTotPrix = new Label(String.format("%.2f", prix) + "€");
        Label labelTotTemps = new Label(toHeure(temps));
        Label labelTotEcolo = new Label(String.format("%.2f",ecolo) + "kg");

        labelPrix.setStyle("-fx-font-size: 15px;");
        labelTemps.setStyle("-fx-font-size: 15px;");
        labelEcolo.setStyle("-fx-font-size: 15px;");

        labelTotPrix.setStyle("-fx-font-size: 17px; -fx-font-weight: bold;");
        labelTotTemps.setStyle("-fx-font-size: 17px; -fx-font-weight: bold;");
        labelTotEcolo.setStyle("-fx-font-size: 17px; -fx-font-weight: bold;");

        vbPrix.getChildren().addAll(labelPrix, labelTotPrix);
        vbTemps.getChildren().addAll(labelTemps, labelTotTemps);
        vbEcolo.getChildren().addAll(labelEcolo, labelTotEcolo);

        totalHBox.getChildren().addAll(vbPrix, vbTemps, vbEcolo);

        // Bouton « Ajouter à l'historique »
        Button ajouterBtn = new Button("+ Ajouter à l'historique");
        //ajouterBtn.setOnAction(e -> ajouterHistoriqueAction(trajet));
        VBox.setMargin(ajouterBtn, new javafx.geometry.Insets(15, 0, 0, 0));
        Region spacer = new Region();
        spacer.setPrefHeight(Double.MAX_VALUE);
        detailsVBox.getChildren().addAll(spacer, ajouterBtn);

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
//        String depart  = departComboBox.getValue();
//        String arrivee = arriverComboBox.getValue();
//
//        if (depart == null || arrivee == null) {
//            return;
//        }
//
//        AppState.getInstance().setVilleDepart(AppState.getInstance().getPlateforme().getVille(depart));
//        AppState.getInstance().setVilleArrivee(AppState.getInstance().getPlateforme().getVille(arrivee));
//        filtreTransportComboBox.setValue(null);
        // Transmettre la sélection à AppState avant navigation
        String depart  = departComboBox.getValue();
        String arrivee = arriverComboBox.getValue();

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
        AppState.getInstance().setMultiGraphe(AlgorithmeKPCC.kpcc(m, AppState.getInstance().getVilleDepart(), AppState.getInstance().getVilleArrivee(), 20));
        lancerRecherche();
    }

    /**
     * Déclenché par le bouton « Le moins coûteux ».
     * Re-trie les résultats actuels par prix croissant.
     */
    @FXML
    private void leMoinsCouteuxAction() {
        leMoinsCouteuxButton.setStyle("-fx-background-color: green;");
        lePlusEcoloButton.setStyle("-fx-background-color: white;");
        lePlusRapideButton.setStyle("-fx-background-color: white;");
        Map<TypeCout, Double> prefs = new EnumMap<>(TypeCout.class);
        prefs.put(TypeCout.CO2, 0.0);
        prefs.put(TypeCout.PRIX, 100.0);
        prefs.put(TypeCout.TEMPS, 0.0);
        AppState.getInstance().getVoyageur().setPreferences(prefs);
        rechercherAction();
    }

    /**
     * Déclenché par le bouton « Le plus écologique ».
     * Re-trie les résultats actuels par émissions CO₂ croissantes.
     */
    @FXML
    private void lePlusEcoloAction() {
        leMoinsCouteuxButton.setStyle("-fx-background-color: white;");
        lePlusEcoloButton.setStyle("-fx-background-color: green;");
        lePlusRapideButton.setStyle("-fx-background-color: white;");
        Map<TypeCout, Double> prefs = new EnumMap<>(TypeCout.class);
        prefs.put(TypeCout.CO2, 100.0);
        prefs.put(TypeCout.PRIX, 0.0);
        prefs.put(TypeCout.TEMPS, 0.0);
        AppState.getInstance().getVoyageur().setPreferences(prefs);
        rechercherAction();
    }

    /**
     * Déclenché par le bouton « Le plus rapide ».
     * Re-trie les résultats actuels par durée croissante.
     */
    @FXML
    private void lePlusRapideAction() {
        leMoinsCouteuxButton.setStyle("-fx-background-color: white;");
        lePlusEcoloButton.setStyle("-fx-background-color: white;");
        lePlusRapideButton.setStyle("-fx-background-color: green;");
        Map<TypeCout, Double> prefs = new EnumMap<>(TypeCout.class);
        prefs.put(TypeCout.CO2, 0.0);
        prefs.put(TypeCout.PRIX, 0.0);
        prefs.put(TypeCout.TEMPS, 100.0);
        AppState.getInstance().getVoyageur().setPreferences(prefs);
        rechercherAction();
    }

    /**
     * Déclenché par le changement de valeur dans {@link #filtreTransportComboBox}.
     * Filtre la liste affichée selon la modalité sélectionnée ({@code null} = tous).
     */
//    @FXML
//    private void filtrerModaliteAction() {
//        ModaliteTransport modalite = filtreTransportComboBox.getValue();
//        if (modalite == null) {
//            afficherResultats(resultatsActuels);
//        } else {
//            List<Trajet> filtres = resultatsActuels.stream()
//                .filter(t -> t.getModalite() == modalite)
//                .collect(Collectors.toList());
//            afficherResultats(filtres);
//        }
//    }

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
        rechercherAction();
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
