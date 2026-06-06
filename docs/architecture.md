# Architecture & Code

Ce document détaille la structure interne du projet, les bibliothèques tierces intégrées et l'architecture logicielle globale.

## Structure du projet

```
IUT-SAE2.01-2.02/
│
├── pom.xml                                   ← fichier de config Maven
│
├── lib/                                      ← bibliothèques JAR fournies manuellement
│   ├── sae-s2-2026.jar                       ← JAR fourni par l'IUT (k plus courts chemins)
│   ├── jgrapht-core-1.5.1.jar                ← bibliothèque de graphes JGraphT
│   ├── jheaps-0.14.jar                       ← structure de tas utilisée par JGraphT
│   └── Licences/                             ← licences open source des JAR ci-dessus
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── module-info.java              ← déclaration du module Java (accès JavaFX)
│   │   │   └── sae/transport/comparison/
│   │   │       ├── AppFX.java                ← point d'entrée de l'application (main)
│   │   │       ├── controllers/              ← logique des boutons / interactions IHM
│   │   │       ├── models/                   ← classes métier (Plateforme, Voyageur, etc.)
│   │   │       ├── views/                    ← composants graphiques personnalisés
│   │   │       └── graphs/                   ← graphes et algos k plus courts chemins
│   │   │
│   │   └── resources/
│   │       └── sae/transport/comparison/
│   │           ├── primary.fxml              ← vue principale FXML
│   │           └── secondary.fxml            ← vue secondaire FXML
│   │
│   └── test/
│       ├── java/                             ← classes de tests JUnit 5
│       └── resources/                        ← fichiers CSV utilisés dans les tests
│
├── docs/
│   ├── sujetSAE2.01-2.02-2026.pdf            ← énoncé du projet
│   ├── graphes/                              ← données CSV et rapports graphes
│   ├── ihm/                                  ← maquettes basse/haute fidélité
│   └── poo/                                  ← diagrammes UML
│
└── target/                                   ← généré par Maven (NE PAS commiter)
```

---

## Les bibliothèques utilisées

| Bibliothèque        | Version | Rôle                                                                     | Chargée comment             |
|---------------------|---------|--------------------------------------------------------------------------|-----------------------------|
| **JavaFX Controls** | 21.0.7  | Composants graphiques (boutons, labels…)                                 | Via Maven (auto)            |
| **JavaFX FXML**     | 21.0.7  | Liaison fichiers `.fxml` ↔ contrôleurs                                   | Via Maven (auto)            |
| **JavaFX Graphics** | 21.0.7  | Moteur de rendu graphique                                                | Via Maven (auto)            |
| **JUnit Jupiter**   | 5.10.0  | Tests unitaires                                                          | Via Maven (auto, test only) |
| **sae-s2-2026.jar** |  2026   | Fourni par l'IUT — calcul des k plus courts chemins dans un graphe valué | `lib/` (local)              |
| **jgrapht-core**    | 1.5.1   | Construction de graphes valués (sommets = lieux, arêtes = connexions)    | `lib/` (local)              |
| **jheaps**          |  0.14   | Structure de tas utilisée en interne par JGraphT                         | `lib/` (local)              |

> **Note :** Les JAR dans `lib/` sont référencés directement depuis le `pom.xml` avec leur chemin local (`systemPath`), car ils ne sont pas disponibles sur les dépôts Maven publics.

---

### Détail des packages

- **`AppFX.java`** : point d'entrée JavaFX.
- **`AppTerminal.java`** : point d'entrée pour la CLI (sans GUI).
- **`controllers/`** : gère les événements utilisateurs (clics, saisies) et lie l'UI au modèle.
- **`models/`** : classes métier sans UI (Lieu, Connexion, ModalitéTransport, TypeCout, Plateforme, etc.).
- **`views/`** : éléments graphiques customisés (si besoin).
- **`graphs/`** : construction du graphe valué à l'aide de JGraphT et calcul des K plus courts chemins.
- **`resources/*.fxml`** : descriptions XML de l'interface graphique.
- **`module-info.java`** : déclaration du module Java (obligatoire pour JavaFX > 9).
