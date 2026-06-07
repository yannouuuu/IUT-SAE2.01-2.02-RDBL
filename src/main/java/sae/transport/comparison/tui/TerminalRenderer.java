package sae.transport.comparison.tui;

import sae.transport.comparison.models.Plateforme;

import static sae.transport.comparison.tui.Ansi.*;

/**
 * Méthodes statiques de rendu TUI : boîtes, titres, messages d'état et écran d'accueil.
 * Aucun état partagé — toutes les méthodes reçoivent leurs données en paramètre.
 */
public class TerminalRenderer {

    public static final int LARGEUR_BOITE = 60;

    // ── Éléments structurants ────────────────────────────────────

    /** Affiche un bloc encadré avec les lignes fournies (supports codes ANSI). */
    public static void boite(String[] lignes) {
        String sep = "─".repeat(LARGEUR_BOITE);
        System.out.println("  ┌" + sep + "┐");
        for (String ligne : lignes) {
            int longueurVisible = ligne.replaceAll("\u001B\\[[;\\d]*m", "").length();
            int padding = LARGEUR_BOITE - longueurVisible - 1;
            if (padding < 0) {
                padding = 0;
            }
            System.out.println("  │ " + ligne + " ".repeat(padding) + "│");
        }
        System.out.println("  └" + sep + "┘");
    }

    /** Affiche un titre de section sur fond bleu. */
    public static void titre(String texte) {
        System.out.println("  " + BG_BLU + WHT + BLD + " " + texte + " " + RST);
    }

    /** Affiche un message d'erreur avec pastille rouge. */
    public static void erreur(String message) {
        nl();
        System.out.println("  " + BG_RED + WHT + BLD + " ✗ Erreur " + RST
            + " " + RED + message + RST);
    }

    /** Affiche un message de succès avec pastille verte. */
    public static void succes(String message) {
        nl();
        System.out.println("  " + BG_GRN + WHT + BLD + " ✓ OK " + RST
            + " " + GRN_V + message + RST);
    }

    /** Affiche une ligne vide. */
    public static void nl() {
        System.out.println();
    }

    // ── Écran d'accueil ──────────────────────────────────────────

    /**
     * Affiche le bandeau d'accueil avec le nombre de villes et de trajets
     * issus de la plateforme fournie.
     */
    public static void ecranAccueil(Plateforme plateforme) {
        nl();
        String sep = "─".repeat(LARGEUR_BOITE);
        System.out.println("  " + BG_DRK + WHT + " " + sep + " " + RST);
        System.out.println("  " + BG_DRK + CYN_V + BLD
            + centrer("✈  COMPARAISON D'ITINÉRAIRES DE TRANSPORT  ✈", LARGEUR_BOITE + 2)
            + RST);
        System.out.println("  " + BG_DRK + WHT
            + centrer("SAE 2.01 / 2.02  —  IUT Lille", LARGEUR_BOITE + 2)
            + RST);
        System.out.println("  " + BG_DRK + " ".repeat(LARGEUR_BOITE + 2) + RST);
        String info = plateforme.getVilles().size() + " villes  |  "
                    + plateforme.getTrajets().size() + " trajets";
        System.out.println("  " + BG_DRK + DIM + WHT
            + centrer(info, LARGEUR_BOITE + 2)
            + RST);
        System.out.println("  " + BG_DRK + WHT + " " + sep + " " + RST);
    }

    // ── Utilitaire ───────────────────────────────────────────────

    /** Centre {@code texte} dans un champ de {@code largeur} caractères. */
    public static String centrer(String texte, int largeur) {
        int longueur = texte.length();
        if (longueur >= largeur) {
            return texte;
        }
        int padGauche = (largeur - longueur) / 2;
        int padDroit  = largeur - longueur - padGauche;
        return " ".repeat(padGauche) + texte + " ".repeat(padDroit);
    }
}
