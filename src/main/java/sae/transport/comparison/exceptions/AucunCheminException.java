package sae.transport.comparison.exceptions;

/**
 * Erreur qui se produit lorsque l'algorithme KPCC ne trouve aucun chemin.
 */

public class AucunCheminException extends Exception {
    public AucunCheminException(String message) {
        super(message);
    }
}
