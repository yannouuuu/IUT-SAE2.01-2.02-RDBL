package sae.transport.comparison;

import fr.ulille.but.sae_s2_2026.AlgorithmeKPCC;
import fr.ulille.but.sae_s2_2026.Chemin;
import fr.ulille.but.sae_s2_2026.Connexion;
import fr.ulille.but.sae_s2_2026.Lieu;
import fr.ulille.but.sae_s2_2026.ModaliteTransport;
import fr.ulille.but.sae_s2_2026.MultiGrapheOrienteValue;
import sae.transport.comparison.exceptions.DonneesInvalidesException;
import sae.transport.comparison.models.HistoriqueManager;
import sae.transport.comparison.models.Plateforme;
import sae.transport.comparison.models.Trajet;
import sae.transport.comparison.models.TypeCout;
import sae.transport.comparison.models.Ville;
import sae.transport.comparison.models.Voyage;
import sae.transport.comparison.models.Voyageur;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static sae.transport.comparison.tui.Ansi.*;
import static sae.transport.comparison.tui.GrapheHelper.*;
import static sae.transport.comparison.tui.ResultatRenderer.*;
import static sae.transport.comparison.tui.TerminalRenderer.*;

/**
 * Mode terminal interactif — TUI avec menus numérotés, KPCC mono/multi-modal
 * et multi-critères, historique persistant. Lancer via {@code mvn clean compile exec:java}.
 *
 * <p>Ce fichier orchestre la navigation et la saisie. Le rendu visuel est délégué à
 * {@link TerminalRenderer}, la construction des graphes à {@link GrapheHelper},
 * l'affichage des résultats à {@link ResultatRenderer}, et les couleurs à {@link Ansi}.
 */
public class AppTerminal {

    // Réseau de démonstration
    private static final String[] RESEAU = {
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
        "Crépy-en-Valois;Paris;BUS;8;1.2;75",
        "Lille;Paris;AVION;120;85.0;55",
        "Dunkerque;Paris;AVION;110;80.0;50"
    };

    // État partagé de l'application
    private static Plateforme       plateforme;
    private static Voyageur         voyageur;
    private static HistoriqueManager historiqueManager;
    private static Scanner          sc;

    // ── Main ────────────────────────────────────────────────────

    /** Point d'entrée du mode terminal. */
    public static void main(String[] args) {
        sc = new Scanner(System.in);
        plateforme = new Plateforme();
        voyageur = new Voyageur("Utilisateur", TypeCout.PRIX);
        historiqueManager = new HistoriqueManager("historique.ser");

        try {
            List<Voyage> sauvegardes = historiqueManager.charger();
            for (Voyage voyage : sauvegardes) {
                voyageur.ajouterVoyage(voyage);
            }
        } catch (IOException e) {
            System.err.println("Historique inaccessible, départ à zéro : " + e.getMessage());
        }

        try {
            plateforme.chargerDepuisTableau(RESEAU);
        } catch (DonneesInvalidesException e) {
            System.err.println("Erreur de chargement du réseau : " + e.getMessage());
            sc.close();
            return;
        }

        ecranAccueil(plateforme);

        boolean actif = true;
        while (actif) {
            afficherMenuPrincipal();
            int choix = lireEntier(0, 6);

            if (choix == 0) {
                actif = false;
            } else if (choix == 1) {
                rechercheMonoModale();
            } else if (choix == 2) {
                rechercheMultiModale();
            } else if (choix == 3) {
                gererProfil();
            } else if (choix == 4) {
                rechercheAvecProfil();
            } else if (choix == 5) {
                afficherHistorique();
            } else if (choix == 6) {
                importerCSV();
            }
        }

        nl();
        System.out.println("  " + CYN + "Au revoir !" + RST);
        nl();
        sc.close();
    }

    // ── Menu principal ───────────────────────────────────────────

