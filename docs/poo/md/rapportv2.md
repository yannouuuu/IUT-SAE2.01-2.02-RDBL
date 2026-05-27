# SAÉ 2.01-2.02 — Rapport POO
**Versions 1 et 2**
*Yann Renard, Tom Cox, Alexandre Sorel — Groupe E, Équipe E5*

---

## 1  Version 1 : Un seul moyen de transport

### 1.1  Lancement et utilisation

L'application est développée avec Java 21 et Maven. Le code source est disponible sur le dépôt GitLab de l'équipe E5. Pour lancer l'application, il faut d'abord compiler le projet avec Maven, puis exécuter la classe principale.

Compilation et exécution depuis la racine du projet :

```bash
mvn clean exec:java
```

Les fichiers de données CSV doivent être placés dans `src/main/resources/`. La bibliothèque graphes (`sae_s2_2026.jar`, `jgrapht-core-1.5.1.jar`, `jheaps-0.14.jar`) est référencée dans le dossier `lib/` à la racine du projet et déclarée dans le `pom.xml`.

---

### 1.2  Diagramme UML et mécanismes objets

**Tableau 1 : Classes de la Version 1**

| Classe/Enum | Package | Rôle |
|---|---|---|
| `TypeCout` | models | Enum représentant les 3 critères : TEMPS, PRIX, CO2 |
| `Cout` | models | Stocke les 3 valeurs de coût dans une `Map<TypeCout, Double>` |
| `Ville` | models | Implémente `Lieu` (lib IUT). Représente une ville du réseau |
| `Trajet` | models | Implémente `Connexion` (lib IUT). Connexion entre deux `Ville` avec un `Cout` et une `ModaliteTransport` |
| `Voyageur` | models | Représente l'utilisateur : nom et `TypeCout` (critère à optimiser) |
| `Plateforme` | models | Classe centrale : contient les listes de `Ville` et `Trajet`, expose les fonctionnalités de chargement, filtrage et tri |

*[Diagramme UML V1 — à insérer ici]*

#### Mécanismes objets mis en œuvre

**Interfaces et implémentation** : Les classes `Ville` et `Trajet` implémentent les interfaces `Lieu` et `Connexion` fournies par la bibliothèque de l'IUT. Ce choix permet à notre code d'être directement compatible avec la bibliothèque de graphes, qui attend des objets `Lieu` et `Connexion`. C'est le principe de programmation par interface : on dépend d'une abstraction plutôt que d'une implémentation concrète.

**Enumérations** : `TypeCout` et `ModaliteTransport` (fournie par la lib) sont des enums. Ce mécanisme est approprié ici car les valeurs possibles sont fixes et connues à l'avance. Utiliser une enum plutôt qu'une `String` évite les erreurs de frappe et rend le code plus lisible.

**Collections** : La classe `Plateforme` utilise des `List<Ville>` et `List<Trajet>` pour stocker le réseau. La classe `Cout` utilise une `Map<TypeCout, Double>` pour associer chaque type de coût à sa valeur. Ce choix permet d'accéder à n'importe quel critère avec `getValeur(TypeCout)`.

**Encapsulation** : Tous les attributs sont privés. L'accès se fait uniquement via des getters, ce qui protège l'état interne des objets et permet de modifier l'implémentation sans impacter le code appelant.

---

### 1.3  Analyse technique des fonctionnalités

**Tableau 2 : Correspondance fonctionnalités / méthodes**

| Méthode | Fonctionnalité couverte (sujet) |
|---|---|
| `chargerDepuisTableau(String[])` | Vérifier la validité des données et reconstituer le réseau |
| `filtrerParModalite(ModaliteTransport)` | Filtrer les données sur un moyen de transport spécifique |
| `getVille(String)` | Utilitaire interne pour retrouver une ville par son nom |
| `cheminExiste(...)` | Déterminer si il existe un voyage possible entre deux villes |
| `getTrajetsTries(TypeCout)` | Afficher les meilleurs voyages ordonnés selon le critère |
| `filtrerParBorne(...)` | Exclure les alternatives qui excèdent les bornes définies |

La méthode `chargerDepuisTableau` effectue la validation des données en vérifiant que chaque ligne contient exactement 6 colonnes et que tous les coûts sont positifs ou nuls. En cas d'erreur, une `IllegalArgumentException` est levée (remplacée par `DonneesInvalidesException` en Version 2).

La recherche des meilleurs itinéraires repose sur la bibliothèque de graphes : on construit un `MultiGrapheOrienteValue` dont les sommets sont les `Ville` et les arêtes les `Trajet` valuées selon le critère choisi, puis on appelle `AlgorithmeKPCC.kpcc` pour obtenir les k plus courts chemins.

---

### 1.4  Analyse des tests

