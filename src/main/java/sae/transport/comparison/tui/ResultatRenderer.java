package sae.transport.comparison.tui;

import fr.ulille.but.sae_s2_2026.Chemin;
import fr.ulille.but.sae_s2_2026.Connexion;
import sae.transport.comparison.models.TypeCout;

import java.util.ArrayList;
import java.util.List;

import static sae.transport.comparison.tui.Ansi.*;
import static sae.transport.comparison.tui.TerminalRenderer.boite;

/**
 * Méthodes statiques d'affichage et de formatage des résultats de recherche.
 * S'appuie sur {@link TerminalRenderer} pour le rendu des boîtes.
 */
public class ResultatRenderer {

    // ── Affichage des résultats ──────────────────────────────────

    /**
     * Affiche les résultats d'une recherche mono-modale.
     *
     * @param chemins  les chemins trouvés par KPCC
     * @param dep      nom de la ville de départ
     * @param arr      nom de la ville d'arrivée
     * @param mode     nom de la modalité de transport
     * @param critere  critère de coût utilisé
     */
    public static void afficherResultatsSimples(
            List<Chemin> chemins, String dep, String arr, String mode, TypeCout critere) {

        if (chemins.isEmpty()) {
            boite(new String[]{ "  Aucun itinéraire trouvé.", "  " + dep + " -> " + arr });
        } else {
            String[] lignes = new String[chemins.size() + 3];
            lignes[0] = BLD + CYN_V + "  " + dep + "  ->  " + arr + RST;
            lignes[1] = DIM + "  Mode : " + couleurModalite(mode) + mode + RST
                + DIM + "   |   Critère : " + couleurCritere(critere) + critere + RST;
            lignes[2] = "";
            for (int i = 0; i < chemins.size(); i++) {
                Chemin chemin = chemins.get(i);
                String couleurRang = (i == 0) ? BG_GRN + WHT + BLD : YLW_V;
                lignes[i + 3] = String.format("  %d.  %s%s%s   %s",
                    i + 1, couleurRang, formatPoids(chemin.poids(), critere), RST,
                    formatCheminSimple(chemin));
            }
            boite(lignes);
        }
    }

    /**
     * Affiche les résultats d'une recherche multi-modale.
     *
     * @param chemins  les chemins trouvés
     * @param dep      nom de la ville de départ
     * @param arr      nom de la ville d'arrivée
     * @param critere  critère de coût utilisé
     */
    public static void afficherResultatsMultiModal(
            List<Chemin> chemins, String dep, String arr, TypeCout critere) {

        if (chemins.isEmpty()) {
            boite(new String[]{ "  Aucun itinéraire multi-modal trouvé.", "  " + dep + " -> " + arr });
        } else {
            String[] lignes = new String[chemins.size() + 3];
            lignes[0] = BLD + CYN_V + "  " + dep + "  ->  " + arr + RST;
            lignes[1] = DIM + "  Multi-modal   |   Critère : " + couleurCritere(critere) + critere + RST;
            lignes[2] = "";
            for (int i = 0; i < chemins.size(); i++) {
                Chemin chemin = chemins.get(i);
                String couleurRang = (i == 0) ? BG_GRN + WHT + BLD : YLW_V;
                lignes[i + 3] = String.format("  %d.  %s%s%s   %s",
                    i + 1, couleurRang, formatPoids(chemin.poids(), critere), RST,
                    formatCheminMultiModal(chemin));
            }
            boite(lignes);
        }
    }

