package sae.transport.comparison.models;

import java.util.HashMap;
import java.util.Map;

/**
 * Représente le coût d'une connexion selon les différents critères d'évaluation.
 * Les valeurs sont stockées dans une table associative reliant
 * chaque {@link TypeCout} à sa valeur numérique.
 */
public class Cout {
    private final Map<TypeCout, Double> valeurs;

    /**
     * Construit un coût avec les trois valeurs associées aux critères.
     *
     * @param prix  le prix en euros (€)
     * @param temps la durée en minutes
     * @param co2   les émissions de gaz à effet de serre en kg CO2e
     */
    public Cout(double prix, double temps, double co2) {
        this.valeurs = new HashMap<>();
        this.valeurs.put(TypeCout.PRIX, prix);
        this.valeurs.put(TypeCout.TEMPS, temps);
        this.valeurs.put(TypeCout.CO2, co2);
    }

    /**
     * Retourne la valeur du coût pour un critère donné.
     *
     * @param type le critère de coût souhaité (TEMPS, PRIX ou CO2)
     * @return la valeur numérique associée à ce critère
     */
    public double getValeur(TypeCout type) {
        return this.valeurs.get(type);
    }
}