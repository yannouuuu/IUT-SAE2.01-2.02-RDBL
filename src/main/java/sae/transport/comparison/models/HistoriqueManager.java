package sae.transport.comparison.models;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Gère la persistance de l'historique des voyages d'un voyageur
 * via la sérialisation binaire Java.
 */
public class HistoriqueManager {

    private final String cheminFichier;

    /**
     * Construit un gestionnaire d'historique pour un fichier donné.
     *
     * @param cheminFichier chemin vers le fichier de sauvegarde
     */
    public HistoriqueManager(String cheminFichier) {
        this.cheminFichier = cheminFichier;
    }

    /**
     * Sauvegarde la liste des voyages dans le fichier binaire.
     *
     * @param voyages la liste des voyages à sauvegarder
     * @throws IOException si l'écriture échoue
     */
    public void sauvegarder(List<Voyage> voyages) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(cheminFichier))) {
            oos.writeObject(voyages);
        }
    }

    /**
     * Charge la liste des voyages depuis le fichier binaire.
     * Retourne une liste vide si le fichier n'existe pas encore.
     *
     * @return la liste des voyages chargée
     * @throws IOException si la lecture échoue
     */
    @SuppressWarnings("unchecked")
    public List<Voyage> charger() throws IOException {
        File fichier = new File(cheminFichier);
        if (!fichier.exists()) {
            return new ArrayList<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(cheminFichier))) {
            return (List<Voyage>) ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("Format de fichier invalide : " + cheminFichier, e);
        }
    }

    /**
     * Ajoute un voyage à l'historique existant et sauvegarde.
     *
     * @param voyage le voyage à ajouter
     * @throws IOException si la lecture ou l'écriture échoue
     */
    public void ajouterEtSauvegarder(Voyage voyage) throws IOException {
        List<Voyage> voyages = charger();
        voyages.add(voyage);
        sauvegarder(voyages);
    }
}