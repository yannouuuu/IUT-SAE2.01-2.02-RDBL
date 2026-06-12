package sae.transport.comparison.models;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * Représente un utilisateur de la plateforme de voyage.
 * En V3, le voyageur peut exprimer des préférences multi-critères
 * et dispose d'un historique de ses voyages sérialisable.
 */
public class Voyageur implements Serializable {

    private String nom;
    private TypeCout typeCout;
    private Map<TypeCout, Double> preferences;
    private List<Voyage> historique;

    /**
     * Constructeur V1/V2 — un seul critère.
     *
     * @param nom      le nom du voyageur
     * @param typeCout le critère de coût préféré (TEMPS, PRIX ou CO2)
     */
    public Voyageur(String nom, TypeCout typeCout) {
        this.nom = nom;
        this.typeCout = typeCout;
        this.preferences = new EnumMap<>(TypeCout.class);
        this.historique = new ArrayList<>();
        initPreferencesEgales();
    }

    /**
     * Constructeur V3 — préférences multi-critères avec poids relatifs.
     *
     * @param nom         le nom du voyageur
     * @param preferences la carte des poids pour chaque critère de coût
     */
    public Voyageur(String nom, Map<TypeCout, Double> preferences) {
        this.nom = nom;
        this.typeCout = null;
        this.preferences = new EnumMap<>(preferences);
        this.historique = new ArrayList<>();
        normaliserPreferences();
    }

    public Voyageur(Map<TypeCout, Double> preferences) {
        this.typeCout = null;
        this.preferences = new EnumMap<>(preferences);
        this.historique = new ArrayList<>();
        normaliserPreferences();
    }

    /**
     * Initialise les préférences à égalité (1/3 chacun).
     */
    private void initPreferencesEgales() {
        preferences.put(TypeCout.PRIX, 1.0 / 3.0);
        preferences.put(TypeCout.TEMPS, 1.0 / 3.0);
        preferences.put(TypeCout.CO2, 1.0 / 3.0);
    }

    /**
     * Normalise les préférences pour que leur somme soit égale à 1.
     */
    private void normaliserPreferences() {
        double somme = 0.0;
        for (double poids : preferences.values()) {
            somme += poids;
        }
        if (somme > 0) {
            for (TypeCout type : preferences.keySet()) {
                preferences.put(type, preferences.get(type) / somme);
            }
        }
    }

    /**
     * Calcule le coût composite d'un trajet selon les préférences du voyageur.
     * Chaque critère est pondéré par son poids relatif.
     *
     * @param cout le coût à évaluer
     * @return le coût composite calculé
     */
    public double calculerCoutComposite(Cout cout) {
        double total = 0.0;
        for (TypeCout type : TypeCout.values()) {
            double poids = preferences.getOrDefault(type, 0.0);
            total += poids * cout.getValeur(type);
        }
        return total;
    }

    /**
     * Ajoute un voyage à l'historique du voyageur.
     *
     * @param voyage le voyage à ajouter
     */
    public void ajouterVoyage(Voyage voyage) {
        historique.add(voyage);
    }

    /**
     * Vide l'historique complet des voyages en mémoire.
     */
    public void viderHistorique() {
        historique.clear();
    }

    /**
     * Retourne l'historique complet des voyages.
     *
     * @return la liste des voyages de l'historique
     */
    public List<Voyage> getHistorique() {
        return new ArrayList<>(historique);
    }

    /**
     * Retourne la somme totale d'un critère sur tous les voyages de l'historique.
     *
     * @param type le critère de coût à sommer
     * @return le total du coût sur l'historique
     */
    public double getTotalHistorique(TypeCout type) {
        double total = 0.0;
        for (Voyage voyage : historique) {
            total += voyage.getCoutTotal(type);
        }
        return total;
    }

    /**
     * Retourne le nom du voyageur.
     *
     * @return le nom du voyageur
     */
    public String getNom() { return nom; }

    /**
     * Retourne le critère de coût principal (V1/V2).
     *
     * @return le critère de coût principal
     */
    public TypeCout getTypeCout() { return typeCout; }

    /**
     * Modifie le critère de coût principal (V1/V2).
     *
     * @param typeCout le nouveau critère de coût principal
     */
    public void setTypeCout(TypeCout typeCout) { this.typeCout = typeCout; }

    /**
     * Retourne une copie des préférences multi-critères du voyageur.
     *
     * @return la carte des préférences
     */
    public Map<TypeCout, Double> getPreferences() { return new EnumMap<>(preferences); }

    /**
     * Modifie les préférences multi-critères du voyageur et les normalise.
     *
     * @param preferences les nouvelles préférences
     */
    public void setPreferences(Map<TypeCout, Double> preferences) {
        this.preferences = new EnumMap<>(preferences);
        normaliserPreferences();
    }
}