package sae.transport.comparison.models;

/**
 * Représente un utilisateur de la plateforme de voyage.
 * Un voyageur est caractérisé par son nom et le critère de coût
 * qu'il souhaite optimiser lors de la comparaison d'itinéraires.
 */
public class Voyageur {
    private String nom;
    private TypeCout typeCout;

    /**
     * Construit un voyageur avec un nom et un critère d'optimisation.
     *
     * @param nom      le nom du voyageur
     * @param typeCout le critère de coût à optimiser (TEMPS, PRIX ou CO2)
     */
    public Voyageur(String nom, TypeCout typeCout) {
        this.nom = nom;
        this.typeCout = typeCout;
    }

    /**
     * Retourne le nom du voyageur.
     *
     * @return le nom du voyageur
     */
    public String getNom() {
        return nom;
    }

    /**
     * Retourne le critère de coût que le voyageur souhaite optimiser.
     *
     * @return le critère de coût
     */
    public TypeCout getTypeCout() {
        return typeCout;
    }

    /**
     * Modifie le critère de coût que le voyageur souhaite optimiser.
     *
     * @param typeCout le nouveau critère de coût
     */
    public void setTypeCout(TypeCout typeCout) {
        this.typeCout = typeCout;
    }
}