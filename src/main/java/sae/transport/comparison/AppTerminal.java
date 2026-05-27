package sae.transport.comparison;

import fr.ulille.but.sae_s2_2026.ModaliteTransport;
import sae.transport.comparison.exceptions.AucunCheminException;
import sae.transport.comparison.exceptions.DonneesInvalidesException;
import sae.transport.comparison.models.*;

import java.util.List;

public class AppTerminal {

    public static void main(String[] args) {

        // --- Données de démonstration ---
        String[] data = {
                "Lille;Dunkerque;TRAIN;18;0.8;65",
                "Dunkerque;Lille;TRAIN;18;0.8;65",
                "Lille;Cambrai;TRAIN;14;0.7;55",
                "Cambrai;Lille;TRAIN;14;0.7;55",
                "Cambrai;Paris;TRAIN;35;1.8;95",
                "Paris;Cambrai;TRAIN;35;1.8;95",
                "Paris;Crépy-en-Valois;TRAIN;12;0.5;45",
                "Crépy-en-Valois;Paris;TRAIN;12;0.5;45",
                "Lille;Paris;TRAIN;55;2.4;60",
                "Paris;Lille;TRAIN;55;2.4;60",
                "Dunkerque;Cambrai;TRAIN;22;1.0;80",
                "Cambrai;Dunkerque;TRAIN;22;1.0;80",
                "Dunkerque;Crépy-en-Valois;TRAIN;45;2.0;150",
                "Dunkerque;Cambrai;BUS;12;2.1;110",
                "Cambrai;Crépy-en-Valois;BUS;18;2.8;130",
                "Paris;Crépy-en-Valois;BUS;8;1.2;75",
                "Lille;Paris;AVION;120;85.0;55",
                "Dunkerque;Paris;AVION;110;80.0;50"
        };

        Plateforme plateforme = new Plateforme();

        try {
            // Chargement des données
            plateforme.chargerDepuisTableau(data);
            System.out.println("Réseau chargé : " + plateforme.getVilles().size() + " villes, " + plateforme.getTrajets().size() + " trajets.");

            // Scénario : voyageur qui veut minimiser le prix en TRAIN
            Voyageur voyageur = new Voyageur("Alice", TypeCout.PRIX);
            System.out.println("\nVoyageur : " + voyageur.getNom() + " (critère : " + voyageur.getTypeCout() + ")");

            // Filtrage par modalité TRAIN
            List<Trajet> trajetsTrain = plateforme.filtrerParModalite(ModaliteTransport.TRAIN);
            System.out.println("Trajets TRAIN disponibles : " + trajetsTrain.size());

            // Vérification qu'un chemin existe
            boolean existe = plateforme.cheminExiste("Lille", "Paris", ModaliteTransport.TRAIN);
            if (existe) {
                System.out.println("\nChemin TRAIN Lille → Paris : existe");
            } else {
                System.out.println("\nChemin TRAIN Lille → Paris : inexistant");
            }

            // Trajets triés par prix
            List<Trajet> tris = plateforme.getTrajetsTries(TypeCout.PRIX);
            System.out.println("\nTrajets triés par prix (les 5 premiers) :");
            for (int i = 0; i < Math.min(5, tris.size()); i++) {
                Trajet t = tris.get(i);
                System.out.println("  " + t.getDepart() + " >> " + t.getArrivee()
                        + " [" + t.getModalite() + "] "
                        + t.getCout().getValeur(TypeCout.PRIX) + " €");
            }

            // Filtrage par borne (max 20€)
            List<Trajet> bornes = plateforme.filtrerParBorne(tris, TypeCout.PRIX, 20.0);
            System.out.println("\nTrajets à moins de 20€ : " + bornes.size());

        } catch (DonneesInvalidesException e) {
            System.err.println("Erreur de données : " + e.getMessage());
        } catch (AucunCheminException e) {
            System.err.println("Aucun chemin : " + e.getMessage());
        }
    }
}
