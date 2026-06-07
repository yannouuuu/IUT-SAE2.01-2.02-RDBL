package sae.transport.comparison.tui;

/**
 * Constantes de codes d'échappement ANSI pour la colorisation du terminal.
 * Toutes les constantes sont {@code public static final} — à utiliser
 * directement ou via un import statique.
 */
public class Ansi {

    // Réinitialisation & styles
    public static final String RST = "\u001B[0m";
    public static final String BLD = "\u001B[1m";
    public static final String DIM = "\u001B[2m";

    // Texte
    public static final String CYN = "\u001B[36m";
    public static final String GRN = "\u001B[32m";
    public static final String YLW = "\u001B[33m";
    public static final String RED = "\u001B[31m";
    public static final String BLU = "\u001B[34m";
    public static final String MAG = "\u001B[35m";
    public static final String WHT = "\u001B[97m";

    // Texte vif
    public static final String GRN_V = "\u001B[92m";
    public static final String YLW_V = "\u001B[93m";
    public static final String CYN_V = "\u001B[96m";

    // Fond
    public static final String BG_RED = "\u001B[41m";
    public static final String BG_GRN = "\u001B[42m";
    public static final String BG_YLW = "\u001B[43m";
    public static final String BG_BLU = "\u001B[44m";
    public static final String BG_MAG = "\u001B[45m";
    public static final String BG_CYN = "\u001B[46m";
    public static final String BG_DRK = "\u001B[100m";
}