    /**
     * Affiche les résultats d'une recherche avec profil voyageur (coût composite).
     *
     * @param chemins  les chemins trouvés
     * @param dep      nom de la ville de départ
     * @param arr      nom de la ville d'arrivée
     * @param mode     nom de la modalité de transport
     */
    public static void afficherResultatsProfil(
            List<Chemin> chemins, String dep, String arr, String mode) {

        if (chemins.isEmpty()) {
            boite(new String[]{ "  Aucun itinéraire trouvé.", "  " + dep + " -> " + arr });
        } else {
            String[] lignes = new String[chemins.size() + 3];
            lignes[0] = BLD + CYN_V + "  " + dep + "  ->  " + arr + RST;
            lignes[1] = DIM + "  Mode : " + couleurModalite(mode) + mode + RST
                + DIM + "   |   Critère : " + MAG + "composite (profil)" + RST;
            lignes[2] = "";
            for (int i = 0; i < chemins.size(); i++) {
                Chemin chemin = chemins.get(i);
                String couleurRang = (i == 0) ? BG_GRN + WHT + BLD : YLW_V;
                lignes[i + 3] = String.format("  %d.  %sscore=%.2f%s   %s",
                    i + 1, couleurRang, chemin.poids(), RST, formatCheminSimple(chemin));
            }
            boite(lignes);
        }
    }

    // ── Formatage des chemins ────────────────────────────────────

    /**
     * Formate un chemin mono-modal en chaîne {@code A -> B -> C}.
     *
     * @param chemin le chemin à formater
     * @return la représentation textuelle du chemin
     */
    public static String formatCheminSimple(Chemin chemin) {
        List<Connexion> aretes = chemin.aretes();
        if (aretes.isEmpty()) {
            return "(vide)";
        }
        List<String> etapes = new ArrayList<>();
        etapes.add(aretes.get(0).getDepart().toString());
        for (Connexion arc : aretes) {
            etapes.add(arc.getArrivee().toString());
        }
        return String.join(" -> ", etapes);
    }

    /**
     * Formate un chemin multi-modal en affichant les modes de transport
     * et les correspondances.
     *
     * @param chemin le chemin multi-modal à formater
     * @return la représentation textuelle avec modalités
     */
    public static String formatCheminMultiModal(Chemin chemin) {
        List<Connexion> aretes = chemin.aretes();
        if (aretes.isEmpty()) {
            return "(vide)";
        }
        String texte = "";
        boolean premierTroncon = true;
        for (Connexion arc : aretes) {
            String nomDepart  = arc.getDepart().toString();
            String nomArrivee = arc.getArrivee().toString();
            String villeDepart  = nomDepart.substring(0,  nomDepart.lastIndexOf("_"));
            String villeArrivee = nomArrivee.substring(0, nomArrivee.lastIndexOf("_"));
            String modeDepart   = nomDepart.substring(nomDepart.lastIndexOf("_") + 1);
            String modeArrivee  = nomArrivee.substring(nomArrivee.lastIndexOf("_") + 1);

            if (villeDepart.equals(villeArrivee)) {
                texte = texte + " [" + modeDepart + "->" + modeArrivee + "]";
            } else {
                if (premierTroncon) {
                    texte = villeDepart + "(" + modeDepart + ")";
                    premierTroncon = false;
                }
                texte = texte + " -> " + villeArrivee + "(" + modeArrivee + ")";
            }
        }
        return texte;
    }

    // ── Formatage des coûts ──────────────────────────────────────

    /**
     * Formate un poids numérique avec l'unité appropriée au critère.
     *
     * @param valeur  le poids à formater
     * @param critere le critère qui détermine l'unité
     * @return la chaîne formatée (ex. {@code "55 €"}, {@code "90 min"}, {@code "2.4 kg"})
     */
    public static String formatPoids(double valeur, TypeCout critere) {
        if (critere == TypeCout.PRIX) {
            return String.format("%.0f €", valeur);
        } else if (critere == TypeCout.TEMPS) {
            return String.format("%.0f min", valeur);
        } else {
            return String.format("%.1f kg", valeur);
        }
    }

    // ── Couleurs sémantiques ─────────────────────────────────────

    /** Renvoie le code ANSI associé à une modalité de transport. */
    public static String couleurModalite(String mode) {
        if ("TRAIN".equals(mode))  return GRN;
        if ("BUS".equals(mode))    return YLW;
        if ("AVION".equals(mode))  return BLU;
        return RST;
    }

    /** Renvoie le code ANSI associé à un critère de coût. */
    public static String couleurCritere(TypeCout critere) {
        if (critere == TypeCout.PRIX)  return GRN;
        if (critere == TypeCout.TEMPS) return CYN;
        if (critere == TypeCout.CO2)   return YLW;
        return RST;
    }
}