    private static void afficherMenuPrincipal() {
        nl();
        int nbVoyages = voyageur.getHistorique().size();
        String texteHistorique = (nbVoyages == 0)
                ? "Historique"
                : "Historique  (" + nbVoyages + " voyage(s))";

        String critereActuel;
        if (voyageur.getTypeCout() != null) {
            critereActuel = voyageur.getTypeCout().name();
        } else {
            critereActuel = "multi-critères";
        }

        int nbTrajets = plateforme.getTrajets().size();
        String texteReseau = (nbTrajets == 19)
            ? "Importer un CSV"
            : "Importer un CSV  " + DIM + "(" + nbTrajets + " trajets chargés)" + RST;

        boite(new String[]{
            BG_BLU + WHT + BLD + "  MENU PRINCIPAL  " + RST,
            "",
            CYN   + "  [1]" + RST + "  Recherche mono-modale",
            CYN   + "  [2]" + RST + "  Recherche multi-modale",
            MAG   + "  [3]" + RST + "  Mon profil  " + DIM + "(critère : " + critereActuel + ")" + RST,
            MAG   + "  [4]" + RST + "  Recherche avec mon profil",
            YLW   + "  [5]" + RST + "  " + texteHistorique,
            GRN_V + "  [6]" + RST + "  " + texteReseau,
            RED   + "  [0]" + RST + "  Quitter",
        });
        System.out.print(CYN_V + "  > " + RST);
    }

    // ── Import CSV ───────────────────────────────────────────────

    /**
     * Importe des trajets depuis un fichier CSV dont chaque ligne suit le format :
     * {@code villeDépart;villeArrivée;modalité;prix;co2;durée}
     * Le réseau existant est conservé : les nouvelles lignes s'ajoutent.
     */
    private static void importerCSV() {
        nl();
        titre("IMPORTER UN FICHIER CSV");
        nl();

        String repCourant = System.getProperty("user.dir");

        System.out.println("  " + DIM + "Format attendu par ligne :" + RST);
        System.out.println("  " + DIM + "  villeDépart;villeArrivée;MODALITE;prix;co2;durée" + RST);
        nl();
        System.out.println("  " + DIM + "Répertoire courant (chemins relatifs partent d'ici) :" + RST);
        System.out.println("  " + CYN + "  " + repCourant + RST);
        nl();
        System.out.println("  " + DIM + "Exemples de chemins :" + RST);
        System.out.println("  " + DIM + "  Relatif  →  data/reseau.csv" + RST);
        System.out.println("  " + DIM + "  Absolu   →  /Users/prenom/Documents/reseau.csv" + RST);
        nl();
        System.out.print("  Chemin du fichier CSV : ");
        String chemin = sc.nextLine().trim();

        if (chemin.isEmpty()) {
            erreur("Chemin vide — import annulé.");
            return;
        }

        int avant = plateforme.getTrajets().size();
        try {
            plateforme.chargerDepuisCSV(chemin);
            int apres   = plateforme.getTrajets().size();
            int ajoutes = apres - avant;
            succes(ajoutes + " trajet(s) importé(s) depuis " + chemin
                + "  (" + apres + " au total)");
        } catch (DonneesInvalidesException e) {
            erreur(e.getMessage());
        }
    }

    // ── Recherche mono-modale (V1) ───────────────────────────────

