package sae.transport.comparison.models;

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.ulille.but.sae_s2_2026.ModaliteTransport;
import sae.transport.comparison.exceptions.AucunCheminException;
import sae.transport.comparison.exceptions.DonneesInvalidesException;

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

    // V1 — chargement

    @Test
    void chargerDepuisTableau_nombreVillesEtTrajets() {
        assertEquals(4, plateforme.getVilles().size());
        assertEquals(6, plateforme.getTrajets().size());
    }

    @Test
    void chargerDepuisTableau_ligneIncomplete_leveException() {
        DonneesInvalidesException exception = null;
        try {
            plateforme.chargerDepuisTableau(new String[]{"villeA;villeB;TRAIN;60;1.7"});
        } catch (DonneesInvalidesException e) {
            exception = e;
        }
        assertNotNull(exception);
    }

    @Test
    void chargerDepuisTableau_coutsNegatifs_leveException() {
        DonneesInvalidesException exception = null;
        try {
            plateforme.chargerDepuisTableau(new String[]{"villeA;villeB;TRAIN;-60;1.7;80"});
        } catch (DonneesInvalidesException e) {
            exception = e;
        }
        assertNotNull(exception);
    }

    // V1 — filtrage

    @Test
    void filtrerParModalite_train() {
        assertEquals(5, plateforme.filtrerParModalite(ModaliteTransport.TRAIN).size());
    }

    @Test
    void filtrerParModalite_avion() {
        assertEquals(1, plateforme.filtrerParModalite(ModaliteTransport.AVION).size());
    }

    @Test
    void getVille_existante() {
        assertNotNull(plateforme.getVille("villeA"));
    }

    @Test
    void getVille_inexistante() {
        assertNull(plateforme.getVille("villeZ"));
    }

    @Test
    void cheminExiste_cheminValide() throws AucunCheminException {
        assertTrue(plateforme.cheminExiste("villeA", "villeB", ModaliteTransport.TRAIN));
    }

    @Test
    void cheminExiste_villeInconnue_leveException() {
        AucunCheminException exception = null;
        try {
            plateforme.cheminExiste("villeA", "villeZ", ModaliteTransport.TRAIN);
        } catch (AucunCheminException e) {
            exception = e;
        }
        assertNotNull(exception);
    }

    @Test
    void cheminExiste_aucunChemin_leveException() {
        AucunCheminException exception = null;
        try {
            plateforme.cheminExiste("villeA", "villeD", ModaliteTransport.TRAIN);
        } catch (AucunCheminException e) {
            exception = e;
        }
        assertNotNull(exception);
    }

    @Test
    void getTrajetsTries_premierEstLeMoinsCher() {
        List<Trajet> tries = plateforme.getTrajetsTries(TypeCout.PRIX);
        assertTrue(tries.get(0).getCout().getValeur(TypeCout.PRIX)
                <= tries.get(1).getCout().getValeur(TypeCout.PRIX));
    }

    @Test
    void filtrerParBorne_tousRespectentLaLimite() {
        List<Trajet> resultat = plateforme.filtrerParBorne(plateforme.getTrajets(), TypeCout.PRIX, 50.0);
        for (Trajet t : resultat) {
            assertTrue(t.getCout().getValeur(TypeCout.PRIX) <= 50.0);
        }
    }

    // V2 — points d'intérêt

    @Test
    void getPointsInteret_memeMode_gardeDebutEtFin() {
        List<Trajet> train = plateforme.filtrerParModalite(ModaliteTransport.TRAIN);
        List<Trajet> points = plateforme.getPointsInteret(train);
        assertEquals(train.get(0), points.get(0));
        assertEquals(train.get(train.size() - 1), points.get(points.size() - 1));
    }

    @Test
    void getPointsInteret_changementDeMode_ajoutePoint() throws DonneesInvalidesException {
        Plateforme p2 = new Plateforme();
        p2.chargerDepuisTableau(new String[]{
            "villeA;villeB;TRAIN;60;1.7;80",
            "villeB;villeC;AVION;110;150;22"
        });
        List<Trajet> points = p2.getPointsInteret(p2.getTrajets());
        assertTrue(points.size() >= 2);
    }

    // V3 — préférences multi-critères

    @Test
    void getTrajetsTriesParPreferences_retourneTousLesTrajets() {
        Voyageur voyageur = new Voyageur("Alice", TypeCout.PRIX);
        List<Trajet> tries = plateforme.getTrajetsTriesParPreferences(voyageur);
        assertEquals(plateforme.getTrajets().size(), tries.size());
    }

    @Test
    void getTrajetsTriesParPreferences_triCroissant() {
        Map<TypeCout, Double> prefs = new EnumMap<>(TypeCout.class);
        prefs.put(TypeCout.PRIX, 0.5);
        prefs.put(TypeCout.TEMPS, 0.3);
        prefs.put(TypeCout.CO2, 0.2);
        Voyageur voyageur = new Voyageur("Bob", prefs);
        List<Trajet> tries = plateforme.getTrajetsTriesParPreferences(voyageur);
        for (int i = 0; i < tries.size() - 1; i++) {
            double coutI       = voyageur.calculerCoutComposite(tries.get(i).getCout());
            double coutSuivant = voyageur.calculerCoutComposite(tries.get(i + 1).getCout());
            assertTrue(coutI <= coutSuivant);
        }
    }

    // V3 — historique et sérialisation

    @Test
    void voyage_coutTotalCorrect() {
        List<Trajet> trajets = plateforme.filtrerParModalite(ModaliteTransport.TRAIN);
        Voyage voyage = new Voyage(trajets);
        double attendu = 0.0;
        for (Trajet t : trajets) {
            attendu += t.getCout().getValeur(TypeCout.PRIX);
        }
        assertEquals(attendu, voyage.getCoutTotal(TypeCout.PRIX), 0.001);
    }

    @Test
    void voyageur_ajoutVoyageDansHistorique() {
        Voyageur voyageur = new Voyageur("Alice", TypeCout.PRIX);
        voyageur.ajouterVoyage(new Voyage(plateforme.filtrerParModalite(ModaliteTransport.TRAIN)));
        assertEquals(1, voyageur.getHistorique().size());
    }

    @Test
    void voyageur_totalHistoriqueCumule() {
        Voyageur voyageur = new Voyageur("Alice", TypeCout.PRIX);
        List<Trajet> trajets = plateforme.filtrerParModalite(ModaliteTransport.TRAIN);
        Voyage voyage = new Voyage(trajets);
        voyageur.ajouterVoyage(voyage);
        voyageur.ajouterVoyage(voyage);
        assertEquals(voyage.getCoutTotal(TypeCout.PRIX) * 2, voyageur.getTotalHistorique(TypeCout.PRIX), 0.001);
    }

    @Test
    void historiqueManager_sauvegardeEtCharge() throws IOException {
        String fichier = System.getProperty("java.io.tmpdir") + "/test_historique.ser";
        HistoriqueManager manager = new HistoriqueManager(fichier);
        Voyage voyage = new Voyage(plateforme.filtrerParModalite(ModaliteTransport.TRAIN));

        manager.ajouterEtSauvegarder(voyage);
        List<Voyage> charge = manager.charger();

        assertEquals(1, charge.size());
        assertEquals(voyage.getVilleDepart(), charge.get(0).getVilleDepart());
        new File(fichier).delete();
    }

    @Test
    void historiqueManager_fichierInexistant_retourneListeVide() throws IOException {
        HistoriqueManager manager = new HistoriqueManager("/tmp/inexistant_test.ser");
        assertEquals(0, manager.charger().size());
    }
}