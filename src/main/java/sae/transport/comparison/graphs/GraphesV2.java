package sae.transport.comparison.graphs;

import java.util.ArrayList;
import java.util.List;

import fr.ulille.but.sae_s2_2026.AlgorithmeKPCC;
import fr.ulille.but.sae_s2_2026.Chemin;
import fr.ulille.but.sae_s2_2026.Connexion;
import fr.ulille.but.sae_s2_2026.Lieu;
import fr.ulille.but.sae_s2_2026.ModaliteTransport;
import fr.ulille.but.sae_s2_2026.MultiGrapheOrienteValue;

/**
 * Prototype Version 2 — multi-modalité avec coûts de correspondance.
 *
 * Technique des sommets dupliqués :
 *   Chaque ville est représentée par autant de sommets que de modes de transport disponibles.
 *   Ex : Lille devient Lille_TRAIN, Lille_AVION, Lille_BUS.
 *
 * Scénario : Lille → Crépy-en-Valois, critère PRIX, k = 4 itinéraires.
 */
public class GraphesV2 {

    // -------------------------------------------------------------------------
    // Méthodes pour créer des Lieu et Connexion
    // -------------------------------------------------------------------------

    private static Lieu creerLieu(String nom) {
        return new Lieu() {
            @Override
            public String toString() {
                return nom;
            }
        };
    }

    private static Connexion creerConnexion(Lieu depart, Lieu arrivee, ModaliteTransport mode) {
        return new Connexion() {
            @Override public Lieu getDepart()                { return depart; }
            @Override public Lieu getArrivee()               { return arrivee; }
            @Override public ModaliteTransport getModalite() { return mode; }
        };
    }

    // -------------------------------------------------------------------------
    // Méthode d'affichage : ne montre que les points d'intérêt
    // (départ, arrivée, et lieux de changement de modalité)
    // -------------------------------------------------------------------------

    private static void afficherItineraire(int rang, Chemin chemin) {
        System.out.print("Itinéraire " + rang + " — " + (int) chemin.poids() + " € — ");

        List<Connexion> aretes = chemin.aretes();

        for (int i = 0; i < aretes.size(); i++) {
            Connexion arete = aretes.get(i);

            String nomDepart  = arete.getDepart().toString();
            String nomArrivee = arete.getArrivee().toString();

            // Extraire le nom de la ville (avant le "_") et le mode (après le "_")
            String villeDepart  = nomDepart.substring(0, nomDepart.lastIndexOf("_"));
            String villeArrivee = nomArrivee.substring(0, nomArrivee.lastIndexOf("_"));
            String modeDepart   = nomDepart.substring(nomDepart.lastIndexOf("_") + 1);
            String modeArrivee  = nomArrivee.substring(nomArrivee.lastIndexOf("_") + 1);

            // Si départ et arrivée sont la même ville c'est une correspondance
            if (villeDepart.equals(villeArrivee)) {
                System.out.print("[corresp. " + modeDepart + " → " + modeArrivee + "] ");
            }

            // Premier tronçon : afficher aussi la ville de départ
            if (i == 0) {
                System.out.print(villeDepart + " (" + modeDepart + ")");
            }

            System.out.print(" → " + villeArrivee + " (" + modeArrivee + ")");
        }

        System.out.println();
    }

    // -------------------------------------------------------------------------
    // Tri des chemins par poids croissant
    // -------------------------------------------------------------------------

    private static void trierParPoidsAscendant(List<Chemin> chemins) {
        for (int i = 0; i < chemins.size() - 1; i++) {
            int indexMin = i;
            for (int j = i + 1; j < chemins.size(); j++) {
                if (chemins.get(j).poids() < chemins.get(indexMin).poids()) {
                    indexMin = j;
                }
            }
            Chemin temporaire    = chemins.get(i);
            chemins.set(i,        chemins.get(indexMin));
            chemins.set(indexMin, temporaire);
        }
    }

