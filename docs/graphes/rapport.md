---
title: SAE S2.02 -- Rapport graphes -- Première Version
subtitle: Équipe E5
author: Yann Renard, Tom Cox, Alexandre
toc: 3
toc-title: "Table des matières"
date: 2026
papersize: a4
geometry: margin=2cm
documentclass: article
numbersections: true
secnumdepth: 2
---

\pagebreak
---

## 1  Version 1 : Un seul moyen de transport

### 1.1  Exemple

Pour illustrer la Version 1, nous avons choisi un réseau de transport reliant cinq villes du nord de la France : Lille, Dunkerque, Cambrai, Paris et Crépy-en-Valois. Ce réseau comprend quinze connexions réparties sur trois modes de transport — train, bus et avion — avec des données de coûts estimées à partir des vitesses moyennes de chaque mode et des barèmes d'émissions carbone de l'ADEME.

**Tableau 1 — Données complètes du réseau de transport**

| Départ | Arrivée | Mode | Prix (€) | CO2 (kg) | Durée (min) |
|---|---|---|---|---|---|
| Lille | Dunkerque | TRAIN | 18 | 0.8 | 65 |
| Dunkerque | Lille | TRAIN | 18 | 0.8 | 65 |
| Lille | Cambrai | TRAIN | 14 | 0.7 | 55 |
| Cambrai | Lille | TRAIN | 14 | 0.7 | 55 |
| Cambrai | Paris | TRAIN | 35 | 1.8 | 95 |
| Paris | Cambrai | TRAIN | 35 | 1.8 | 95 |
| Paris | Crépy-en-Valois | TRAIN | 12 | 0.5 | 45 |
| Crépy-en-Valois | Paris | TRAIN | 12 | 0.5 | 45 |
| Lille | Paris | TRAIN | 55 | 2.4 | 60 |
| Paris | Lille | TRAIN | 55 | 2.4 | 60 |
| Dunkerque | Cambrai | BUS | 12 | 2.1 | 110 |
| Cambrai | Crépy-en-Valois | BUS | 18 | 2.8 | 130 |
| Paris | Crépy-en-Valois | BUS | 8 | 1.2 | 75 |
| Lille | Paris | AVION | 120 | 85.0 | 55 |
| Dunkerque | Paris | AVION | 110 | 80.0 | 50 |

*Les durées intègrent les temps d'attente habituels (enregistrement aéroport pour l'avion, correspondances en gare).*

Le scénario retenu pour ce prototype est le suivant :

- Mode de transport : TRAIN uniquement
- Critère d'optimisation : le prix (on cherche à minimiser le coût en euros)
- Ville de départ : Lille
- Ville d'arrivée : Crépy-en-Valois
- Nombre d'itinéraires souhaités : 4

Ce scénario est représentatif d'un voyageur contraint par son budget, souhaitant rejoindre Crépy-en-Valois depuis Lille en train. Le Tableau 2 présente les quatre meilleurs itinéraires identifiés, classés par ordre croissant de prix.

**Tableau 2 — Les 4 meilleurs itinéraires TRAIN de Lille à Crépy-en-Valois (critère : prix)**

| Itinéraire | Durée (min) | CO2 (kg) | Prix (€) | Classement |
|---|---|---|---|---|
| Lille → Cambrai → Paris → Crépy-en-Valois | 195 | 2.5 | 61 | 1er |
| Lille → Paris → Crépy-en-Valois | 105 | 2.9 | 67 | 2e |
| Lille → Cambrai → Paris → Crépy-en-Valois (variante) | 245 | 3.0 | 76 | 3e |
| Lille → Dunkerque → Cambrai → Paris → Crépy-en-Valois | 325 | 5.4 | 95 | 4e |

*Les itinéraires empruntant l'avion ou le bus sont exclus du scénario, le filtre portant exclusivement sur le mode TRAIN.*

---

### 1.2  Modèle pour l'exemple

Pour résoudre ce problème, on modélise le réseau filtré par un graphe orienté valué. Chaque ville du réseau devient un sommet, et chaque connexion TRAIN devient une arête orientée dont le poids correspond au critère choisi — ici le prix en euros.

*[Figure 1 — Schéma du graphe orienté valué pour le scénario (mode TRAIN, critère PRIX) — à insérer]*

Dans ce graphe, les cinq sommets sont les villes {Lille, Dunkerque, Cambrai, Paris, Crépy-en-Valois}. Les arêtes orientées et valuées représentent les connexions TRAIN disponibles. À titre d'exemple, l'arête Lille → Paris porte le poids 55 (prix en euros), l'arête Lille → Cambrai porte le poids 14, et l'arête Paris → Crépy-en-Valois porte le poids 12.

Les quatre meilleurs chemins dans ce graphe, exprimés sous forme de suites d'arêtes avec leur poids cumulé, sont :