Deux classes de test ont été développées avec JUnit 5 : `CoutTest` et `PlateformeTest`.

**CoutTest** couvre :
- La création d'un `Cout` avec des valeurs valides
- La récupération correcte de chaque valeur (PRIX, TEMPS, CO2)
- Le cas limite avec des valeurs à zéro

**PlateformeTest** couvre :
- Le chargement correct des données : vérification du nombre de villes (4) et de trajets (6)
- La détection des données invalides : ligne incomplète, coûts négatifs
- Le filtrage par modalité : vérification du nombre de trajets TRAIN et AVION
- La recherche de ville : ville existante et ville inexistante (`null`)
- La vérification d'existence de chemin : chemin existant, chemin inexistant, ville inconnue
- Le tri par critère : vérification que le premier élément est bien le moins coûteux
- Le filtrage par borne : vérification que tous les trajets retournés respectent la limite

Ces tests couvrent les cas simples, les cas limites et les cas d'erreur pour chaque fonctionnalité principale de cette version.

---

## 2  Version 2 : Multi-modalité

### 2.1  Lancement et utilisation

Le lancement est identique à la Version 1. Les nouvelles fonctionnalités (import CSV, exceptions, points d'intérêt) s'utilisent directement via la classe `Plateforme`. Les fichiers CSV de test sont disponibles dans `src/test/resources/` :

- `exemple-reseau.csv` : données du réseau au format `villeDépart;villeArrivée;modalité;prix;co2;durée`
- `exemple-correspondances.csv` : coûts de correspondance au format `ville;mode1;mode2;durée;co2;prix`

Exemple d'utilisation depuis le code :

```java
Plateforme p = new Plateforme();
p.chargerDepuisCSV("src/test/resources/exemple-reseau.csv");
```

---

### 2.2  Diagramme UML et mécanismes objets

**Tableau 3 : Nouveautés de la Version 2**

| Élément ajouté | Justification (sujet) |
|---|---|
| `AucunCheminException` | Signaler l'absence de chemin via un mécanisme d'exception |
| `DonneesInvalidesException` | Signaler les problèmes de validité de données via un mécanisme d'exception |
| `chargerDepuisCSV(String)` | Import des données issues de fichiers CSV |
| `getPointsInteret(List<Trajet>)` | N'afficher que le départ, l'arrivée et les lieux où il y a un changement de modalité |

![Diagramme UML V2](../uml/diagramme_uml_v2.puml)

#### Mécanismes objets mis en œuvre

**Exceptions personnalisées** : Deux classes d'exception ont été créées en étendant `Exception`. `AucunCheminException` est levée lorsque les villes demandées sont inconnues ou qu'aucun trajet ne les relie. `DonneesInvalidesException` est levée lorsque les données sont mal formatées ou contiennent des valeurs invalides. Ce mécanisme permet à l'appelant de distinguer les deux types d'erreur et de les traiter séparément.

**Restructuration de `cheminExiste`** : La méthode ne retourne plus `false` en cas d'absence de chemin : elle lève systématiquement une `AucunCheminException`. Ce choix est plus cohérent avec le principe selon lequel une méthode nommée `cheminExiste` ne devrait jamais retourner `false` si l'appelant s'attend à ce qu'un chemin existe.

**Lecture de fichier** : La méthode `chargerDepuisCSV` utilise un `BufferedReader` avec try-with-resources, ce qui garantit que le fichier est toujours fermé proprement, même en cas d'exception. Les `IOException` sont converties en `DonneesInvalidesException` pour homogénéiser la gestion des erreurs.

---

### 2.3  Analyse technique des fonctionnalités

La méthode `getPointsInteret` prend en entrée une liste de `Trajet` représentant un itinéraire complet et retourne uniquement les points significatifs : le premier trajet, le dernier, et ceux dont la modalité diffère du trajet précédent. Cette méthode répond à l'exigence du sujet : *« n'afficher que le départ, l'arrivée et les lieux où il y a un changement dans la modalité de transport »*.

Les exceptions personnalisées améliorent la lisibilité du code appelant : au lieu d'attraper une `IllegalArgumentException` générique, l'appelant peut traiter spécifiquement les erreurs de données invalides et les absences de chemin.

---

### 2.4  Analyse des tests

Les classes de test existantes ont été mises à jour pour couvrir les nouvelles fonctionnalités.

**PlateformeTest — nouveaux cas de test :**
- `chargerDepuisCSV` : vérification du chargement depuis un fichier CSV valide
- `cheminExiste` avec exception : vérification que `AucunCheminException` est bien levée pour une ville inconnue et pour deux villes sans connexion directe
- `getPointsInteret` : vérification que seuls le départ, l'arrivée et les changements de mode sont retournés
