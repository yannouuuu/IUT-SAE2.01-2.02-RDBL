# SAE 2.01 / 2.02 — Comparaison d'itinéraires de transport

Assistant personnel de voyage permettant de calculer, visualiser et comparer des itinéraires dans un réseau de transport multimodal (train, avion, bus). L'optimisation se fait selon trois critères : **durée (min)**, **prix (€)** et **émissions de CO₂ (kg CO₂e)**.

---

## Sommaire

- [Prérequis](#prérequis)
- [Structure du projet](#structure-du-projet)
- [Comment ça marche (Maven en deux mots)](#comment-ça-marche-maven-en-deux-mots)
- [Lancer le projet](#lancer-le-projet)
- [Compiler sans lancer](#compiler-sans-lancer)
- [Lancer les tests](#lancer-les-tests)
- [Les bibliothèques utilisées](#les-bibliothèques-utilisées)
- [Architecture du code](#architecture-du-code)
- [Format des données](#format-des-données)
- [Versions du projet](#versions-du-projet)
- [Deadlines et tags Git](#deadlines-et-tags-git)
- [Livrables attendus](#livrables-attendus)

---

## Prérequis

Avant de commencer, il faut avoir installé :

- **Java 11 ou supérieur** — vérifier avec `java -version`
- **Maven** — vérifier avec `mvn -version`

> Maven s'installe facilement :
> - **macOS** : `brew install maven`
> - **Linux** : `sudo apt install maven`
> - **Windows** : télécharger sur [maven.apache.org](https://maven.apache.org/download.cgi) et ajouter au PATH

---

## Structure du projet

```
IUT-SAE2.01-2.02/
│
├── pom.xml                                   ← fichier de config Maven (voir ci-dessous)
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
│   │   │       │   ├── PrimaryController.java
│   │   │       │   └── SecondaryController.java
│   │   │       ├── models/                   ← classes métier : Plateforme, Voyageur,
│   │   │       │                               Lieu, Connexion, Voyage, Cout,
│   │   │       │                               ModalitéTransport, TypeCout…
│   │   │       ├── views/                    ← composants graphiques personnalisés
│   │   │       └── graphs/                   ← construction du graphe et calcul
│   │   │                                       des k plus courts chemins
│   │   │
│   │   └── resources/
│   │       └── sae/transport/comparison/
│   │           ├── primary.fxml              ← interface graphique de la vue principale
│   │           └── secondary.fxml            ← interface graphique de la vue secondaire
│   │
│   └── test/
│       ├── java/                             ← classes de tests JUnit 5
│       └── resources/                        ← fichiers CSV / données utilisés dans les tests
│
├── docs/
│   ├── sujetSAE2.01-2.02-2026.pdf            ← énoncé du projet
│   ├── graphes/                              ← données CSV et rapports graphes
│   │   └── rapport_v1.md
│   ├── ihm/                                  ← maquettes basse/haute fidélité
│   └── poo/                                  ← diagrammes UML par version
│
└── target/                                   ← généré automatiquement par Maven
    └── (fichiers compilés, JAR...)           ← NE PAS modifier, NE PAS commiter
```

---

## Comment ça marche (Maven en deux mots)

**Maven** est un outil qui automatise trois choses pour vous :

1. **Télécharger les dépendances** (bibliothèques Java) — plus besoin d'aller chercher les JAR à la main sur internet, Maven s'en charge.
2. **Compiler le projet** — il sait exactement dans quel ordre compiler les fichiers `.java`.
3. **Lancer / tester** — il connaît le point d'entrée de l'application et les tests à exécuter.

Toute la configuration est dans le fichier **`pom.xml`** à la racine du projet. C'est lui qui dit :
- quelle version de Java utiliser (ici Java 11)
- quelles bibliothèques sont nécessaires (JavaFX, JUnit, JGraphT…)
- quelle classe contient le `main` (`AppFX`)
- comment adapter la compilation selon l'OS (Mac ARM, Mac x86, Linux, Windows)

Le dossier `target/` est créé automatiquement par Maven quand on compile. Il contient les fichiers `.class` (bytecode Java) et les JAR générés. **Il ne faut pas le versionner** (il est normalement dans le `.gitignore`).

---

## Lancer le projet

Dans un terminal, à la racine du projet (là où se trouve `pom.xml`) :

```bash
mvn clean javafx:run
```

- `clean` : supprime l'ancien dossier `target/` pour repartir de zéro
- `javafx:run` : compile puis lance l'application JavaFX

> La première fois, Maven va télécharger les dépendances (JavaFX, JUnit…). C'est normal, ça peut prendre une minute.

---

## Compiler sans lancer

Si vous voulez juste vérifier que le code compile sans l'exécuter :

```bash
mvn compile
```

---

## Lancer les tests

```bash
mvn test
```

Maven compile les classes de test (`src/test/java/`) et les exécute avec JUnit 5. Les résultats s'affichent dans le terminal.

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

Les JAR dans `lib/` sont référencés directement depuis le `pom.xml` avec leur chemin local (`systemPath`), car ils ne sont pas disponibles sur les dépôts Maven publics.

---

## Architecture du code

Le projet suit une architecture **MVC** (Modèle - Vue - Contrôleur) et s'appuie sur des interfaces et énumérations imposées par le sujet :

```
AppFX.java                                                      → lance l'application, charge les vues FXML
    │
    ├── resources/ (.fxml)                                      → ce que l'utilisateur voit (FXML)
    │       ↕ liaison FXML
    ├── controllers/                                            → réagit aux actions de l'utilisateur
    │       ↕ appelle
    ├── models/                                                 → logique métier, données
    │   ├── «interface» Lieu
    │   ├── «interface» Connexion
    │   │       getDepart() : Lieu
    │   │       getArrivee() : Lieu
    │   │       getModalite() : ModalitéTransport
    │   ├── «enum» ModalitéTransport  (TRAIN, AVION, BUS…)
    │   ├── «enum» TypeCout           (CO2, TEMPS, PRIX)
    │   ├── Cout                    → Map<TypeCout, Double>
    │   ├── Voyage                                              → liste de Connexions
    │   ├── Voyageur                                            → nom + critère à optimiser
    │   └── Plateforme                                          → ensemble de Lieux + Connexions,
    │                                                             point d'entrée pour comparer des voyages
    │       ↕ utilise
    └── graphs/                                                 → construit le graphe JGraphT à partir
                                                                  d'une Plateforme et calcule les k plus
                                                                  courts chemins selon les préférences
```

**Détail des packages :**

- **`AppFX.java`** : point d'entrée (`main`). Initialise JavaFX et charge la première vue FXML.
- **`controllers/`** : chaque contrôleur est lié à un fichier `.fxml`. Il reçoit les événements (clics, saisies…) et délègue au modèle.
- **`models/`** : classes Java pures, sans interface graphique. Contient les interfaces `Lieu` et `Connexion` (imposées par le sujet), les énumérations `ModalitéTransport` et `TypeCout`, ainsi que les classes `Plateforme`, `Voyageur`, `Voyage` et `Cout`.
- **`views/`** : éventuels composants graphiques personnalisés (hors FXML).
- **`graphs/`** : construit le graphe valué (sommets = lieux, arêtes = connexions) et utilise la bibliothèque SAE pour calculer les k plus courts chemins.
- **`resources/*.fxml`** : fichiers XML décrivant l'interface graphique, liés à leur contrôleur via `fx:controller`.
- **`module-info.java`** : déclare le module Java 9+, nécessaire pour que JavaFX puisse accéder aux contrôleurs via FXML.

---

## Format des données

### Réseau de transport (String[] ou CSV)

```
villeDépart;villeArrivée;modalitéTransport;prix(€);émissionsGES(kgCO2e);durée(min)
```

Exemple :
```
villeA;villeB;Train;60;1.7;80
villeA;villeC;Train;42;1.4;50
villeC;villeD;Avion;110;150;22
```

Règles de validité : toutes les valeurs de coût doivent être présentes et positives ou nulles.

### Coûts de correspondance (Version 2, CSV)

```
ville;transport1;transport2;durée(min);émissions(kgCO2e);prix(€)
```

Exemple :
```
Lille;Train;Avion;130;0.1;20
Lille;Train;Bus;20;0;0
Valenciennes;Train;Bus;10;0;0
```

Ces coûts s'appliquent lors d'un changement de modalité de transport dans une ville donnée.

---

## Versions du projet

### Version 1 — Transport mono-modal

L'utilisateur choisit **un seul moyen de transport** et **un seul critère** d'optimisation (durée, prix ou CO₂). L'application :
- valide les données d'entrée
- filtre les connexions selon la modalité choisie
- calcule les meilleurs itinéraires (plus courts chemins dans un graphe)
- écarte les voyages dépassant une borne définie par l'utilisateur (ex : "minimiser le CO₂ mais durée max 180 min")

### Version 2 — Transport multimodal

Les voyages peuvent combiner plusieurs moyens de transport. Chaque changement de modalité engendre un **coût de correspondance** (temps + CO₂ + prix) dépendant de la ville et des transports concernés. L'application signale aussi les erreurs via des **exceptions** et filtre l'affichage pour ne montrer que les points de changement.

### Version 3 — Multi-critères + historique

L'utilisateur peut exprimer des **préférences relatives** sur plusieurs critères simultanément (ex : "je préfère le prix, mais l'environnement compte aussi"). L'application enregistre les voyages via **sérialisation binaire** et affiche l'évolution de l'historique (budget dépensé, CO₂ cumulé…).

---

## Deadlines et tags Git

Les commits doivent être **étiquetés** (tags Git) selon le calendrier suivant :

|   Tag    |  Deadline   | Contenu                                                                                           |
|----------|-------------|---------------------------------------------------------------------------------------------------|
| `POO-v1` | **8 mai**   | Classes de base, Version 1 fonctionnelle, tests, diagramme UML v1 + rapport graphes v1 sur Moodle |
| `IHM-v1` | **25 mai**  | Maquettes basse fidélité                                                                          |
| `POO-v2` | **29 mai**  | Version 2 multimodale, exceptions, import CSV, UML v2 + rapport graphes v2 sur Moodle             |
| `POO-v3` | **12 juin** | Version 3 multi-critères, historique, sérialisation                                               |
| `IHM-v2` | **12 juin** | IHM JavaFX terminée, prototype haute fidélité                                                     |

Créer un tag Git :
```bash
git tag POO-v1
git push origin POO-v1
```

---

## Livrables attendus

### Partie POO (R2.01 / R2.03)
- Diagramme UML pour chaque version (dans `docs/poo/`)
- Rapport PDF cumulatif à rendre sur Moodle (mode de lancement, UML, analyse technique, analyse des tests)
- **JAR sans interface graphique** à la racine du dépôt GitLab

### Partie Graphes (R2.07)
- Rapport PDF sur Moodle (modélisation graphe v1 + v2, classes de test)

### Partie IHM (R2.02)
- Archive ZIP contenant :
  - **JAR exécutable** (JavaFX 21.0.7)
  - Export HTML ou PDF des mockups dans un dossier `mockups/`
  - Compte rendu PDF (noms, groupe, lien GitLab, captures d'écran, justification des choix ergonomiques, contributions de chaque membre)
  - **Vidéo de présentation de 2-3 minutes** à destination de personnes extérieures au projet