    // -------------------------------------------------------------------------
    // Garde uniquement les k premiers éléments d'une liste
    // -------------------------------------------------------------------------

    private static List<Chemin> garderLesK(List<Chemin> chemins, int k) {
        List<Chemin> resultat = new ArrayList<>();
        for (int i = 0; i < chemins.size() && i < k; i++) {
            resultat.add(chemins.get(i));
        }
        return resultat;
    }

    // -------------------------------------------------------------------------
    // Main
    // -------------------------------------------------------------------------

    public static void main(String[] args) {

        // Sommets dupliqués : une entrée par ville ET par mode de transport 

        // Lille : accessible en TRAIN, AVION et BUS
        Lieu lilleTrain   = creerLieu("Lille_TRAIN");
        Lieu lilleAvion   = creerLieu("Lille_AVION");
        Lieu lilleBus     = creerLieu("Lille_BUS");

        // Paris : accessible en TRAIN, AVION et BUS
        Lieu parisTrain   = creerLieu("Paris_TRAIN");
        Lieu parisAvion   = creerLieu("Paris_AVION");
        Lieu parisBus     = creerLieu("Paris_BUS");

        // Dunkerque : accessible en TRAIN et AVION uniquement
        Lieu dunkerqueTrain = creerLieu("Dunkerque_TRAIN");
        Lieu dunkerqueAvion = creerLieu("Dunkerque_AVION");

        // Cambrai : accessible en TRAIN et BUS uniquement
        Lieu cambraiTrain = creerLieu("Cambrai_TRAIN");
        Lieu cambraiBus   = creerLieu("Cambrai_BUS");

        // Crépy-en-Valois : destination, accessible en TRAIN et BUS
        Lieu crepyTrain   = creerLieu("Crépy-en-Valois_TRAIN");
        Lieu crepyBus     = creerLieu("Crépy-en-Valois_BUS");


        // --- Arêtes TRAIN (poids = prix en €) ---

        Connexion lilleTrain_dunkerqueTrain   = creerConnexion(lilleTrain,     dunkerqueTrain, ModaliteTransport.TRAIN);
        Connexion dunkerqueTrain_lilleTrain   = creerConnexion(dunkerqueTrain, lilleTrain,     ModaliteTransport.TRAIN);
        Connexion lilleTrain_cambraiTrain     = creerConnexion(lilleTrain,     cambraiTrain,   ModaliteTransport.TRAIN);
        Connexion cambraiTrain_lilleTrain     = creerConnexion(cambraiTrain,   lilleTrain,     ModaliteTransport.TRAIN);
        Connexion lilleTrain_parisTrain       = creerConnexion(lilleTrain,     parisTrain,     ModaliteTransport.TRAIN);
        Connexion parisTrain_lilleTrain       = creerConnexion(parisTrain,     lilleTrain,     ModaliteTransport.TRAIN);
        Connexion cambraiTrain_parisTrain     = creerConnexion(cambraiTrain,   parisTrain,     ModaliteTransport.TRAIN);
        Connexion parisTrain_cambraiTrain     = creerConnexion(parisTrain,     cambraiTrain,   ModaliteTransport.TRAIN);
        Connexion parisTrain_crepyTrain       = creerConnexion(parisTrain,     crepyTrain,     ModaliteTransport.TRAIN);
        Connexion crepyTrain_parisTrain       = creerConnexion(crepyTrain,     parisTrain,     ModaliteTransport.TRAIN);
        Connexion dunkerqueTrain_cambraiTrain = creerConnexion(dunkerqueTrain, cambraiTrain,   ModaliteTransport.TRAIN);
        Connexion cambraiTrain_dunkerqueTrain = creerConnexion(cambraiTrain,   dunkerqueTrain, ModaliteTransport.TRAIN);
        Connexion dunkerqueTrain_crepyTrain   = creerConnexion(dunkerqueTrain, crepyTrain,     ModaliteTransport.TRAIN);


        // --- Arêtes AVION (poids = prix en €) ---

        Connexion lilleAvion_parisAvion       = creerConnexion(lilleAvion,     parisAvion,     ModaliteTransport.AVION);
        Connexion parisAvion_lilleAvion       = creerConnexion(parisAvion,     lilleAvion,     ModaliteTransport.AVION);
        Connexion dunkerqueAvion_parisAvion   = creerConnexion(dunkerqueAvion, parisAvion,     ModaliteTransport.AVION);
        Connexion parisAvion_dunkerqueAvion   = creerConnexion(parisAvion,     dunkerqueAvion, ModaliteTransport.AVION);


        // --- Arêtes BUS (poids = prix en €) ---

        Connexion parisBus_crepyBus           = creerConnexion(parisBus,   crepyBus,   ModaliteTransport.BUS);
        Connexion crepyBus_parisBus           = creerConnexion(crepyBus,   parisBus,   ModaliteTransport.BUS);
        Connexion cambraiBus_crepyBus         = creerConnexion(cambraiBus, crepyBus,   ModaliteTransport.BUS);
        Connexion crepyBus_cambraiBus         = creerConnexion(crepyBus,   cambraiBus, ModaliteTransport.BUS);


        // --- Arêtes de correspondance Lille (poids = prix du changement en €) ---

        Connexion lilleTrain_lilleAvion = creerConnexion(lilleTrain, lilleAvion, ModaliteTransport.TRAIN);
        Connexion lilleAvion_lilleTrain = creerConnexion(lilleAvion, lilleTrain, ModaliteTransport.AVION);
        Connexion lilleTrain_lilleBus   = creerConnexion(lilleTrain, lilleBus,   ModaliteTransport.TRAIN);
        Connexion lilleBus_lilleTrain   = creerConnexion(lilleBus,   lilleTrain, ModaliteTransport.BUS);
        Connexion lilleAvion_lilleBus   = creerConnexion(lilleAvion, lilleBus,   ModaliteTransport.AVION);
        Connexion lilleBus_lilleAvion   = creerConnexion(lilleBus,   lilleAvion, ModaliteTransport.BUS);


        // --- Arêtes de correspondance Paris (poids = prix du changement en €) ---

        Connexion parisTrain_parisAvion = creerConnexion(parisTrain, parisAvion, ModaliteTransport.TRAIN);
        Connexion parisAvion_parisTrain = creerConnexion(parisAvion, parisTrain, ModaliteTransport.AVION);
        Connexion parisTrain_parisBus   = creerConnexion(parisTrain, parisBus,   ModaliteTransport.TRAIN);
        Connexion parisBus_parisTrain   = creerConnexion(parisBus,   parisTrain, ModaliteTransport.BUS);
        Connexion parisAvion_parisBus   = creerConnexion(parisAvion, parisBus,   ModaliteTransport.AVION);
        Connexion parisBus_parisAvion   = creerConnexion(parisBus,   parisAvion, ModaliteTransport.BUS);


        // --- Construction du graphe ---

        MultiGrapheOrienteValue graphe = new MultiGrapheOrienteValue();

        graphe.ajouterSommet(lilleTrain);
        graphe.ajouterSommet(lilleAvion);
        graphe.ajouterSommet(lilleBus);
        graphe.ajouterSommet(parisTrain);
        graphe.ajouterSommet(parisAvion);
        graphe.ajouterSommet(parisBus);
        graphe.ajouterSommet(dunkerqueTrain);
        graphe.ajouterSommet(dunkerqueAvion);
        graphe.ajouterSommet(cambraiTrain);
        graphe.ajouterSommet(cambraiBus);
        graphe.ajouterSommet(crepyTrain);
        graphe.ajouterSommet(crepyBus);

        // TRAIN
        graphe.ajouterArete(lilleTrain_dunkerqueTrain,   18.0);
        graphe.ajouterArete(dunkerqueTrain_lilleTrain,   18.0);
        graphe.ajouterArete(lilleTrain_cambraiTrain,     14.0);
        graphe.ajouterArete(cambraiTrain_lilleTrain,     14.0);
        graphe.ajouterArete(lilleTrain_parisTrain,       55.0);
        graphe.ajouterArete(parisTrain_lilleTrain,       55.0);
        graphe.ajouterArete(cambraiTrain_parisTrain,     35.0);
        graphe.ajouterArete(parisTrain_cambraiTrain,     35.0);
        graphe.ajouterArete(parisTrain_crepyTrain,       12.0);
        graphe.ajouterArete(crepyTrain_parisTrain,       12.0);
        graphe.ajouterArete(dunkerqueTrain_cambraiTrain, 22.0);
        graphe.ajouterArete(cambraiTrain_dunkerqueTrain, 22.0);
        graphe.ajouterArete(dunkerqueTrain_crepyTrain,   45.0);

        // AVION
        graphe.ajouterArete(lilleAvion_parisAvion,     120.0);
        graphe.ajouterArete(parisAvion_lilleAvion,     120.0);
        graphe.ajouterArete(dunkerqueAvion_parisAvion, 110.0);
        graphe.ajouterArete(parisAvion_dunkerqueAvion, 110.0);

        // BUS
        graphe.ajouterArete(parisBus_crepyBus,    8.0);
        graphe.ajouterArete(crepyBus_parisBus,    8.0);
        graphe.ajouterArete(cambraiBus_crepyBus,  18.0);
        graphe.ajouterArete(crepyBus_cambraiBus,  18.0);

        // Correspondances Lille
        graphe.ajouterArete(lilleTrain_lilleAvion, 20.0);
        graphe.ajouterArete(lilleAvion_lilleTrain, 20.0);
        graphe.ajouterArete(lilleTrain_lilleBus,    0.0);
        graphe.ajouterArete(lilleBus_lilleTrain,    0.0);
        graphe.ajouterArete(lilleAvion_lilleBus,   20.0);
        graphe.ajouterArete(lilleBus_lilleAvion,   20.0);

        // Correspondances Paris
        graphe.ajouterArete(parisTrain_parisAvion, 20.0);
        graphe.ajouterArete(parisAvion_parisTrain, 20.0);
        graphe.ajouterArete(parisTrain_parisBus,    0.0);
        graphe.ajouterArete(parisBus_parisTrain,    0.0);
        graphe.ajouterArete(parisAvion_parisBus,   20.0);
        graphe.ajouterArete(parisBus_parisAvion,   20.0);


        // --- Calcul des meilleurs itinéraires ---
        // On calcule séparément vers crepyTrain et crepyBus
        // car la destination peut être atteinte par deux modes différents

        List<Chemin> cheminsVersCrepyTrain = AlgorithmeKPCC.kpcc(graphe, lilleTrain, crepyTrain, 4);
        List<Chemin> cheminsVersCrepyBus   = AlgorithmeKPCC.kpcc(graphe, lilleTrain, crepyBus,   4);

        List<Chemin> tousLesChemins = new ArrayList<>();
        tousLesChemins.addAll(cheminsVersCrepyTrain);
        tousLesChemins.addAll(cheminsVersCrepyBus);

        trierParPoidsAscendant(tousLesChemins);

        List<Chemin> top4 = garderLesK(tousLesChemins, 4);


        // Affichage

        System.out.println("=== " + top4.size() + " meilleur(s) itinéraire(s) de Lille à Crépy-en-Valois (critère : PRIX, multi-modal) ===\n");

        if (top4.isEmpty()) {
            System.out.println("Aucun itinéraire trouvé.");
            return;
        }

        int rang = 1;
        for (Chemin chemin : top4) {
            afficherItineraire(rang, chemin);
            rang++;
        }
    }
}