package sae.transport.comparison.graphs;

import java.util.List;

import fr.ulille.but.sae_s2_2026.AlgorithmeKPCC;
import fr.ulille.but.sae_s2_2026.Chemin;
import fr.ulille.but.sae_s2_2026.Connexion;
import fr.ulille.but.sae_s2_2026.Lieu;
import fr.ulille.but.sae_s2_2026.ModaliteTransport;
import fr.ulille.but.sae_s2_2026.MultiGrapheOrienteValue;

public class GraphesV1 {

    // Création d'un lieu en respectant les contraintes de l'interface Lieu
    private static Lieu lieu(String nom) {
        return new Lieu() {
            @Override public String toString() { return nom; }
        };
    }

    // Création d'une connexion en respectant les contraintes de l'interface Connexion
    private static Connexion connexion(Lieu depart, Lieu arrivee, ModaliteTransport mode) {
        return new Connexion() {
            @Override public Lieu getDepart()                { return depart; }
            @Override public Lieu getArrivee()               { return arrivee; }
            @Override public ModaliteTransport getModalite() { return mode; }
        };
    }

    public static void main(String[] args) {

        // Scénario : mode TRAIN, critère PRIX, Lille -> Crépy-en-Valois, k=4

        Lieu lille      = lieu("Lille");
        Lieu dunkerque  = lieu("Dunkerque");
        Lieu cambrai    = lieu("Cambrai");
        Lieu paris      = lieu("Paris");
        Lieu crepy      = lieu("Crépy-en-Valois");

        // Connexions TRAIN du tableau 1 du rapport — poids = prix (€)
        Connexion lilleDunkerque   = connexion(lille,      dunkerque, ModaliteTransport.TRAIN);
        Connexion dunkerqueLille   = connexion(dunkerque,  lille,     ModaliteTransport.TRAIN);
        Connexion lilleCambrai     = connexion(lille,      cambrai,   ModaliteTransport.TRAIN);
        Connexion cambraiLille     = connexion(cambrai,    lille,     ModaliteTransport.TRAIN);
        Connexion cambraiParis     = connexion(cambrai,    paris,     ModaliteTransport.TRAIN);
        Connexion parisCambrai     = connexion(paris,      cambrai,   ModaliteTransport.TRAIN);
        Connexion parisCrepy       = connexion(paris,      crepy,     ModaliteTransport.TRAIN);
        Connexion crepyParis       = connexion(crepy,      paris,     ModaliteTransport.TRAIN);
        Connexion lilleParis       = connexion(lille,      paris,     ModaliteTransport.TRAIN);
        Connexion parisLille       = connexion(paris,      lille,     ModaliteTransport.TRAIN);
        Connexion dunkerqueCambrai = connexion(dunkerque,  cambrai,   ModaliteTransport.TRAIN);
        Connexion cambraiDunkerque = connexion(cambrai,    dunkerque, ModaliteTransport.TRAIN);
        Connexion dunkerqueCrepy   = connexion(dunkerque,  crepy,     ModaliteTransport.TRAIN);

        // Construction du graphe
        MultiGrapheOrienteValue graphe = new MultiGrapheOrienteValue();

        graphe.ajouterSommet(lille);
        graphe.ajouterSommet(dunkerque);
        graphe.ajouterSommet(cambrai);
        graphe.ajouterSommet(paris);
        graphe.ajouterSommet(crepy);

        graphe.ajouterArete(lilleDunkerque,   18.0);
        graphe.ajouterArete(dunkerqueLille,   18.0);
        graphe.ajouterArete(lilleCambrai,     14.0);
        graphe.ajouterArete(cambraiLille,     14.0);
        graphe.ajouterArete(cambraiParis,     35.0);
        graphe.ajouterArete(parisCambrai,     35.0);
        graphe.ajouterArete(parisCrepy,       12.0);
        graphe.ajouterArete(crepyParis,       12.0);
        graphe.ajouterArete(lilleParis,       55.0);
        graphe.ajouterArete(parisLille,       55.0);
        graphe.ajouterArete(dunkerqueCambrai, 22.0);
        graphe.ajouterArete(cambraiDunkerque, 22.0);
        graphe.ajouterArete(dunkerqueCrepy,   45.0);

        // Calcul des 4 meilleurs itinéraires de Lille à Crépy-en-Valois
        List<Chemin> chemins = AlgorithmeKPCC.kpcc(graphe, lille, crepy, 4);

        System.out.println("=== " + chemins.size() + " meilleur(s) itinéraire(s) TRAIN de Lille à Crépy-en-Valois (critère : PRIX) ===\n");

        if (chemins.isEmpty()) {
            System.out.println("Aucun itinéraire trouvé.");
            return;
        }

        int rang = 1;
        for (Chemin chemin : chemins) {
            System.out.print("Itinéraire " + rang + " - " + (int) chemin.poids() + " € - ");
            List<Connexion> aretes = chemin.aretes();
            for (int i = 0; i < aretes.size(); i++) {
                if (i == 0) System.out.print(aretes.get(i).getDepart());
                System.out.print(" -> " + aretes.get(i).getArrivee());
            }
            System.out.println();
            rang++;
        }
    }
}
