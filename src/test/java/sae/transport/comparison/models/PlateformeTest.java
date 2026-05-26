package sae.transport.comparison.models;

import fr.ulille.but.sae_s2_2026.ModaliteTransport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sae.transport.comparison.exceptions.DonneesInvalidesException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlateformeTest {

    private Plateforme plateforme;

    @BeforeEach
    void setUp() throws DonneesInvalidesException {
        plateforme = new Plateforme();
        String[] data = {
                "villeA;villeB;TRAIN;60;1.7;80",
                "villeB;villeD;TRAIN;22;2.4;40",
                "villeA;villeC;TRAIN;42;1.4;50",
                "villeB;villeC;TRAIN;14;1.4;60",
                "villeC;villeD;AVION;110;150;22",
                "villeC;villeD;TRAIN;65;1.2;90"
        };
        plateforme.chargerDepuisTableau(data);
    }

    @Test
    void chargerDepuisTableau() {
        assertEquals(6, plateforme.getTrajets().size());
        assertEquals(4, plateforme.getVilles().size());
    }

    @Test
    void chargerDepuisTableauDonneesInvalides() {
        assertThrows(IllegalArgumentException.class, () -> {
            plateforme.chargerDepuisTableau(new String[]{"villeA;villeB;TRAIN;60;1.7"});
        });
    }

    @Test
    void chargerDepuisTableauCoutsNegatifs() {
        assertThrows(IllegalArgumentException.class, () -> {
            plateforme.chargerDepuisTableau(new String[]{"villeA;villeB;TRAIN;-60;1.7;80"});
        });
    }

    @Test
    void filtrerParModalite() {
        assertEquals(5, plateforme.filtrerParModalite(ModaliteTransport.TRAIN).size());
        assertEquals(1, plateforme.filtrerParModalite(ModaliteTransport.AVION).size());
    }

    @Test
    void getVille() {
        assertNotNull(plateforme.getVille("villeA"));
        assertNull(plateforme.getVille("villeZ"));
    }

    @Test
    void cheminExiste() {
        assertTrue(plateforme.cheminExiste("villeA", "villeB", ModaliteTransport.TRAIN));
        assertFalse(plateforme.cheminExiste("villeA", "villeD", ModaliteTransport.TRAIN));
        assertFalse(plateforme.cheminExiste("villeA", "villeZ", ModaliteTransport.TRAIN));
    }

    @Test
    void getTrajetsTries() {
        List<Trajet> tries = plateforme.getTrajetsTries(TypeCout.PRIX);
        assertTrue(tries.get(0).getCout().getValeur(TypeCout.PRIX) <= tries.get(1).getCout().getValeur(TypeCout.PRIX));
    }

    @Test
    void filtrerParBorne() {
        List<Trajet> resultat = plateforme.filtrerParBorne(plateforme.getTrajets(), TypeCout.PRIX, 50.0);
        for (Trajet t : resultat) {
            assertTrue(t.getCout().getValeur(TypeCout.PRIX) <= 50.0);
        }
    }
}