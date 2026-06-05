package sae.transport.comparison.models;

import java.io.Serializable;

import fr.ulille.but.sae_s2_2026.Lieu;

/**
 * Représente une ville du réseau de transport.
 * Implémente l'interface {@link Lieu} fournie par la bibliothèque IUT.
 * Deux villes sont considérées égales si elles ont le même nom.
 */
public class Ville implements Lieu, Serializable {
    private final String nom;

    /**
     * Construit une ville avec le nom donné.
     *
     * @param nom le nom de la ville
     */
    public Ville(String nom) {
        this.nom = nom;
    }

    /**
     * Retourne le nom de la ville.
     *
     * @return le nom de la ville
     */
    public String getNom() {
        return this.nom;
    }

    /**
     * Vérifie l'égalité entre deux villes sur la base de leur nom.
     *
     * @param o l'objet à comparer
     * @return {@code true} si les deux villes ont le même nom, {@code false} sinon
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Ville ville = (Ville) o;
        return nom.equals(ville.nom);
    }

    /**
     * Retourne le code de hachage de la ville basé sur son nom.
     *
     * @return le code de hachage
     */
    @Override
    public int hashCode() {
        return nom.hashCode();
    }

    /**
     * Retourne une représentation textuelle de la ville (son nom).
     *
     * @return le nom de la ville
     */
    @Override
    public String toString() {
        return this.nom;
    }
}