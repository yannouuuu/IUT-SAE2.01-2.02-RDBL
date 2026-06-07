package sae.transport.comparison.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Contrôleur de la vue principale de l'application (app-view.fxml).
 * Gère la barre de recherche, les filtres de tri, et la navigation.
 */
public class AppViewController implements Initializable {

    // ---------------------------------------------------------------
    // Champs injectés depuis le FXML
    // ---------------------------------------------------------------

    /** Bouton retour vers l'accueil. */
    @FXML
    private Button accueilButton;

    /** Bouton d'accès au compte utilisateur. */
    @FXML
    private Button compteButton;

    /** Bouton de changement de thème visuel. */
    @FXML
    private Button themeButton;

    /** Conteneur de la barre de recherche. */
    @FXML
    private HBox searchBar;

    /** Conteneur des ComboBox départ/arrivée. */
    @FXML
    private HBox comboBoxHBox;

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

    /** HBox conteneur du bouton de pondérations. */
    @FXML
    private HBox ponderationButton;

    /** Bouton d'accès aux pondérations multi-critères. */
    @FXML
    private Button ponderationsButton;

    // ---------------------------------------------------------------
    // Initialisation
    // ---------------------------------------------------------------

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // TODO : peupler les ComboBox depuis la plateforme partagée
    }

    // ---------------------------------------------------------------
    // Handlers FXML
    // ---------------------------------------------------------------

    /**
     * Déclenché par le bouton compte / accueil.
     * TODO : naviguer vers la vue compte ou revenir à l'accueil.
     */
    @FXML
    private void compteAction() {
        // TODO
    }

    /**
     * Déclenché par le bouton « Thème ».
     * TODO : basculer entre thème clair et sombre.
     */
    @FXML
    private void themeAction() {
        // TODO
    }

    /**
     * Déclenché par le bouton « Rechercher ».
     * TODO : lancer la recherche d'itinéraires départ → arrivée.
     */
    @FXML
    private void rechercherAction() {
        // TODO
    }

    /**
     * Déclenché par le bouton « Le moins coûteux ».
     * TODO : trier et afficher les itinéraires par prix croissant.
     */
    @FXML
    private void leMoinsCouteuxAction() {
        // TODO
    }

    /**
     * Déclenché par le bouton « Le plus écologique ».
     * TODO : trier et afficher les itinéraires par CO2 croissant.
     */
    @FXML
    private void lePlusEcoloAction() {
        // TODO
    }

    /**
     * Déclenché par le bouton « Le plus rapide ».
     * TODO : trier et afficher les itinéraires par temps croissant.
     */
    @FXML
    private void lePlusRapideAction() {
        // TODO
    }

    /**
     * Déclenché lorsqu'un drag est détecté sur le conteneur de pondérations.
     *
     * @param event l'événement souris
     */
    @FXML
    private void ponderationAction(MouseEvent event) {
        event.consume();
    }

    /**
     * Déclenché par le bouton « Pondérations ».
     * TODO : ouvrir la vue de configuration des pondérations.
     */
    @FXML
    private void ponderationsAction() {
        // TODO
    }
}
