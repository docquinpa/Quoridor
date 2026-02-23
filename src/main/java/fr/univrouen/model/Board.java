package fr.univrouen.model;

import java.util.List;
import java.util.Map;

/**
 * Interface représentant le plateau de jeu Quoridor.
 * Gère les pawns, les murs, et la détection des conditions de victoire.
 */
public interface Board {
    
    /**
     * Retourne la taille du plateau.
     * @return la taille du plateau (ex: 9 pour un plateau 9x9)
     */
    int getSize();
    
    /**
     * Ajoute un pawn au plateau.
     * @param pawn le pawn à ajouter
     * @precondition pawn != null et les coordonnées du pawn sont valides (0 <= x,z < size)
     * @postcondition le pawn est ajouté et peut être récupéré via getPawn(id)
     * @throws IllegalArgumentException si les coordonnées sont hors limites
     */
    void addPawn(Pawn pawn);
    
    /**
     * Récupère un pawn par son identifiant.
     * @param id l'identifiant du pawn
     * @return le pawn correspondant, ou null s'il n'existe pas
     */
    Pawn getPawn(String id);
    
    /**
     * Vérifie si une case est occupée par un pawn.
     * @param x la coordonnée X
     * @param z la coordonnée Z
     * @return true si une case contient un pawn, false sinon
     * @precondition 0 <= x,z < size
     */
    boolean isOccupied(int x, int z);
    
    /**
     * Vérifie si un mouvement est possible pour un pawn.
     * Prend en compte les murs et les autres pawns.
     * @param pawnId l'identifiant du pawn
     * @param toX la coordonnée X de destination
     * @param toZ la coordonnée Z de destination
     * @return true si le mouvement est valide, false sinon
     * @precondition pawnId correspond à un pawn existant et 0 <= toX,toZ < size
     * @postcondition aucun changement d'état du plateau
     */
    boolean canMove(String pawnId, int toX, int toZ);
    
    /**
     * Effectue un mouvement de pawn.
     * @param pawnId l'identifiant du pawn
     * @param toX la coordonnée X de destination
     * @param toZ la coordonnée Z de destination
     * @precondition canMove(pawnId, toX, toZ) == true
     * @postcondition le pawn est déplacé aux coordonnées (toX, toZ)
     * @throws IllegalArgumentException si le mouvement n'est pas valide
     */
    void movePawn(String pawnId, int toX, int toZ);
    
    /**
     * Vérifie les conditions de victoire du jeu.
     * @return l'identifiant du pawn gagnant, ou null s'il n'y a pas de gagnant
     * @postcondition aucun changement d'état du plateau
     */
    String checkWinCondition();
    
    /**
     * Réinitialise le plateau à son état initial.
     * Vide tous les murs et réinitialise les murs disponibles pour chaque joueur.
     * @postcondition tous les murs sont supprimés, chaque joueur dispose de 10 murs
     */
    void reset();
    
    /**
     * Vérifie si une action (mouvement ou placement de mur) a été effectuée ce tour.
     * @return true si une action a été prise, false sinon
     */
    boolean hasActionThisTurn();
    
    /**
     * Marque qu'une action a été effectuée ce tour.
     * @postcondition hasActionThisTurn() retourne true
     */
    void markActionTaken();
    
    /**
     * Réinitialise le drapeau d'action pour un nouveau tour.
     * @postcondition hasActionThisTurn() retourne false
     */
    void resetActionFlag();
    
    /**
     * Place un mur pour un joueur.
     * @param playerId l'identifiant du joueur
     * @param x la coordonnée X du mur
     * @param z la coordonnée Z du mur
     * @param orientation l'orientation du mur (HORIZONTAL ou VERTICAL)
     * @return true si le mur a été placé avec succès, false sinon
     * @precondition playerId correspond à un joueur existant et il dispose de murs disponibles
     * @postcondition si true: le mur est placé et les murs restants sont décrémentés
     */
    boolean placeWallForPlayer(String playerId, int x, int z, Orientation orientation);
    
    /**
     * Retourne le nombre de murs restants pour un joueur.
     * @param playerId l'identifiant du joueur
     * @return le nombre de murs disponibles (>= 0)
     */
    int getWallsRemaining(String playerId);
    
    /**
     * Retourne une copie de la map des murs restants pour tous les joueurs.
     * @return une map [playerId -> nombre de murs]
     * @postcondition aucun changement d'état du plateau
     */
    Map<String, Integer> getWallsRemainingMap();
}
