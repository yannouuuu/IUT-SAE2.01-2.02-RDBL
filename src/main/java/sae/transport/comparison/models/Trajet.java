package sae.transport.comparison.models;

import fr.ulille.but.sae_s2_2026.Lieu;
import fr.ulille.but.sae_s2_2026.Connexion;
import fr.ulille.but.sae_s2_2026.ModaliteTransport;

import java.io.Serializable;

/**
 * Représente une connexion de transport entre deux villes.
 * Implémente l'interface {@link Connexion} fournie par la bibliothèque IUT.
 * Un trajet est caractérisé par un lieu de départ, un lieu d'arrivée,
 * une modalité de transport et un coût selon trois critères.
 */
public class Trajet implements Connexion, Serializable {
    private final Lieu depart;
    private final Lieu arrivee;
    private final Cout cout;
    private final ModaliteTransport modalite;

    /**
     * Construit un trajet entre deux lieux avec une modalité et un coût donnés.
     *
     * @param depart   le lieu de départ
     * @param arrivee  le lieu d'arrivée
     * @param cout     le coût associé à ce trajet (temps, prix, co2)
     * @param modalite la modalité de transport utilisée
     */
    public Trajet(Lieu depart, Lieu arrivee, Cout cout, ModaliteTransport modalite) {
        this.depart = depart;
        this.arrivee = arrivee;
        this.cout = cout;
        this.modalite = modalite;
    }

    /**
     * Retourne le lieu de départ du trajet.
     *
     * @return le lieu de départ
     */
    @Override
    public Lieu getDepart() {
        return this.depart;
    }

    /**
     * Retourne le lieu d'arrivée du trajet.
     *
     * @return le lieu d'arrivée
     */
    @Override
    public Lieu getArrivee() {
        return arrivee;
    }

    /**
     * Retourne le coût associé à ce trajet.
     *
     * @return le coût du trajet
     */
    public Cout getCout() {
        return cout;
    }

    /**
     * Retourne la modalité de transport de ce trajet.
     *
     * @return la modalité de transport
     */
    @Override
    public ModaliteTransport getModalite() {
        return modalite;
    }
}