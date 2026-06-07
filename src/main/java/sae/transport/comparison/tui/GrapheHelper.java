package sae.transport.comparison.tui;

import fr.ulille.but.sae_s2_2026.Chemin;
import fr.ulille.but.sae_s2_2026.Connexion;
import fr.ulille.but.sae_s2_2026.Lieu;
import fr.ulille.but.sae_s2_2026.ModaliteTransport;
import fr.ulille.but.sae_s2_2026.MultiGrapheOrienteValue;
import sae.transport.comparison.models.Trajet;
import sae.transport.comparison.models.TypeCout;
import sae.transport.comparison.models.Voyageur;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Méthodes statiques de construction des graphes et utilitaires réseau.
 * Toutes les méthodes sont purement fonctionnelles (sans état partagé).
 */
public class GrapheHelper {

    // ── Construction des graphes ─────────────────────────────────

    /**
     * Construit un graphe mono-modal à partir d'une liste de trajets.
     * Si {@code profilVoyageur} est non null, le poids de chaque arc est
     * le coût composite pondéré (mode V3) ; sinon c'est la valeur brute du {@code critere}.
     *
     * @param trajets         les trajets à intégrer
     * @param critere         le critère de coût (ignoré si {@code profilVoyageur} != null)
     * @param profilVoyageur  le voyageur dont on utilise les préférences, ou {@code null}
     * @return le graphe orienté valué prêt pour KPCC
     */
    public static MultiGrapheOrienteValue grapheSimple(
            List<Trajet> trajets, TypeCout critere, Voyageur profilVoyageur) {

        MultiGrapheOrienteValue graphe = new MultiGrapheOrienteValue();
        List<String> nomsAjoutes = new ArrayList<>();

        for (Trajet trajet : trajets) {
            String nomDepart  = trajet.getDepart().toString();
            String nomArrivee = trajet.getArrivee().toString();

            if (!nomsAjoutes.contains(nomDepart)) {
                graphe.ajouterSommet(trajet.getDepart());
                nomsAjoutes.add(nomDepart);
            }
            if (!nomsAjoutes.contains(nomArrivee)) {
                graphe.ajouterSommet(trajet.getArrivee());
                nomsAjoutes.add(nomArrivee);
            }

            double poids;
            if (profilVoyageur != null) {
                poids = profilVoyageur.calculerCoutComposite(trajet.getCout());
            } else {
                poids = trajet.getCout().getValeur(critere);
            }
            graphe.ajouterArete(trajet, poids);
        }
        return graphe;
    }

    /**
     * Construit un graphe multi-modal avec sommets dupliqués {@code VilleNom_MODE}.
     * Remplit {@code sommets} (clé → sommet) et ajoute des arêtes de
     * correspondance à coût 0 entre modes d'une même ville.
     *
     * @param trajets  les trajets du réseau
     * @param sommets  map à remplir : clé {@code Ville_MODE} → sommet
     * @param critere  le critère de coût utilisé comme poids des arcs de transport
     * @return le graphe orienté valué prêt pour KPCC
     */
    public static MultiGrapheOrienteValue grapheMultiModal(
            List<Trajet> trajets, Map<String, Lieu> sommets, TypeCout critere) {

        MultiGrapheOrienteValue graphe = new MultiGrapheOrienteValue();

        // Sommets dupliqués : VilleNom_MODE
        for (Trajet trajet : trajets) {
            String cleDep = trajet.getDepart().toString()  + "_" + trajet.getModalite().name();
            String cleArr = trajet.getArrivee().toString() + "_" + trajet.getModalite().name();

            if (!sommets.containsKey(cleDep)) {
                Lieu lieu = creerLieu(cleDep);
                sommets.put(cleDep, lieu);
                graphe.ajouterSommet(lieu);
            }
            if (!sommets.containsKey(cleArr)) {
                Lieu lieu = creerLieu(cleArr);
                sommets.put(cleArr, lieu);
                graphe.ajouterSommet(lieu);
            }
        }

        // Arêtes de transport
        for (Trajet trajet : trajets) {
            String cleDep = trajet.getDepart().toString()  + "_" + trajet.getModalite().name();
            String cleArr = trajet.getArrivee().toString() + "_" + trajet.getModalite().name();
            Connexion arc = creerConnexion(sommets.get(cleDep), sommets.get(cleArr), trajet.getModalite());
            graphe.ajouterArete(arc, trajet.getCout().getValeur(critere));
        }

        // Arêtes de correspondance — regrouper les clés par ville
        Map<String, List<String>> cleParVille = new HashMap<>();
        for (String cle : sommets.keySet()) {
            String nomVille = cle.substring(0, cle.lastIndexOf("_"));
            if (!cleParVille.containsKey(nomVille)) {
                cleParVille.put(nomVille, new ArrayList<>());
            }
            cleParVille.get(nomVille).add(cle);
        }

        for (List<String> clesVille : cleParVille.values()) {
            for (int i = 0; i < clesVille.size(); i++) {
                for (int j = 0; j < clesVille.size(); j++) {
                    if (i != j) {
                        Lieu depart  = sommets.get(clesVille.get(i));
                        Lieu arrivee = sommets.get(clesVille.get(j));
                        String modeStr = clesVille.get(i).substring(clesVille.get(i).lastIndexOf("_") + 1);
                        ModaliteTransport mode = ModaliteTransport.valueOf(modeStr);
                        graphe.ajouterArete(creerConnexion(depart, arrivee, mode), 0.0);
                    }
                }
            }
        }

        return graphe;
    }

