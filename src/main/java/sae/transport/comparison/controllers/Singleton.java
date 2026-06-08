package sae.transport.comparison.controllers;

public class Singleton {
    static Singleton instance;
    private double temps = 100/3;
    private double co2 = 100/3;
    private double prix = 100/3 + 1;

    public static Singleton getInstance() {
        if(instance == null){instance = new Singleton();}
        return instance;
    }

    public double getTemps() {
        return temps;
    }
    public void setTemps(double temps) {
        this.temps = temps;
    }

    public double getCo2() {
        return co2;
    }

    public void setCo2(double co2) {
        this.co2 = co2;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }
}
