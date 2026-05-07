package sae.transport.comparison.graphs;

import java.util.List;

import fr.ulille.but.sae_s2_2026.AlgorithmeKPCC;
import fr.ulille.but.sae_s2_2026.Chemin;
import fr.ulille.but.sae_s2_2026.Connexion;
import fr.ulille.but.sae_s2_2026.Lieu;
import fr.ulille.but.sae_s2_2026.ModaliteTransport;
import fr.ulille.but.sae_s2_2026.MultiGrapheOrienteValue;

public class GraphesV1 {
    public static void main(String[] args) {

        // --- Scénario : mode TRAIN, critère PRIX, Lille -> Crépy-en-Valois, k=4 ---

        // Sommets
        Lieu lille          = new Lieu() { public String toString() { return "Lille"; } };
        Lieu dunkerque      = new Lieu() { public String toString() { return "Dunkerque"; } };
        Lieu cambrai        = new Lieu() { public String toString() { return "Cambrai"; } };
        Lieu paris          = new Lieu() { public String toString() { return "Paris"; } };
        Lieu crepy          = new Lieu() { public String toString() { return "Crépy-en-Valois"; } };

        // Connexions TRAIN (toutes les connexions TRAIN du tableau 1 du rapport)
        // prix en euros — utilisé comme poids dans le graphe
        Connexion lilleDunkerque     = new Connexion() { public Lieu getDepart() { return lille; }     public Lieu getArrivee() { return dunkerque; } public ModaliteTransport getModalite() { return ModaliteTransport.TRAIN; } };
        Connexion dunkerqueLille     = new Connexion() { public Lieu getDepart() { return dunkerque; } public Lieu getArrivee() { return lille; }      public ModaliteTransport getModalite() { return ModaliteTransport.TRAIN; } };
        Connexion lilleCambrai       = new Connexion() { public Lieu getDepart() { return lille; }     public Lieu getArrivee() { return cambrai; }    public ModaliteTransport getModalite() { return ModaliteTransport.TRAIN; } };
        Connexion cambraiLille       = new Connexion() { public Lieu getDepart() { return cambrai; }   public Lieu getArrivee() { return lille; }      public ModaliteTransport getModalite() { return ModaliteTransport.TRAIN; } };
        Connexion cambraiParis       = new Connexion() { public Lieu getDepart() { return cambrai; }   public Lieu getArrivee() { return paris; }      public ModaliteTransport getModalite() { return ModaliteTransport.TRAIN; } };
        Connexion parisCambrai       = new Connexion() { public Lieu getDepart() { return paris; }     public Lieu getArrivee() { return cambrai; }    public ModaliteTransport getModalite() { return ModaliteTransport.TRAIN; } };
        Connexion parisCrepy         = new Connexion() { public Lieu getDepart() { return paris; }     public Lieu getArrivee() { return crepy; }      public ModaliteTransport getModalite() { return ModaliteTransport.TRAIN; } };
        Connexion crepyParis         = new Connexion() { public Lieu getDepart() { return crepy; }     public Lieu getArrivee() { return paris; }      public ModaliteTransport getModalite() { return ModaliteTransport.TRAIN; } };
        Connexion lilleParis         = new Connexion() { public Lieu getDepart() { return lille; }     public Lieu getArrivee() { return paris; }      public ModaliteTransport getModalite() { return ModaliteTransport.TRAIN; } };
        Connexion parisLille         = new Connexion() { public Lieu getDepart() { return paris; }     public Lieu getArrivee() { return lille; }      public ModaliteTransport getModalite() { return ModaliteTransport.TRAIN; } };
        Connexion dunkerqueCambrai   = new Connexion() { public Lieu getDepart() { return dunkerque; } public Lieu getArrivee() { return cambrai; }    public ModaliteTransport getModalite() { return ModaliteTransport.TRAIN; } };
        Connexion cambraiDunkerque   = new Connexion() { public Lieu getDepart() { return cambrai; }   public Lieu getArrivee() { return dunkerque; }  public ModaliteTransport getModalite() { return ModaliteTransport.TRAIN; } };
        Connexion dunkerqueCrepy     = new Connexion() { public Lieu getDepart() { return dunkerque; } public Lieu getArrivee() { return crepy; }      public ModaliteTransport getModalite() { return ModaliteTransport.TRAIN; } };


        // Construction du graphe — filtre TRAIN, poids = prix (€)
        MultiGrapheOrienteValue graphe = new MultiGrapheOrienteValue();

        graphe.ajouterSommet(lille);
        graphe.ajouterSommet(dunkerque);
        graphe.ajouterSommet(cambrai);
        graphe.ajouterSommet(paris);
        graphe.ajouterSommet(crepy);

        graphe.ajouterArete(lilleDunkerque, 18.0);
        graphe.ajouterArete(dunkerqueLille, 18.0);
        graphe.ajouterArete(lilleCambrai,   14.0);
        graphe.ajouterArete(cambraiLille,   14.0);
        graphe.ajouterArete(cambraiParis,   35.0);
        graphe.ajouterArete(parisCambrai,   35.0);
        graphe.ajouterArete(parisCrepy,     12.0);
        graphe.ajouterArete(crepyParis,     12.0);
        graphe.ajouterArete(lilleParis,       55.0);
        graphe.ajouterArete(parisLille,       55.0);
        graphe.ajouterArete(dunkerqueCambrai, 22.0);
        graphe.ajouterArete(cambraiDunkerque, 22.0);
        graphe.ajouterArete(dunkerqueCrepy,   45.0);

        // Calcul des 4 meilleurs itinéraires de Lille à Crépy-en-Valois
        List<Chemin> chemins = AlgorithmeKPCC.kpcc(graphe, lille, crepy, 4);

        // Affichage
        System.out.println("=== " + chemins.size() + " meilleur(s) itinéraire(s) TRAIN de Lille à Crépy-en-Valois (critère : PRIX) ===\n");

        if (chemins.isEmpty()) {
            System.out.println("Aucun itinéraire trouvé.");
            return;
        }

        int rang = 1;
        for (Chemin chemin : chemins) {
            System.out.print("Itinéraire " + rang + " — " + (int) chemin.poids() + " € — ");
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