    // ── Utilitaires réseau ───────────────────────────────────────

    /**
     * Retourne {@code true} si {@code nomVille} apparaît comme départ
     * ou arrivée dans au moins un trajet de la liste.
     */
    public static boolean villePresente(List<Trajet> trajets, String nomVille) {
        boolean trouvee = false;
        for (int i = 0; i < trajets.size() && !trouvee; i++) {
            Trajet trajet = trajets.get(i);
            if (trajet.getDepart().toString().equals(nomVille)
                    || trajet.getArrivee().toString().equals(nomVille)) {
                trouvee = true;
            }
        }
        return trouvee;
    }

    /**
     * Retourne toutes les clés de {@code sommets} dont la partie ville
     * (avant le dernier {@code _}) correspond à {@code nomVille}.
     */
    public static List<String> clesPourVille(Map<String, Lieu> sommets, String nomVille) {
        List<String> cles = new ArrayList<>();
        for (String cle : sommets.keySet()) {
            String ville = cle.substring(0, cle.lastIndexOf("_"));
            if (ville.equals(nomVille)) {
                cles.add(cle);
            }
        }
        return cles;
    }

    /**
     * Tri par sélection croissant sur les poids des chemins.
     * N'utilise pas {@code Collections.sort()} — exigence SAE.
     */
    public static void trierParPoids(List<Chemin> chemins) {
        for (int i = 0; i < chemins.size() - 1; i++) {
            int indexMin = i;
            for (int j = i + 1; j < chemins.size(); j++) {
                if (chemins.get(j).poids() < chemins.get(indexMin).poids()) {
                    indexMin = j;
                }
            }
            Chemin tmp = chemins.get(i);
            chemins.set(i, chemins.get(indexMin));
            chemins.set(indexMin, tmp);
        }
    }

    // ── Factories Lieu / Connexion (classes anonymes) ────────────

    /** Crée un {@link Lieu} dont {@code toString()} retourne {@code nom}. */
    public static Lieu creerLieu(String nom) {
        return new Lieu() {
            @Override
            public String toString() {
                return nom;
            }
        };
    }

    /** Crée une {@link Connexion} anonyme reliant {@code depart} à {@code arrivee} par {@code mode}. */
    public static Connexion creerConnexion(Lieu depart, Lieu arrivee, ModaliteTransport mode) {
        return new Connexion() {
            @Override public Lieu getDepart()                { return depart; }
            @Override public Lieu getArrivee()               { return arrivee; }
            @Override public ModaliteTransport getModalite() { return mode; }
        };
    }
}
