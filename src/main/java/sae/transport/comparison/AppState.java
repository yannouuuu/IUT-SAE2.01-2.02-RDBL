package sae.transport.comparison;

import fr.ulille.but.sae_s2_2026.Chemin;
import fr.ulille.but.sae_s2_2026.Lieu;
import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.BlendMode;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import sae.transport.comparison.models.Plateforme;
import sae.transport.comparison.models.TypeCout;
import sae.transport.comparison.models.Voyageur;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

/**
 * Point de communication entre tous les controllers de l'application.
 * Centralise l'état global : plateforme de données, voyageur courant,
 * villes sélectionnées et méthodes de navigation.
 */
public class AppState {

    /** Instance par laquelle les controllers communiquent. */
    private static AppState instance;

    /** Réseau de transport chargé. */
    private Plateforme plateforme;

    /** Voyageur actuellement connecté, {@code null} si personne. */
    private Voyageur voyageur;

    /** Fenêtre principale de l'application. */
    private Stage primaryStage;

    /** Ville de départ sélectionnée pour la recherche. */
    private Lieu villeDepart;

    /** Ville d'arrivée sélectionnée pour la recherche. */
    private Lieu villeArrivee;

    /** Les 10 plus court chemins en partant de villeDepart à villeArrivee. Poids : Co2. */
    private List<Chemin> multiGraphe;

    /** Couleur d'accentuation dynamique */
    private ObjectProperty<Color> themeColor;

    /** Dit si le dark mode est activé ou non. */
    private boolean darkMode;

    /** Point d'entrée vers les fichiers de préférences. */
    private final String preferencePath = "src/main/resources/sae/transport/comparison/preferences/";


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

            HashMap<TypeCout, Double> map = new HashMap<>();
            map.put(TypeCout.CO2, 33.0);
            map.put(TypeCout.TEMPS, 33.0);
            map.put(TypeCout.PRIX, 34.0);
            instance.voyageur = new Voyageur(map);