    /** Recherche V1 : un mode, un critère, k plus courts chemins. */
    private static void rechercheMonoModale() {
        nl();
        titre("RECHERCHE MONO-MODALE");

        ModaliteTransport modalite = choisirModalite();

        if (modalite != null) {
            String villeDepart  = choisirVille("Ville de départ");
            String villeArrivee = choisirVille("Ville d'arrivée");

            if (villeDepart != null && villeArrivee != null && !villeDepart.equals(villeArrivee)) {
                TypeCout critere = choisirCritere();

                if (critere != null) {
                    int k = choisirK();
                    List<Trajet> trajets = plateforme.filtrerParModalite(modalite);
                    boolean departOk  = villePresente(trajets, villeDepart);
                    boolean arriveeOk = villePresente(trajets, villeArrivee);

                    if (trajets.isEmpty()) {
                        erreur("Aucun trajet disponible en mode " + modalite + ".");
                    } else if (!departOk) {
                        erreur("Aucun trajet " + modalite + " au départ de " + villeDepart + ".");
                    } else if (!arriveeOk) {
                        erreur("Aucun trajet " + modalite + " à l'arrivée de " + villeArrivee + ".");
                    } else {
                        Lieu sommetDepart  = plateforme.getVille(villeDepart);
                        Lieu sommetArrivee = plateforme.getVille(villeArrivee);
                        MultiGrapheOrienteValue graphe = grapheSimple(trajets, critere, null);
                        List<Chemin> chemins = AlgorithmeKPCC.kpcc(graphe, sommetDepart, sommetArrivee, k);
                        nl();
                        afficherResultatsSimples(chemins, villeDepart, villeArrivee, modalite.name(), critere);
                        proposerSauvegarde(chemins);
                    }
                }
            } else if (villeDepart != null && villeArrivee != null && villeDepart.equals(villeArrivee)) {
                erreur("Le départ et l'arrivée sont identiques.");
            }
        }
    }

    // ── Recherche multi-modale (V2) ──────────────────────────────

    /** Recherche V2 : sommets dupliqués par mode, correspondances à coût nul. */
    private static void rechercheMultiModale() {
        nl();
        titre("RECHERCHE MULTI-MODALE");

        String villeDepart  = choisirVille("Ville de départ");
        String villeArrivee = choisirVille("Ville d'arrivée");

        if (villeDepart != null && villeArrivee != null && !villeDepart.equals(villeArrivee)) {
            TypeCout critere = choisirCritere();

            if (critere != null) {
                int k = choisirK();
                Map<String, Lieu> sommets = new HashMap<>();
                MultiGrapheOrienteValue graphe = grapheMultiModal(plateforme.getTrajets(), sommets, critere);

                List<String> clesDepartPossibles  = clesPourVille(sommets, villeDepart);
                List<String> clesArriveePossibles = clesPourVille(sommets, villeArrivee);

                if (clesDepartPossibles.isEmpty() || clesArriveePossibles.isEmpty()) {
                    erreur("Ville introuvable ou aucun trajet disponible.");
                } else {
                    List<Chemin> tousChemins = new ArrayList<>();
                    for (int i = 0; i < clesDepartPossibles.size(); i++) {
                        for (int j = 0; j < clesArriveePossibles.size(); j++) {
                            Lieu depart  = sommets.get(clesDepartPossibles.get(i));
                            Lieu arrivee = sommets.get(clesArriveePossibles.get(j));
                            List<Chemin> resultat = AlgorithmeKPCC.kpcc(graphe, depart, arrivee, k);
                            tousChemins.addAll(resultat);
                        }
                    }
                    trierParPoids(tousChemins);
                    List<Chemin> topK = new ArrayList<>();
                    for (int i = 0; i < tousChemins.size() && i < k; i++) {
                        topK.add(tousChemins.get(i));
                    }
                    nl();
                    afficherResultatsMultiModal(topK, villeDepart, villeArrivee, critere);
                    proposerSauvegarde(topK);
                }
            }
        } else if (villeDepart != null && villeArrivee != null && villeDepart.equals(villeArrivee)) {
            erreur("Le départ et l'arrivée sont identiques.");
        }
    }

    // ── Gestion du profil (V3) ───────────────────────────────────