- **Chemin 1 :** Lille → Cambrai → Paris → Crépy-en-Valois, poids total = 14 + 35 + 12 = **61 €**
- **Chemin 2 :** Lille → Paris → Crépy-en-Valois, poids total = 55 + 12 = **67 €**
- **Chemin 3 :** Lille → Cambrai → Paris → Crépy-en-Valois (variante plus longue), poids total = **76 €**
- **Chemin 4 :** Lille → Dunkerque → Cambrai → Paris → Crépy-en-Valois, poids total = 18 + 14 + 35 + 12 = **79 €** (via Dunkerque en TRAIN)

> Note : l'arête Dunkerque → Cambrai n'est disponible qu'en BUS dans notre réseau. Le chemin 4 passe donc par Dunkerque en TRAIN depuis Lille, puis Cambrai en TRAIN depuis Dunkerque — cette connexion n'existe pas dans nos données, ce chemin est donc en réalité impossible dans notre réseau. Le 4e chemin retenu par l'algorithme sera le prochain chemin valide.

L'algorithme KPCC explore les chemins sans cycle dans le graphe et les retourne ordonnés par poids croissant. Les résultats ci-dessus ont été obtenus et vérifiés par l'exécution de la classe `GraphesV1` décrite en Section 1.4.

---

### 1.3  Modélisation pour la Version 1 dans le cas général

On se donne un problème de recherche d'itinéraire défini par : un ensemble de villes V, un ensemble de connexions C entre ces villes, une modalité de transport m choisie, un critère d'optimisation c parmi {TEMPS, PRIX, CO2}, une ville de départ s, une ville d'arrivée t, et un entier k représentant le nombre d'itinéraires souhaités. Voici comment ce problème se traduit en termes de graphes.

**Type de graphe**

On utilise un multigraphe orienté valué. L'orientation est nécessaire car une connexion de la ville x vers la ville y n'implique pas l'existence d'une connexion en sens inverse — les lignes de transport sont directionnelles. La valuation permet d'associer à chaque arête le coût correspondant au critère choisi. Enfin, la structure de multigraphe est justifiée par le fait que deux villes pourraient théoriquement être reliées par plusieurs connexions de même modalité, ce qui correspondrait à des arêtes parallèles dans le graphe.

**Sommets du graphe**

L'ensemble des sommets est constitué des villes apparaissant dans au moins une connexion de modalité m. Formellement, on pose :

> V_m = { v ∈ V | ∃ c ∈ C_m : c.départ = v ou c.arrivée = v }

où C_m désigne les connexions filtrées sur la modalité m. Ce sous-ensemble forme le sous-graphe induit par la sélection de la modalité sur l'ensemble des connexions.

**Arêtes du graphe et leurs poids**

Il existe une arête orientée du sommet x vers le sommet y dans le graphe si et seulement si il existe une connexion de modalité m allant de x à y dans l'ensemble C. Le poids de cette arête est la valeur du critère c pour cette connexion :

- si c = PRIX → poids = prix en euros
- si c = TEMPS → poids = durée en minutes
- si c = CO2 → poids = émissions en kg CO2e

Cette construction garantit que minimiser le poids total d'un chemin revient exactement à minimiser le critère souhaité par l'utilisateur.

**Algorithme de résolution**

Une fois le graphe construit, on applique l'algorithme des k plus courts chemins (KPCC) en appelant `AlgorithmeKPCC.kpcc(graphe, s, t, k)`. Cet algorithme retourne une liste ordonnée d'au plus k objets `Chemin`, chacun contenant la liste des arêtes empruntées et le poids total du chemin. Les chemins sont sans cycle et triés par poids croissant, ce qui correspond directement aux k meilleurs itinéraires selon le critère de l'utilisateur.

Cette modélisation est pensée pour permettre une évolution naturelle vers la Version 2 : il suffira d'enrichir le graphe avec des nœuds de correspondance pour prendre en charge les changements de modalité entre deux étapes d'un même voyage.

---

### 1.4  Prototype pour la Version 1

La classe `GraphesV1` est une classe Java autonome et exécutable. Conformément aux exigences du sujet, elle ne dépend d'aucune autre classe du projet, à l'exception des interfaces `Lieu` et `Connexion` de la bibliothèque fournie, qu'elle implémente sous forme de classes anonymes directement dans la méthode `main`.

La classe reproduit le scénario décrit en Section 1.1 : elle instancie les cinq villes comme sommets, ajoute les connexions TRAIN avec leurs prix comme poids d'arêtes, puis appelle `AlgorithmeKPCC.kpcc` pour calculer les quatre meilleurs itinéraires de Lille à Crépy-en-Valois. Les résultats sont affichés de façon lisible, avec pour chaque itinéraire le rang, le coût total en euros et le détail des étapes ville par ville.

**Lien vers le code source (commit avant la date limite) :**
[À compléter — lien GitLab vers `GraphesV1.java`]