            String themeColor = "#a855f7";
            boolean darkMode = false;
            try{
                BufferedReader br = new BufferedReader(new FileReader(instance.preferencePath + "apparence.txt"));
                themeColor = br.readLine();
                darkMode = Boolean.parseBoolean(br.readLine());
                br.close();
            }catch(IOException ignore){ignore.printStackTrace();}
            instance.themeColor = new SimpleObjectProperty<>(Color.web(themeColor));
            instance.darkMode = darkMode;
        }
        return instance;
    }

    public ObjectProperty<Color> themeColorProperty() {
        return themeColor;
    }

    public Color getThemeColor() {
        return themeColor.get();
    }

    /**
     * Change la couleur de préférence, aussi bien dans le Singleton que dans le fichier de préférences.
     *
     * @param color la couleur qui va remplacer l'ancienne.
     */
    public void setThemeColor(Color color) {
        this.themeColor.set(color);
        try{
            List<String> lignes = Files.readAllLines(Path.of(preferencePath + "apparence.txt"));
            lignes.set(0, toHexString(color));
            Files.write(Path.of(preferencePath + "apparence.txt"), lignes);
        }catch(IOException ignore){ignore.printStackTrace();}
    }


    public boolean getDarkMode(){
        return darkMode;
    }

    /**
     * Change l'état du darkmode, aussi bien dans le Singleton que dans le fichier de préférences.
     *
     * @param darkMode l'état qui va remplacer l'ancien.
     */
    public void setDarkMode(boolean darkMode){
        this.darkMode = darkMode;
        try{
            List<String> lignes = Files.readAllLines(Path.of(preferencePath + "apparence.txt"));
            lignes.set(1, String.valueOf(darkMode));
            Files.write(Path.of(preferencePath + "apparence.txt"), lignes);
        }catch(IOException ignore){ignore.printStackTrace();}
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
                appliquerTheme(newRoot, instance.getThemeColor(), instance.darkMode);
                
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
    public Lieu getVilleDepart() {
        return villeDepart;
    }

    /**
     * Définit la ville de départ pour la recherche.
     *
     * @param villeDepart le nom de la ville de départ
     */
    public void setVilleDepart(Lieu villeDepart) {
        this.villeDepart = villeDepart;
    }

    /**
     * Retourne la ville d'arrivée sélectionnée pour la recherche courante.
     *
     * @return le nom de la ville d'arrivée, ou {@code null}
     */
    public Lieu getVilleArrivee() {
        return villeArrivee;
    }

    /**
     * Définit la ville d'arrivée pour la recherche.
     *
     * @param villeArrivee le nom de la ville d'arrivée
     */
    public void setVilleArrivee(Lieu villeArrivee) {
        this.villeArrivee = villeArrivee;
    }

    public List<Chemin> getMultiGraphe() {
        return multiGraphe;
    }

    public void setMultiGraphe(List<Chemin> multiGraphe) {
        this.multiGraphe = multiGraphe;
    }

    // ---------------------------------------------------------------
    // Thème Dynamique
    // ---------------------------------------------------------------

    /**
     * Applique la couleur de thème au composant racine.
     * Génère les variables CSS (looked-up colors).
     *
     * @param root La scene sur laquelle appliquer les changements.
     * @param baseColor La couleur à appliquer.
     * @param darkMode L'état du darkMode
     */
    public void appliquerTheme(Parent root, Color baseColor, boolean darkMode) {
        if (root == null || baseColor == null)return;
        
        Color lightColor = baseColor.deriveColor(0, 0.8, 1.2, 1.0);
        Color hoverColor = baseColor.deriveColor(0, 1.0, 0.9, 1.0);
        Color lightHover = lightColor.deriveColor(0, 1.0, 0.9, 1.0);
        Color bgLight = baseColor.deriveColor(0, 0.2, 2.5, 1.0);
        Color shadowColor = new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 0.35);

        String style = String.format(
            "-fx-primary-base: %s; " +
            "-fx-primary-light: %s; " +
            "-fx-primary-hover: %s; " +
            "-fx-primary-light-hover: %s; " +
            "-fx-primary-bg: %s; " +
            "-fx-primary-shadow: %s;",
            toHexString(baseColor),
            toHexString(lightColor),
            toHexString(hoverColor),
            toHexString(lightHover),
            toHexString(bgLight),
            toRgbaString(shadowColor)
        );
        
        // Conserve les styles existants (si c'est un Node avec d'autres styles inline)
        String currentStyle = root.getStyle();
        // Retire les anciennes variables pour éviter l'accumulation
        currentStyle = currentStyle.replaceAll("-fx-primary-[^;]+;\\s*", "");
        root.setStyle(currentStyle + style);
        if(darkMode){
            root.setBlendMode(BlendMode.DIFFERENCE);
        }else{
            root.setBlendMode(BlendMode.SRC_OVER);
        }
    }

    /**
     * Traduit une couleur en une chaine de caractère au format Hexadecimal. Exemple : #f335ab
     *
     * @param color La couleur qui va être traduite.
     */
    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
            (int) (color.getRed() * 255),
            (int) (color.getGreen() * 255),
            (int) (color.getBlue() * 255));
    }

    /**
     * Traduit une couleur en une chaine de caractère au format RGBA, ou Rouge, Vert, Bleu et Opacité. Rouge, Vert et Bleu
     * peuvent fluctuer entre 0 et 255, tandis que Opacité fluctue entre 0 et 1. Exemple : rgba(150, 0, 255, 0.9)
     *
     * @param color La couleur qui va être traduite.
     */
    private String toRgbaString(Color color) {
        return String.format("rgba(%d,%d,%d,%f)",
            (int) (color.getRed() * 255),
            (int) (color.getGreen() * 255),
            (int) (color.getBlue() * 255),
            color.getOpacity());
    }
}
