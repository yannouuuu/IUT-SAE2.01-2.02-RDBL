# Démarrage Rapide

Ce document explique comment préparer l'environnement, compiler et lancer le projet.

## Prérequis

Avant de commencer, il faut avoir installé :

- **Java 11 ou supérieur** — vérifier avec `java -version`
- **Maven** — vérifier avec `mvn -version`

> **Note :** Maven s'installe facilement :
> - **macOS** : `brew install maven`
> - **Linux** : `sudo apt install maven`
> - **Windows** : télécharger sur [maven.apache.org](https://maven.apache.org/download.cgi) et ajouter au PATH

---

## Comment ça marche (Maven en deux mots)

**Maven** est un outil qui automatise trois choses pour vous :

1. **Télécharger les dépendances** (bibliothèques Java) : plus besoin d'aller chercher les JAR à la main sur internet, Maven s'en charge.
2. **Compiler le projet** : il sait exactement dans quel ordre compiler les fichiers `.java`.
3. **Lancer / tester** : il connaît le point d'entrée de l'application et les tests à exécuter.

Toute la configuration est dans le fichier **`pom.xml`** à la racine du projet. C'est lui qui dit :
- quelle version de Java utiliser (ici Java 17)
- quelles bibliothèques sont nécessaires (JavaFX, JUnit, JGraphT…)
- quelles classes contiennent les points d'entrée (`AppFX` pour l'IHM, `AppTerminal` pour le terminal)
- comment adapter la compilation selon l'OS (Mac ARM, Mac x86, Linux, Windows)

Le dossier `target/` est créé automatiquement par Maven quand on compile. Il contient les fichiers `.class` et les JAR générés. **Il ne faut pas le versionner** (il est normalement dans le `.gitignore`).

---

## Lancer le projet

Dans un terminal, à la racine du projet (là où se trouve `pom.xml`) :

### Application JavaFX (interface graphique)

```bash
mvn clean javafx:run
```

- `clean` : supprime l'ancien dossier `target/` pour repartir de zéro
- `javafx:run` : compile puis lance l'application JavaFX

### Application Terminal (ligne de commande)

```bash
mvn clean compile exec:java
```

- `compile` : compile le projet avant l'exécution (sans ça, `exec:java` cherche des classes qui n'existent pas après `clean`)
- `exec:java` : lance `AppTerminal` dans le terminal courant

> *La première fois, Maven va télécharger les dépendances (JavaFX, JUnit…). C'est normal, ça peut prendre une minute.*

---

## Générer les JAR

### JAR terminal

```bash
mvn clean package
```

Le fichier `E5-app-cli.jar` est généré à la racine du projet.

### JAR interface graphique par plateforme

JavaFX contient des bibliothèques natives différentes selon le système. Il faut donc générer un JAR graphique par plateforme, avec un seul classifier JavaFX à la fois :

```bash
mvn clean package -Djavafx.platform=mac-aarch64
mvn clean package -Djavafx.platform=mac
mvn clean package -Djavafx.platform=linux
mvn clean package -Djavafx.platform=win
```

Ces commandes génèrent les fichiers suivants à la racine du projet :

- `E5-app-gui-mac-aarch64.jar` pour macOS Apple Silicon
- `E5-app-gui-mac.jar` pour macOS Intel
- `E5-app-gui-linux.jar` pour Linux
- `E5-app-gui-win.jar` pour Windows

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