    private static void gererProfil() {
        nl();
        titre("MON PROFIL VOYAGEUR");

        String critereActuel;
        if (voyageur.getTypeCout() != null) {
            critereActuel = voyageur.getTypeCout().name();
        } else {
            Map<TypeCout, Double> preferences = voyageur.getPreferences();
            critereActuel = String.format("PRIX=%.0f%%  TEMPS=%.0f%%  CO2=%.0f%%",
                preferences.get(TypeCout.PRIX)  * 100,
                preferences.get(TypeCout.TEMPS) * 100,
                preferences.get(TypeCout.CO2)   * 100);
        }

        boite(new String[]{
            "  Critère actuel : " + BLD + critereActuel + RST,
            "",
            "  [1]  Minimiser le PRIX",
            "  [2]  Minimiser le TEMPS",
            "  [3]  Minimiser le CO2",
            "  [4]  Préférences pondérées (multi-critères)",
            "  [0]  Retour",
        });
        System.out.print(CYN + "  > " + RST);
        int choix = lireEntier(0, 4);

        if (choix == 1) {
            voyageur.setTypeCout(TypeCout.PRIX);
            succes("Critère mis à jour : PRIX");
        } else if (choix == 2) {
            voyageur.setTypeCout(TypeCout.TEMPS);
            succes("Critère mis à jour : TEMPS");
        } else if (choix == 3) {
            voyageur.setTypeCout(TypeCout.CO2);
            succes("Critère mis à jour : CO2");
        } else if (choix == 4) {
            saisirPreferencesMulti();
        }
    }

    private static void saisirPreferencesMulti() {
        nl();
        System.out.println("  " + DIM + "Poids entier >= 0 par critère — normalisés automatiquement." + RST);
        nl();

        System.out.print("  Poids PRIX  : ");
        int poidsPrix  = lireEntierPositif();
        System.out.print("  Poids TEMPS : ");
        int poidsTemps = lireEntierPositif();
        System.out.print("  Poids CO2   : ");
        int poidsCo2   = lireEntierPositif();

        if (poidsPrix == 0 && poidsTemps == 0 && poidsCo2 == 0) {
            erreur("Au moins un poids doit être non nul.");
        } else {
            Map<TypeCout, Double> preferences = new EnumMap<>(TypeCout.class);
            preferences.put(TypeCout.PRIX,  (double) poidsPrix);
            preferences.put(TypeCout.TEMPS, (double) poidsTemps);
            preferences.put(TypeCout.CO2,   (double) poidsCo2);
            voyageur.setPreferences(preferences);
            voyageur.setTypeCout(null);

            Map<TypeCout, Double> normalises = voyageur.getPreferences();
            succes(String.format(
                "Préférences : PRIX=%.0f%%  TEMPS=%.0f%%  CO2=%.0f%%",
                normalises.get(TypeCout.PRIX)  * 100,
                normalises.get(TypeCout.TEMPS) * 100,
                normalises.get(TypeCout.CO2)   * 100));
        }
    }

    // ── Recherche avec profil voyageur (V3) ──────────────────────

    /** Recherche V3 : coût composite pondéré comme poids d'arc. */
    private static void rechercheAvecProfil() {
        nl();
        titre("RECHERCHE AVEC MON PROFIL");

        Map<TypeCout, Double> preferences = voyageur.getPreferences();
        System.out.println("  " + DIM + String.format(
            "Profil : PRIX=%.0f%%  TEMPS=%.0f%%  CO2=%.0f%%",
            preferences.get(TypeCout.PRIX)  * 100,
            preferences.get(TypeCout.TEMPS) * 100,
            preferences.get(TypeCout.CO2)   * 100) + RST);

        ModaliteTransport modalite = choisirModalite();

        if (modalite != null) {
            String villeDepart  = choisirVille("Ville de départ");
            String villeArrivee = choisirVille("Ville d'arrivée");

            if (villeDepart != null && villeArrivee != null && !villeDepart.equals(villeArrivee)) {
                int k = choisirK();
                List<Trajet> trajets = plateforme.filtrerParModalite(modalite);
                boolean departOk  = villePresente(trajets, villeDepart);
                boolean arriveeOk = villePresente(trajets, villeArrivee);

                if (trajets.isEmpty()) {
                    erreur("Aucun trajet disponible en mode " + modalite + ".");
                } else if (!departOk) {
                    erreur("Aucun trajet " + modalite + " au départ de " + villeDepart + ".");
                } else if (!arriveeOk) {
                    erreur("Aucun trajet " + modalite + " à l'arrivée de " + villeArrivee + ".");
                } else {
                    Lieu sommetDepart  = plateforme.getVille(villeDepart);
                    Lieu sommetArrivee = plateforme.getVille(villeArrivee);
                    MultiGrapheOrienteValue graphe = grapheSimple(trajets, null, voyageur);
                    List<Chemin> chemins = AlgorithmeKPCC.kpcc(graphe, sommetDepart, sommetArrivee, k);
                    nl();
                    afficherResultatsProfil(chemins, villeDepart, villeArrivee, modalite.name());
                    proposerSauvegarde(chemins);
                }
            } else if (villeDepart != null && villeArrivee != null && villeDepart.equals(villeArrivee)) {
                erreur("Le départ et l'arrivée sont identiques.");
            }
        }
    }

