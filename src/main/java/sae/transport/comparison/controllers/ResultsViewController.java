package sae.transport.comparison.controllers;

import fr.ulille.but.sae_s2_2026.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import sae.transport.comparison.AppState;
import sae.transport.comparison.models.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

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

    /** MenuButton de filtrage multi-sélection par modalité de transport. */
    @FXML
    private MenuButton filtreTransportMenu;

    /** Liste des modes de transport actuellement cochés. */
    private List<ModaliteTransport> modesSelectionnes = new ArrayList<>();

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

        setupFiltreDoublon();

        // --- Peupler le filtre transport (multi-sélection) ---
        for (ModaliteTransport mod : ModaliteTransport.values()) {
            CheckBox cb = new CheckBox(mod.toString());
            cb.setSelected(true); // Cochés par défaut
            modesSelectionnes.add(mod);

            cb.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    if (!modesSelectionnes.contains(mod)) modesSelectionnes.add(mod);
                } else {
                    modesSelectionnes.remove(mod);
                }
                lancerRecherche(); // Met à jour l'affichage en temps réel
            });

            CustomMenuItem item = new CustomMenuItem(cb);
            item.setHideOnClick(false); // Le menu ne se ferme pas après un clic
            filtreTransportMenu.getItems().add(item);
        }

        // --- Configurer la ListView ---

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

    private boolean isUpdatingCombos = false;

    /**
     * Empêche de choisir la même ville au départ et à l'arrivée en mettant à jour 
     * dynamiquement les listes des ComboBox.
     */
    private void setupFiltreDoublon() {
        departComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updateComboBoxes(departComboBox, arriverComboBox));
        arriverComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updateComboBoxes(arriverComboBox, departComboBox));
    }

    private void updateComboBoxes(ComboBox<String> source, ComboBox<String> target) {
        if (isUpdatingCombos) return;
        isUpdatingCombos = true;
        
        String targetSelected = target.getValue();
        String sourceSelected = source.getValue();
        
        target.getItems().clear();
        for (sae.transport.comparison.models.Ville ville : AppState.getInstance().getPlateforme().getVilles()) {
            if (sourceSelected == null || !ville.getNom().equals(sourceSelected)) {
                target.getItems().add(ville.getNom());
            }
        }
        
        if (targetSelected != null && !targetSelected.equals(sourceSelected)) {
            target.setValue(targetSelected);
        } else {
            target.setValue(null);
        }
        
        isUpdatingCombos = false;
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

        List<Chemin> allChemins = AppState.getInstance().getMultiGraphe();
        List<Chemin> filtered = new ArrayList<>();

        if (allChemins != null) {
            for (Chemin c : allChemins) {
                boolean isValid = true;
                for (Connexion conn : c.aretes()) {
                    if (!modesSelectionnes.contains(conn.getModalite())) {
                        isValid = false;
                        break;
                    }
                }
                if (isValid) {
                    filtered.add(c);
                }
            }
        }

        afficherResultats(filtered);
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
                if (empty || item == null || item.aretes().isEmpty()) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent; -fx-padding: 5;");
                } else {
                    setText(null);

                    // Calcul des coûts totaux
                    double totalPrix = 0, totalTemps = 0, totalCO2 = 0;
                    List<String> modalitesList = new ArrayList<>();
                    for (Connexion c : item.aretes()) {
                        Trajet t = retrouverTrajet(c);
                        if (t != null) {
                            totalPrix += t.getCout().getValeur(TypeCout.PRIX);
                            totalTemps += t.getCout().getValeur(TypeCout.TEMPS);
                            totalCO2 += t.getCout().getValeur(TypeCout.CO2);
                        }
                        if (!modalitesList.contains(c.getModalite().toString())) {
                            modalitesList.add(c.getModalite().toString());
                        }
                    }

                    // VBox gauche : Titre et Description
                    VBox leftBox = new VBox(5);
                    leftBox.setAlignment(Pos.CENTER_LEFT);
                    
                    String titleStr;
                    String detailsStr;
                    
                    if (item.aretes().size() == 1) {
                        titleStr = "Trajet Direct";
                        detailsStr = "Mode : " + item.aretes().get(0).getModalite();
                    } else {
                        List<String> etapes = new ArrayList<>();
                        for (int i = 0; i < item.aretes().size() - 1; i++) {
                            etapes.add(item.aretes().get(i).getArrivee().toString());
                        }
                        titleStr = "Via " + String.join(", ", etapes);
                        detailsStr = (item.aretes().size() - 1) + " correspondance(s) • " + String.join(", ", modalitesList);
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

                    // Region flexible pour pousser les stats à droite (si le texte est court)
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

                    // HBox droite : Stats (Prix, Temps, CO2)
                    HBox rightBox = new HBox(15);
                    rightBox.setAlignment(Pos.CENTER_RIGHT);

                    // Nudging écologique : CO2 en vert, Prix en gris, Temps en indigo
                    Label co2Label = new Label(String.format("%.1f kg CO₂", totalCO2));
                    co2Label.setStyle("-fx-font-size: 12px; -fx-text-fill: #10b981; -fx-background-color: #d1fae5; -fx-padding: 2 6; -fx-background-radius: 8;"); // Vert

                    Label tempsLabel = new Label(toHeure(totalTemps));
                    tempsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6366f1; -fx-background-color: #e0e7ff; -fx-padding: 2 6; -fx-background-radius: 8;"); // Indigo
                    
                    Label prixLabel = new Label(String.format("%.2f €", totalPrix));
                    prixLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #475569;"); // Gris foncé

                    rightBox.getChildren().addAll(co2Label, tempsLabel, prixLabel);

                    // Conteneur principal de la cellule
                    HBox rootBox = new HBox(leftBox, spacer, rightBox);
                    rootBox.setAlignment(Pos.CENTER);
                    rootBox.setPadding(new Insets(10));
                    rootBox.prefWidthProperty().bind(list.widthProperty().subtract(45));
                    
                    setGraphic(rootBox);
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

    private void afficherDetails(Chemin trajet) {
        detailsVBox.getChildren().clear();
        detailsVBox.setPadding(new Insets(25));
        detailsVBox.setAlignment(Pos.TOP_LEFT);
        detailsVBox.setSpacing(20);

        double prix = 0;
        double temps = 0;
        double ecolo = 0;

        for(Connexion c : trajet.aretes()){
            Trajet t = retrouverTrajet(c);
            if (t != null) {
                prix += t.getCout().getValeur(TypeCout.PRIX);
                temps += t.getCout().getValeur(TypeCout.TEMPS);
                ecolo += t.getCout().getValeur(TypeCout.CO2);
            }
        }

        // --- 1. Cartes de résumé (En-tête) ---
        HBox summaryHBox = new HBox(15);
        summaryHBox.setAlignment(Pos.CENTER_LEFT);

        // Nudging: Le CO2 est mis en valeur avec la couleur verte pour inciter au choix écologique
        VBox vbEcolo = creerCarteInfo("Émissions CO₂", String.format("%.1f kg", ecolo), "#10b981", "#d1fae5");
        VBox vbTemps = creerCarteInfo("Temps estimé", toHeure(temps), "#6366f1", "#e0e7ff");
        VBox vbPrix = creerCarteInfo("Prix total", String.format("%.2f €", prix), "#64748b", "#f1f5f9");

        summaryHBox.getChildren().addAll(vbEcolo, vbTemps, vbPrix);
        detailsVBox.getChildren().add(summaryHBox);

        // Ligne de séparation
        javafx.scene.shape.Line separator = new javafx.scene.shape.Line(0, 0, 400, 0);
        separator.setStroke(javafx.scene.paint.Color.web("#e5e7eb"));
        detailsVBox.getChildren().add(separator);

        // --- 2. Frise chronologique du trajet ---
        VBox timelineVBox = new VBox();
        timelineVBox.setSpacing(0);

        for (Connexion c : trajet.aretes()) {
            Trajet t = retrouverTrajet(c);
            
            // Ville de départ de cette étape
            Label ville = new Label(c.getDepart().toString());
            ville.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");
            timelineVBox.getChildren().add(ville);

            // Détails du transport (Ligne verticale + infos)
            HBox stepBox = new HBox(15);
            stepBox.setPadding(new Insets(5, 0, 5, 8)); // Aligné avec le texte de la ville
            
            // La barre verticale
            Region verticalLine = new Region();
            verticalLine.setPrefWidth(2);
            verticalLine.setStyle("-fx-background-color: #cbd5e1;");
            
            VBox transportDetails = new VBox(2);
            transportDetails.setPadding(new Insets(10, 0, 10, 0));
            
            Label modeLabel = new Label("Mode : " + c.getModalite());
            modeLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #4b5563;");
            
            String infosExtra = "Détails introuvables";
            if (t != null) {
                infosExtra = "Durée : " + toHeure(t.getCout().getValeur(TypeCout.TEMPS)) + "   |   Coût : " + String.format("%.2f €", t.getCout().getValeur(TypeCout.PRIX));
            }
            Label infoLabel = new Label(infosExtra);
            infoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");

            transportDetails.getChildren().addAll(modeLabel, infoLabel);
            stepBox.getChildren().addAll(verticalLine, transportDetails);
            
            timelineVBox.getChildren().add(stepBox);
        }
        
        // Ville d'arrivée finale
        Label villeArrivee = new Label(trajet.aretes().get(trajet.aretes().size()-1).getArrivee().toString());
        villeArrivee.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        timelineVBox.getChildren().add(villeArrivee);

        detailsVBox.getChildren().add(timelineVBox);

        // --- 3. Bouton d'action en bas ---
        Region spacer = new Region();
        VBox.setVgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Button ajouterBtn = new Button("+ Ajouter à l'historique");
        ajouterBtn.setOnAction(e -> ajouterHistoriqueAction(trajet));
        
        detailsVBox.getChildren().addAll(spacer, ajouterBtn);
    }

    /**
     * Crée une carte de résumé colorée pour le haut du panneau de détails.
     */
    private VBox creerCarteInfo(String titre, String valeur, String textColor, String bgColor) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(10, 15, 10, 15));
        card.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 10;");
        
        Label titreLabel = new Label(titre);
        titreLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7280; -fx-font-weight: bold;");
        
        Label valeurLabel = new Label(valeur);
        valeurLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + textColor + ";");
        
        card.getChildren().addAll(titreLabel, valeurLabel);
        return card;
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
     * Ajoute le chemin sélectionné à l'historique du voyageur courant.
     * Sans-op si aucun voyageur n'est connecté.
     *
     * @param chemin le chemin à enregistrer
     */
    private void ajouterHistoriqueAction(Chemin chemin) {
        AppState state = AppState.getInstance();
        if (state.getVoyageur() == null) {
            afficherErreurDetails("Connectez-vous pour sauvegarder dans l'historique.");
            return;
        }

        List<Trajet> listeTrajets = new ArrayList<>();
        for (Connexion c : chemin.aretes()) {
            Trajet t = retrouverTrajet(c);
            if (t != null) {
                listeTrajets.add(t);
            }
        }
        
        Voyage voyage = new Voyage(listeTrajets);
        state.getVoyageur().ajouterVoyage(voyage);

        // Persistance
        String cheminFichier = System.getProperty("user.home") + File.separator
            + ".sae-transport" + File.separator
            + state.getVoyageur().getNom() + ".ser";

        new File(cheminFichier).getParentFile().mkdirs();
        HistoriqueManager manager = new HistoriqueManager(cheminFichier);
        try {
            manager.ajouterEtSauvegarder(voyage);
            Label succes = new Label("Trajet ajouté à l'historique !");
            succes.setStyle("-fx-text-fill: #4caf50; -fx-font-weight: bold;");
            detailsVBox.getChildren().add(succes);
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
        Label erreur = new Label(message);
        erreur.setStyle("-fx-text-fill: #e53935; -fx-font-weight: bold;");
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

        AppState.getInstance().setVilleDepart(AppState.getInstance().getPlateforme().getVille(depart));
        AppState.getInstance().setVilleArrivee(AppState.getInstance().getPlateforme().getVille(arrivee));

        MultiGrapheOrienteValue m = new MultiGrapheOrienteValue();
        for (Lieu l : AppState.getInstance().getPlateforme().getVilles()) {
            m.ajouterSommet(l);
        }

        for (Trajet t : AppState.getInstance().getPlateforme().getTrajets()) {
            double poids = t.getCout().getValeur(TypeCout.CO2) * AppState.getInstance().getVoyageur().getPreferences().get(TypeCout.CO2) +
                           t.getCout().getValeur(TypeCout.TEMPS) * AppState.getInstance().getVoyageur().getPreferences().get(TypeCout.TEMPS) +
                           t.getCout().getValeur(TypeCout.PRIX) * AppState.getInstance().getVoyageur().getPreferences().get(TypeCout.PRIX);
            m.ajouterArete(t, poids);
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
     * Action déclenchée par le bouton de changement de thème.
     */
    @FXML
    private void themeAction() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/sae/transport/comparison/fxml/apparence-view.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Apparence");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.show();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
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
