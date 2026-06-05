package sae.transport.comparison.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Représente un voyage effectué par un voyageur.
 * Contient la liste des trajets empruntés et permet
 * de calculer les coûts totaux selon chaque critère.
 * Implémente Serializable pour la persistance de l'historique.
 */
public class Voyage implements Serializable {

    private final List<Trajet> trajets;
    private final String villeDepart;
    private final String villeArrivee;

    /**
     * Construit un voyage à partir d'une liste de trajets.
     *
     * @param trajets la liste des trajets composant le voyage
     */
    public Voyage(List<Trajet> trajets) {
        this.trajets = new ArrayList<>(trajets);
        this.villeDepart = trajets.isEmpty() ? "" : trajets.get(0).getDepart().toString();
        this.villeArrivee = trajets.isEmpty() ? "" : trajets.get(trajets.size() - 1).getArrivee().toString();
    }

    /**
     * Retourne le coût total du voyage pour un critère donné.
     *
     * @param type le critère de coût à évaluer (TEMPS, PRIX ou CO2)
     * @return le coût total pour ce critère
     */
    public double getCoutTotal(TypeCout type) {
        double total = 0.0;
        for (Trajet trajet : trajets) {
            total += trajet.getCout().getValeur(type);
        }
        return total;
    }

    /**
     * Retourne la liste des trajets du voyage.
     *
     * @return la liste des trajets
     */
    public List<Trajet> getTrajets() {
        return new ArrayList<>(trajets);
    }

    /**
     * Retourne la ville de départ du voyage.
     *
     * @return le nom de la ville de départ
     */
    public String getVilleDepart() {
        return villeDepart;
    }

    /**
     * Retourne la ville d'arrivée du voyage.
     *
     * @return le nom de la ville d'arrivée
     */
    public String getVilleArrivee() {
        return villeArrivee;
    }

    /**
     * Retourne une représentation textuelle du voyage, incluant le départ,
     * l'arrivée, et les coûts totaux.
     *
     * @return la représentation du voyage sous forme de chaîne de caractères
     */
    @Override
    public String toString() {
        return villeDepart + " → " + villeArrivee
                + " | Prix: " + String.format("%.2f", getCoutTotal(TypeCout.PRIX)) + "€"
                + " | Temps: " + String.format("%.0f", getCoutTotal(TypeCout.TEMPS)) + "min"
                + " | CO2: " + String.format("%.2f", getCoutTotal(TypeCout.CO2)) + "kg";
    }
}
