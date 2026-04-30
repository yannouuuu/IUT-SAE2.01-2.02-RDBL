package sae.transport.comparison.models;

/**
 * Enumération des critères de coût utilisés pour évaluer et comparer les trajets.
 * Chaque valeur représente une dimension différente du coût d'un voyage.
 */
public enum TypeCout {
    /** Émissions de gaz à effet de serre, exprimées en kilogrammes CO2 équivalent (kg CO2e). */
    CO2,

    /** Durée du trajet, exprimée en minutes. */
    TEMPS,

    /** Prix du trajet, exprimé en euros (€). */
    PRIX
}