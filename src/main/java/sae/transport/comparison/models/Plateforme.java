package sae.transport.comparison.models;

import java.util.ArrayList;
import java.util.List;

import fr.ulille.but.sae_s2_2026.ModaliteTransport;
import sae.transport.comparison.exceptions.AucunCheminException;
import sae.transport.comparison.exceptions.DonneesInvalidesException;

/**
 * Représente la plateforme centrale du réseau de transport.
 * Regroupe l'ensemble des villes et des trajets disponibles,
 * et fournit les fonctionnalités de chargement, filtrage et tri des données.
 */
public class Plateforme {
    private List<Trajet> trajets;
    private List<Ville> villes;

    /**
     * Construit une plateforme vide sans villes ni trajets.
     */
    public Plateforme() {
        this.trajets = new ArrayList<>();
        this.villes = new ArrayList<>();
    }

    /**
     * Retourne la liste de tous les trajets du réseau.
     *
     * @return la liste des trajets
     */
    public List<Trajet> getTrajets() {
        return trajets;
    }

    /**
     * Retourne la liste de toutes les villes du réseau.
     *
     * @return la liste des villes
     */
    public List<Ville> getVilles() {
        return villes;
    }

    /**
     * Remplace la liste des trajets par celle fournie.
     *
     * @param trajets la nouvelle liste de trajets
     */
    public void setTrajets(List<Trajet> trajets) {
        this.trajets = trajets;
    }

    /**
     * Remplace la liste des villes par celle fournie.
     *
     * @param villes la nouvelle liste de villes
     */
    public void setVilles(List<Ville> villes) {
        this.villes = villes;
    }

    /**
     * Charge les données du réseau depuis un tableau de chaînes de caractères.
     * Chaque ligne doit respecter le format :
     * {@code villeDépart;villeArrivée;modalité;prix;co2;durée}
     * Les villes sont créées automatiquement si elles n'existent pas encore.
     *
     * @param data le tableau de chaînes représentant les connexions
     * @throws IllegalArgumentException si une ligne ne contient pas exactement 6 colonnes,
     *                                  si un coût est négatif, ou si la modalité est inconnue
     */
    public void chargerDepuisTableau(String[] data) throws DonneesInvalidesException {
        for (String ligne : data) {
            String[] colonnes = ligne.split(";");

            if (colonnes.length != 6) {
                throw new DonneesInvalidesException("Données invalides : " + ligne);
            }

            Ville depart = getVille(colonnes[0]);
            if (depart == null) {
                depart = new Ville(colonnes[0]);
                villes.add(depart);
            }

            Ville arrivee = getVille(colonnes[1]);
            if (arrivee == null) {
                arrivee = new Ville(colonnes[1]);
                villes.add(arrivee);
            }

            ModaliteTransport modalite = ModaliteTransport.valueOf(colonnes[2].toUpperCase());

            double prix = Double.parseDouble(colonnes[3]);
            double co2 = Double.parseDouble(colonnes[4]);
            double temps = Double.parseDouble(colonnes[5]);

            if (prix < 0 || co2 < 0 || temps < 0) {
                throw new DonneesInvalidesException("Les coûts doivent être positifs : " + ligne);
            }

            Cout cout = new Cout(prix, temps, co2);
            Trajet trajet = new Trajet(depart, arrivee, cout, modalite);
            trajets.add(trajet);
        }
    }

    /**
     * Filtre les trajets selon une modalité de transport donnée.
     *
     * @param modalite la modalité de transport souhaitée (TRAIN, AVION, BUS...)
     * @return la liste des trajets correspondant à cette modalité
     */
    public List<Trajet> filtrerParModalite(ModaliteTransport modalite) {
        List<Trajet> resultat = new ArrayList<>();
        for (Trajet trajet : trajets) {
            if (trajet.getModalite() == modalite) {
                resultat.add(trajet);
            }
        }
        return resultat;
    }

    /**
     * Recherche une ville dans le réseau par son nom.
     *
     * @param nom le nom de la ville recherchée
     * @return la ville correspondante, ou {@code null} si elle n'existe pas
     */
    public Ville getVille(String nom) {
        for (Ville ville : villes) {
            if (ville.getNom().equals(nom)) {
                return ville;
            }
        }
        return null;
    }

    /**
     * Vérifie s'il existe une connexion directe entre deux villes
     * pour une modalité de transport donnée.
     *
     * @param nomDepart  le nom de la ville de départ
     * @param nomArrivee le nom de la ville d'arrivée
     * @param modalite   la modalité de transport souhaitée
     * @return {@code true} si une connexion directe existe, {@code false} sinon
     */
    public boolean cheminExiste(String nomDepart, String nomArrivee, ModaliteTransport modalite) throws AucunCheminException {
        Ville depart = getVille(nomDepart);
        Ville arrivee = getVille(nomArrivee);

        if (depart == null || arrivee == null) {
            throw new AucunCheminException("Les villes " + nomDepart + " et " + nomArrivee + " n'existent pas.");
        }

        for (Trajet trajet : filtrerParModalite(modalite)) {
            if (trajet.getDepart().equals(depart) && trajet.getArrivee().equals(arrivee)) {
                return true;
            }
        }
        throw new AucunCheminException("Aucun chemin " + modalite + " entre " + nomDepart + " et " + nomArrivee);
    }

    /**
     * Retourne la liste de tous les trajets triés par ordre croissant
     * selon le critère de coût spécifié.
     *
     * @param critere le critère de tri (TEMPS, PRIX ou CO2)
     * @return la liste des trajets triés
     */
    public List<Trajet> getTrajetsTries(TypeCout critere) {
        List<Trajet> resultat = new ArrayList<>(trajets);

        for (int i = 0; i < resultat.size() - 1; i++) {
            int minIndex = i;
            for (int j = i + 1; j < resultat.size(); j++) {
                if (resultat.get(j).getCout().getValeur(critere) < resultat.get(minIndex).getCout().getValeur(critere)) {
                    minIndex = j;
                }
            }
            Trajet temp = resultat.get(i);
            resultat.set(i, resultat.get(minIndex));
            resultat.set(minIndex, temp);
        }
        return resultat;
    }

    /**
     * Filtre une liste de trajets en excluant ceux dont le coût
     * selon le critère donné dépasse la borne maximale.
     *
     * @param trajets  la liste de trajets à filtrer
     * @param critere  le critère de coût à vérifier (TEMPS, PRIX ou CO2)
     * @param borneMax la valeur maximale autorisée
     * @return la liste des trajets dont le coût est inférieur ou égal à {@code borneMax}
     */
    public List<Trajet> filtrerParBorne(List<Trajet> trajets, TypeCout critere, double borneMax) {
        List<Trajet> resultat = new ArrayList<>();
        for (Trajet trajet : trajets) {
            if (trajet.getCout().getValeur(critere) <= borneMax) {
                resultat.add(trajet);
            }
        }
        return resultat;
    }
}