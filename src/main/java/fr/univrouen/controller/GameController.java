package fr.univrouen.controller;

import fr.univrouen.model.Board;
import fr.univrouen.model.Orientation;
import fr.univrouen.model.Pawn;

import java.util.List;

/**
 * Interface du contrôleur de jeu pour Quoridor.
 * Gère les joueurs, les tours, les mouvements et le placement de murs.
 */
public interface GameController {
    
    /**
     * Interface pour écouter les événements du jeu.
     */
    interface GameListener {
        /**
         * Appelé quand le tour change.
         * @param currentPlayerId l'identifiant du joueur actuel (peut être null si pas de joueur)
         */
        void onTurnChanged(String currentPlayerId);
        
        /**
         * Appelé quand un pawn se déplace.
         * @param pawnId l'identifiant du pawn
         * @param x la nouvelle coordonnée X
         * @param z la nouvelle coordonnée Z
         */
        void onPawnMoved(String pawnId, int x, int z);
        
        /**
         * Appelé quand un mur est placé.
         * @param x la coordonnée X du mur
         * @param z la coordonnée Z du mur
         * @param orientation l'orientation du mur (HORIZONTAL ou VERTICAL)
         */
        void onWallPlaced(int x, int z, Orientation orientation);
        
        /**
         * Appelé quand un nouveau joueur est enregistré.
         * @param playerId l'identifiant du joueur
         */
        void onPlayerRegistered(String playerId);
        
        /**
         * Appelé quand un joueur a gagné la partie.
         * @param winnerPlayerId l'identifiant du gagnant
         */
        void onGameWon(String winnerPlayerId);
    }

    /**
     * Retourne le modèle de plateau associé à ce contrôleur.
     * @return l'interface Board
     */
    Board getModel();
    
    /**
     * Ajoute un écouteur d'événements du jeu.
     * @param l l'écouteur à ajouter
     * @precondition l != null
     * @postcondition l sera notifié des événements futurs
     */
    void addListener(GameListener l);
    
    /**
     * Supprime un écouteur d'événements du jeu.
     * @param l l'écouteur à supprimer
     * @precondition l a été ajouté via addListener()
     * @postcondition l ne sera plus notifié des événements
     */
    void removeListener(GameListener l);
    
    /**
     * Enregistre un nouveau joueur dans la partie.
     * @param p le pawn du joueur à enregistrer
     * @precondition p != null et p.getId() est unique
     * @postcondition le joueur est ajouté à l'ordre des joueurs et ses écouteurs sont notifiés
     */
    void registerPlayer(Pawn p);
    
    /**
     * Effectue un mouvement d'un pawn.
     * @param pawnId l'identifiant du pawn
     * @param toX la coordonnée X de destination
     * @param toZ la coordonnée Z de destination
     * @return true si le mouvement a réussi, false sinon
     * @precondition c'est le tour du joueur propriétaire du pawn et aucune action n'a eu lieu ce tour
     * @postcondition si true: le pawn est déplacé, le tour avance, la victoire est vérifiée
     */
    boolean movePawn(String pawnId, int toX, int toZ);
    
    /**
     * Place un mur pour un joueur.
     * @param playerId l'identifiant du joueur
     * @param x la coordonnée X du mur
     * @param z la coordonnée Z du mur
     * @param orientation l'orientation du mur (HORIZONTAL ou VERTICAL)
     * @return true si le mur a été placé avec succès, false sinon
     * @precondition c'est le tour du joueur et aucune action n'a eu lieu ce tour
     * @postcondition si true: le mur est placé, le tour avance, les écouteurs sont notifiés
     */
    boolean placeWall(String playerId, int x, int z, Orientation orientation);
    
    /**
     * Retourne l'identifiant du joueur actuel.
     * @return l'ID du joueur actuel, ou null s'il n'y a pas de joueur
     */
    String getCurrentPlayerId();
    
    /**
     * Retourne l'ordre des joueurs dans la partie.
     * @return une liste immuable des identifiants des joueurs
     * @postcondition l'ordre ne change pas dans cette liste (mais peut changer d'un appel à l'autre)
     */
    List<String> getPlayerOrder();
    
    /**
     * Retourne le nombre de murs restants pour un joueur.
     * @param playerId l'identifiant du joueur
     * @return le nombre de murs disponibles (>= 0)
     * @precondition playerId correspond à un joueur enregistré
     */
    int getWallsRemaining(String playerId);
    
    /**
     * Réinitialise la partie.
     * Réinitialise le plateau et le tour initial.
     * @postcondition le jeu est dans un état initial avec les joueurs enregistrés
     */
    void resetGame();
}
