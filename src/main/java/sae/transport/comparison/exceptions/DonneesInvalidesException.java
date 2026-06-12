package sae.transport.comparison.exceptions;

/**
 * Erreur qui se produit lorsque le fichier CSV importé n'est pas aux normes. Pour verifier les normes, le bouton "créer fichier CSV"
 * dans la page d'accueil vous donnera tous les détails.
 */

public class DonneesInvalidesException extends Exception {
    public DonneesInvalidesException(String message) {
        super(message);
    }
}
