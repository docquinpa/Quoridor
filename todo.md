# 🧩 TODO – Projet Quoridor (Java / JavaFX 3D)

Ce fichier contient toutes les tâches que Copilot Chat doit exécuter pour compléter et améliorer le projet Quoridor.  
Les tâches sont formulées pour être comprises et exécutées directement par Copilot.

---

## 🔍 1. Analyse du projet existant
- [ ] Analyser toute la structure du projet et générer une carte des classes.
- [ ] Identifier les classes liées au plateau, aux pions, aux barrières et à l’IA.
- [ ] Expliquer comment la scène JavaFX 3D est construite et comment elle s’intègre au moteur de jeu.
- [ ] Lister les fonctionnalités déjà implémentées et celles manquantes.

---

## 🖥️ 2. Menu principal (JavaFX)
- [ ] Créer une scène JavaFX dédiée au menu principal.
- [ ] Ajouter les boutons : Nouvelle Partie, Choisir la difficulté, Règles, Exemples de parties, Quitter.
- [ ] Implémenter la navigation entre le menu et la scène de jeu existante.
- [ ] Créer un `MenuController` et relier les actions des boutons.

---

## 🎚️ 3. Sélection de la difficulté
- [ ] Créer une fenêtre ou panneau permettant de choisir la difficulté (Facile / Moyen / Difficile).
- [ ] Créer une classe `GameSettings` pour stocker la difficulté.
- [ ] Modifier l’IA pour qu’elle lise la difficulté et ajuste la profondeur de recherche.
- [ ] Vérifier que la difficulté est appliquée au lancement d’une nouvelle partie.

---

## 🧠 4. IA et algorithmes de recherche
- [ ] Analyser l’IA existante et identifier les parties manquantes.
- [ ] Implémenter Minimax si absent.
- [ ] Implémenter Alpha-Beta.
- [ ] Implémenter NegAlphaBeta.
- [ ] Implémenter SSS* si demandé.
- [ ] Ajouter une fonction d’évaluation basée sur :
  - [ ] Distance BFS au but.
  - [ ] Différence de distances joueur/adversaire.
  - [ ] Nombre de barrières restantes.
- [ ] Implémenter une heuristique incrémentale.
- [ ] Ajouter une table de transpositions.
- [ ] Implémenter le hashing de Zobrist pour les états du plateau.

---

## 🧱 5. Gestion des barrières
- [ ] Vérifier la logique de placement des barrières.
- [ ] Empêcher qu’une barrière bloque totalement un joueur.
- [ ] Ajouter un BFS pour vérifier qu’un chemin existe toujours.
- [ ] Corriger les collisions entre barrières et déplacements si nécessaire.

---

## 🧍 6. Déplacements des pions
- [ ] Vérifier et corriger les déplacements simples (haut, bas, gauche, droite).
- [ ] Implémenter le saut par-dessus un pion.
- [ ] Implémenter la bifurcation droite/gauche si une barrière bloque derrière le pion sauté.
- [ ] Ajouter une animation JavaFX 3D pour les déplacements.

---

## 🎮 7. Améliorations de l’interface 3D
- [ ] Améliorer le rendu du plateau (textures, couleurs, lumière).
- [ ] Ajouter une animation pour la pose de barrières.
- [ ] Ajouter un panneau latéral affichant :
  - [ ] Joueur courant.
  - [ ] Barrières restantes.
  - [ ] Difficulté.
  - [ ] Boutons : Recommencer, Retour au menu.

---

## 📚 8. Règles du jeu & exemples de parties
- [ ] Créer une scène JavaFX affichant les règles du jeu.
- [ ] Ajouter une scène permettant de rejouer des parties d’exemple.
- [ ] Charger des parties pré-enregistrées (JSON ou autre format).
- [ ] Implémenter un système de replay coup par coup.

---

## 🧪 9. Tests
- [ ] Ajouter des tests unitaires pour :
  - [ ] BFS / pathfinding.
  - [ ] Placement des barrières.
  - [ ] Déplacements.
  - [ ] IA (profondeur, heuristique).
- [ ] Ajouter des tests d’intégration simulant une partie complète.

---

## 📄 10. Rapport final
- [ ] Générer une section expliquant les heuristiques d’évaluation.
- [ ] Décrire les structures de données utilisées.
- [ ] Décrire les algorithmes IA implémentés.
- [ ] Ajouter un manuel d’utilisation.
- [ ] Ajouter des captures d’écran du jeu.

---