    // ── Historique ───────────────────────────────────────────────

    private static void afficherHistorique() {
        nl();
        titre("HISTORIQUE DES VOYAGES");

        List<Voyage> historique = voyageur.getHistorique();

        if (historique.isEmpty()) {
            boite(new String[]{ "  Aucun voyage enregistré." });
        } else {
            double totalPrix  = voyageur.getTotalHistorique(TypeCout.PRIX);
            double totalTemps = voyageur.getTotalHistorique(TypeCout.TEMPS);
            double totalCo2   = voyageur.getTotalHistorique(TypeCout.CO2);

            String[] lignes = new String[historique.size() + 4];
            lignes[0] = BLD + YLW_V + "  " + historique.size() + " voyage(s) enregistré(s)" + RST;
            lignes[1] = "";

            for (int i = 0; i < historique.size(); i++) {
                Voyage voyage = historique.get(i);
                String couleurLigne = (i % 2 == 0) ? CYN : WHT;
                lignes[i + 2] = couleurLigne + String.format(
                    "  %2d.  %-30s  %5.0f€  %4.0fmin  %4.1fkg",
                    i + 1,
                    voyage.getVilleDepart() + " -> " + voyage.getVilleArrivee(),
                    voyage.getCoutTotal(TypeCout.PRIX),
                    voyage.getCoutTotal(TypeCout.TEMPS),
                    voyage.getCoutTotal(TypeCout.CO2)) + RST;
            }

            lignes[historique.size() + 2] = "";
            lignes[historique.size() + 3] = BG_DRK + WHT + String.format(
                "  Total : %.0f€  |  %.0fmin  |  %.1fkg CO2  ",
                totalPrix, totalTemps, totalCo2) + RST;

            boite(lignes);

            nl();
            System.out.print("  Vider l'historique ? [o/N] ");
            String reponse = sc.nextLine().trim().toLowerCase();
            if (reponse.equals("o")) {
                viderHistorique();
            }
        }
    }

    private static void viderHistorique() {
        try {
            historiqueManager.sauvegarder(new ArrayList<>());
            String nom = voyageur.getNom();
            TypeCout ancienCritere = voyageur.getTypeCout();
            if (ancienCritere != null) {
                voyageur = new Voyageur(nom, ancienCritere);
            } else {
                voyageur = new Voyageur(nom, voyageur.getPreferences());
            }
            succes("Historique effacé.");
        } catch (IOException e) {
            erreur("Impossible de vider l'historique : " + e.getMessage());
        }
    }

    // ── Sauvegarde dans l'historique ─────────────────────────────

    private static void proposerSauvegarde(List<Chemin> chemins) {
        if (!chemins.isEmpty()) {
            nl();
            System.out.print("  Sauvegarder le meilleur itinéraire ? [o/N] ");
            String reponse = sc.nextLine().trim().toLowerCase();

            if (reponse.equals("o")) {
                List<Trajet> trajets = new ArrayList<>();
                for (Connexion arc : chemins.get(0).aretes()) {
                    if (arc instanceof Trajet) {
                        trajets.add((Trajet) arc);
                    }
                }
                if (trajets.isEmpty()) {
                    succes("Itinéraire affiché — non sérialisable (arêtes anonymes en multi-modal).");
                } else {
                    Voyage voyage = new Voyage(trajets);
                    voyageur.ajouterVoyage(voyage);
                    try {
                        historiqueManager.ajouterEtSauvegarder(voyage);
                        succes("Voyage sauvegardé.");
                    } catch (IOException e) {
                        erreur("Impossible de sauvegarder : " + e.getMessage());
                    }
                }
            }
        }
    }

