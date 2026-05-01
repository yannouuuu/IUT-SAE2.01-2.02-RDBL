package sae.transport.comparison.models;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CoutTest {
    private Cout cout;

    @BeforeEach
    void setUp() {
        cout = new Cout(60.0, 80.0, 1.7);
    }

    @Test
    void testGetValeurPrix() {
        Cout cout = new Cout(60.0, 80.0, 1.7);
        assertEquals(60.0, cout.getValeur(TypeCout.PRIX));
    }

    @Test
    void testGetValeurTemps() {
        Cout cout = new Cout(60.0, 80.0, 1.7);
        assertEquals(80.0, cout.getValeur(TypeCout.TEMPS));
    }

    @Test
    void testGetValeurCO2() {
        Cout cout = new Cout(60.0, 80.0, 1.7);
        assertEquals(1.7, cout.getValeur(TypeCout.CO2));
    }

    @Test
    void testGetValeurZero() {
        Cout cout = new Cout(0.0, 0.0, 0.0);
        assertEquals(0.0, cout.getValeur(TypeCout.PRIX));
    }
}