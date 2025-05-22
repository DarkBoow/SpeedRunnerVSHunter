# SpeedRunnerVSHunter

SpeedRunnerVSHunter est un plugin **Spigot** inspiré du concept de *Speedrunner VS Hunter* popularisé par Dream. Il permet d’organiser des parties où les Speedrunners doivent terminer le jeu tandis que les Hunters les pourchassent grâce à une boussole.

Ce projet cible la version **1.16** de Spigot et inclut plusieurs options pour adapter la difficulté ou le comportement de la partie.

## Fonctionnement

- Choix de l’équipe via un inventaire interactif : Speedrunner ou Chasseur.
- Les chasseurs reçoivent une boussole pointant automatiquement vers le Speedrunner le plus proche, ou vers une cible précise définie avec la commande `cible`.
- Possibilité d’activer le mode **Assassins** : un chasseur qui fixe un Speedrunner est paralysé.
- Gestion des événements : victoire des Hunters si tous les Speedrunners sont éliminés, victoire des Speedrunners si l’Ender Dragon est vaincu.
- Protection du monde en dehors des parties (dégâts, blocs, inventaires…) via la section `OffGameProtection` du fichier de configuration.

## Fichiers de configuration

### config.yml

Options principales&nbsp;:

- `Play_Permission` *(boolean)* : impose la permission `speedrunnervshunter.player` pour jouer.
- `OffGameProtection` *(section)* : désactive divers comportements hors partie (dégâts, pose/casse de blocs, drop d’objet, etc.).
- `StartWorldBorder` *(double|SpawnRadiusGamerule)* : taille de la bordure au lancement ou valeur issue de la gamerule `spawnRadius`.
- `WorldStartTime` *(long)* : heure du monde au démarrage (mettre `-1` pour laisser l’heure actuelle).
- `ResetHealthOnStop` *(boolean)* : réinitialise la vie de tous les joueurs à l’arrêt de la partie.
- `ResetAchievements` *(boolean)* : réinitialise les avancements lorsque la partie commence.
- `GameOptions` *(section)* :
  - `SpeedRunnersBecomesHuntersAtDeath` *(boolean)* : un Speedrunner mort rejoint les Hunters.
  - `SpectatorAfterSpeedRunnerRealDeath` *(boolean)* : passage en mode Spectateur après la mort d’un Speedrunner (si l’option précédente est désactivée).
  - `AssassinsMode` *(boolean)* : active le gel des Hunters lorsqu’ils regardent un Speedrunner.

### lang.yml

Contient l’ensemble des messages du plugin (exemple&nbsp;: titres de victoire, messages d’erreur…). Le fichier fourni est en français par défaut et peut être modifié pour personnaliser le plugin.

## Commandes

Commande principale&nbsp;: `/speedrunnervshunter` (alias `svh`, `speedrunner`, `srvsh`).

Sous-commandes courantes&nbsp;:

- `/speedrunnervshunter start` – Démarre la partie. Requiert la permission `speedrunnervshunter.admin`.
- `/speedrunnervshunter stop` – Stoppe la partie en cours (même permission).
- `/speedrunnervshunter cible` ou `/speedrunnervshunter target` – Ouvre le menu de sélection de cible pour un Chasseur.
- `/speedrunnervshunter leave` – Quitte son équipe actuelle.
- `/speedrunnervshunter join <speedrunners|hunters> <joueur>` – Force un joueur à rejoindre une équipe (admin).

## Compilation / Installation

Ce projet utilise **Maven**. La compilation génère un fichier `*-shaded.jar` prêt à être installé sur un serveur Spigot&nbsp;:

```bash
mvn package -DskipTests
```

Le jar compilé se trouve dans le dossier `target/` (ex. `SpeedRunnerVSHunter-1.6-shaded.jar`). Placez ce fichier dans le répertoire `plugins/` de votre serveur.

## Fichiers importants

- `src/main/java/fr/darkbow_/speedrunnervshunter/SpeedRunnerVSHunter.java` – classe principale du plugin.
- `CommandSpeedRunnerVSHunter.java` – gestion des commandes et du déroulement de la partie.
- `SpeedRunnerVSHunterEvenement.java` – écoute les événements (morts, clics, dégâts…).
- `Task.java` – tâche récurrente utilisée pour le mode Assassins.
- `plugin.yml` – déclaration Spigot du plugin et des commandes.
- `config.yml` et `lang.yml` dans `src/main/resources/` pour la configuration et les messages.

## Utilisation rapide

1. Copiez `SpeedRunnerVSHunter-1.6-shaded.jar` dans le dossier `plugins/` de votre serveur Spigot 1.16+.
2. Démarrez le serveur pour générer les fichiers `config.yml` et `lang.yml`.
3. Ajustez la configuration selon vos besoins puis rechargez ou redémarrez le serveur.
4. Exécutez `/svh` pour choisir votre camp et `/svh start` pour lancer la chasse.

## Licence et contributions

Aucune licence n’est spécifiée dans ce dépôt. Les demandes de contributions se font via des *pull requests* GitHub.