    // ── Saisie utilisateur ───────────────────────────────────────

    private static ModaliteTransport choisirModalite() {
        nl();
        boite(new String[]{
            BLD + "  Mode de transport :" + RST,
            "",
            GRN + "  [1]" + RST + "  🚆 TRAIN",
            YLW + "  [2]" + RST + "  🚌 BUS",
            BLU + "  [3]" + RST + "  ✈️ AVION",
            RED + "  [0]" + RST + "  Annuler",
        });
        System.out.print(CYN_V + "  > " + RST);
        int choix = lireEntier(0, 3);

        ModaliteTransport resultat = null;
        if (choix == 1) {
            resultat = ModaliteTransport.TRAIN;
        } else if (choix == 2) {
            resultat = ModaliteTransport.BUS;
        } else if (choix == 3) {
            resultat = ModaliteTransport.AVION;
        }
        return resultat;
    }

    private static String choisirVille(String label) {
        List<Ville> villes = plateforme.getVilles();
        String[] options = new String[villes.size() + 2];
        options[0] = "  " + label + " :";
        options[1] = "";
        for (int i = 0; i < villes.size(); i++) {
            options[i + 2] = "  [" + (i + 1) + "]  " + villes.get(i).getNom();
        }
        nl();
        boite(options);
        System.out.print(CYN + "  > " + RST);
        int choix = lireEntier(0, villes.size());

        String villeChoisie = null;
        if (choix != 0) {
            villeChoisie = villes.get(choix - 1).getNom();
        }
        return villeChoisie;
    }

    private static TypeCout choisirCritere() {
        nl();
        boite(new String[]{
            BLD + "  Critère d'optimisation :" + RST,
            "",
            GRN + "  [1]" + RST + "  💶 PRIX   (euros)",
            CYN + "  [2]" + RST + "  ⏱️ TEMPS  (minutes)",
            YLW + "  [3]" + RST + "  🌿 CO2    (kg CO2e)",
            RED + "  [0]" + RST + "  Annuler",
        });
        System.out.print(CYN_V + "  > " + RST);
        int choix = lireEntier(0, 3);

        TypeCout critereChoisi = null;
        if (choix == 1) {
            critereChoisi = TypeCout.PRIX;
        } else if (choix == 2) {
            critereChoisi = TypeCout.TEMPS;
        } else if (choix == 3) {
            critereChoisi = TypeCout.CO2;
        }
        return critereChoisi;
    }

    private static int choisirK() {
        nl();
        System.out.print("  Nombre d'itinéraires à afficher [1-9] : ");
        return lireEntier(1, 9);
    }

    private static int lireEntier(int min, int max) {
        boolean valide = false;
        int valeur = min;
        while (!valide) {
            String saisie = sc.nextLine().trim();
            try {
                int entier = Integer.parseInt(saisie);
                if (entier >= min && entier <= max) {
                    valeur = entier;
                    valide = true;
                } else {
                    System.out.print("  Entrez un nombre entre " + min + " et " + max + " : ");
                }
            } catch (NumberFormatException e) {
                System.out.print("  Entier attendu : ");
            }
        }
        return valeur;
    }

    private static int lireEntierPositif() {
        boolean valide = false;
        int valeur = 0;
        while (!valide) {
            String saisie = sc.nextLine().trim();
            try {
                int entier = Integer.parseInt(saisie);
                if (entier >= 0) {
                    valeur = entier;
                    valide = true;
                } else {
                    System.out.print("  Valeur >= 0 attendue : ");
                }
            } catch (NumberFormatException e) {
                System.out.print("  Entier attendu : ");
            }
        }
        return valeur;
    }
}
