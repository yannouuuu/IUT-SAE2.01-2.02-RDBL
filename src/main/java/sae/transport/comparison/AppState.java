package sae.transport.comparison;

import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import sae.transport.comparison.models.Plateforme;
import sae.transport.comparison.models.Voyageur;

import java.io.IOException;

/**
 * Partagé entre tous les controllers de l'application.
 * Centralise l'état global : plateforme de données, voyageur courant,
 * villes sélectionnées et méthodes de navigation.
 */
public class AppState {

    private static AppState instance;

    /** Réseau de transport chargé. */
    private Plateforme plateforme;

    /** Voyageur actuellement connecté, {@code null} si personne. */
    private Voyageur voyageur;

    /** Fenêtre principale de l'application. */
    private Stage primaryStage;

    /** Ville de départ sélectionnée pour la recherche. */
    private String villeDepart;

    /** Ville d'arrivée sélectionnée pour la recherche. */
    private String villeArrivee;

    // ---------------------------------------------------------------
    // Singleton
    // ---------------------------------------------------------------

    private AppState() {
        this.plateforme = new Plateforme();
    }

    /**
     * Retourne l'instance unique de l'état applicatif.
     *
     * @return l'instance singleton
     */
    public static AppState getInstance() {
        if (instance == null) {
            instance = new AppState();
        }
        return instance;
    }

    // ---------------------------------------------------------------
    // Navigation principale (avec animation fade)
    // ---------------------------------------------------------------

    /**
     * Navigue vers une nouvelle vue principale avec une animation fade.
     * Remplace la racine de la scène principale (scene root swap).
     *
     * @param fxmlPath chemin absolu du FXML (ex: "/sae/transport/comparison/fxml/home-view.fxml")
     */
    public void naviguerVers(String fxmlPath) {
        Parent root = AppFX.getScene().getRoot();

        FadeTransition fadeOut = new FadeTransition(Duration.millis(400), root);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        fadeOut.setOnFinished(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(fxmlPath)
                );
                Parent newRoot = loader.load();
                newRoot.setOpacity(0.0);
                AppFX.getScene().setRoot(newRoot);

                FadeTransition fadeIn = new FadeTransition(Duration.millis(400), newRoot);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        fadeOut.play();
    }

    /**
     * Ouvre une vue secondaire dans une fenêtre popup modale.
     * La popup bloque la fenêtre principale jusqu'à sa fermeture.
     *
     * @param fxmlPath chemin absolu du FXML de la popup
     * @return la {@link Stage} créée, ou {@code null} en cas d'erreur de chargement
     */
    public Stage ouvrirPopup(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource(fxmlPath)
            );
            Parent root = loader.load();

            Stage popup = new Stage();
            popup.initOwner(AppFX.getScene().getWindow());
            popup.initModality(Modality.WINDOW_MODAL);
            popup.setResizable(false);
            popup.setScene(new Scene(root));
            popup.show();

            return popup;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ---------------------------------------------------------------
    // Getters / Setters
    // ---------------------------------------------------------------

    /**
     * Retourne la plateforme de données courante (villes et trajets chargés).
     *
     * @return la plateforme
     */
    public Plateforme getPlateforme() {
        return plateforme;
    }

    /**
     * Remplace la plateforme de données courante.
     *
     * @param plateforme la nouvelle plateforme
     */
    public void setPlateforme(Plateforme plateforme) {
        this.plateforme = plateforme;
    }

    /**
     * Retourne le voyageur actuellement connecté, ou {@code null}.
     *
     * @return le voyageur courant
     */
    public Voyageur getVoyageur() {
        return voyageur;
    }

    /**
     * Définit le voyageur courant. Passer {@code null} déconnecte l'utilisateur.
     *
     * @param voyageur le voyageur à connecter, ou {@code null}
     */
    public void setVoyageur(Voyageur voyageur) {
        this.voyageur = voyageur;
    }

    /**
     * Retourne la fenêtre principale de l'application.
     *
     * @return le stage principal
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Enregistre la fenêtre principale (appelé depuis {@link AppFX#start}).
     *
     * @param primaryStage la fenêtre principale
     */
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    /**
     * Retourne la ville de départ sélectionnée pour la recherche courante.
     *
     * @return le nom de la ville de départ, ou {@code null}
     */
    public String getVilleDepart() {
        return villeDepart;
    }

    /**
     * Définit la ville de départ pour la recherche.
     *
     * @param villeDepart le nom de la ville de départ
     */
    public void setVilleDepart(String villeDepart) {
        this.villeDepart = villeDepart;
    }

    /**
     * Retourne la ville d'arrivée sélectionnée pour la recherche courante.
     *
     * @return le nom de la ville d'arrivée, ou {@code null}
     */
    public String getVilleArrivee() {
        return villeArrivee;
    }

    /**
     * Définit la ville d'arrivée pour la recherche.
     *
     * @param villeArrivee le nom de la ville d'arrivée
     */
    public void setVilleArrivee(String villeArrivee) {
        this.villeArrivee = villeArrivee;
    }
}
