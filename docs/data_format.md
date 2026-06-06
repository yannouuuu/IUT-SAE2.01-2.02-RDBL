# Format des Données

L'application traite des données issues de fichiers texte/CSV pour construire son réseau de transport multimodal. Voici les formats attendus :

---

## 1. Réseau de transport (String[] ou CSV)

Ce format est utilisé pour l'initialisation principale des connexions entre différentes villes.

```csv
villeDépart;villeArrivée;modalitéTransport;prix(€);émissionsGES(kgCO2e);durée(min)
```

### Exemple de données :
```csv
villeA;villeB;Train;60;1.7;80
villeA;villeC;Train;42;1.4;50
villeC;villeD;Avion;110;150;22
```

> **Règles de validité :** 
> - Toutes les valeurs de coût doivent être présentes.
> - Elles doivent être positives ou nulles (pas de valeur négative autorisée).

---

## 2. Coûts de correspondance (Version 2, CSV)

Ce format intervient lors de la phase 2 du projet. Il définit les pénalités et surcoûts liés au changement de transport au sein d'une même ville.

```csv
ville;transport1;transport2;durée(min);émissions(kgCO2e);prix(€)
```

### Exemple de données :
```csv
Lille;Train;Avion;130;0.1;20
Lille;Train;Bus;20;0;0
Valenciennes;Train;Bus;10;0;0
```

*Explication : À Lille, passer d'un Train à un Avion ajoute 130 minutes au voyage, 0.1 kg de CO2e, et 20€ de frais de transfert.*
