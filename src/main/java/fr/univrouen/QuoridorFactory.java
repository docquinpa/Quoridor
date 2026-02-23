package fr.univrouen;

import fr.univrouen.controller.GameController;
import fr.univrouen.controller.StdGameController;
import fr.univrouen.model.Board;
import fr.univrouen.model.Pawn;
import fr.univrouen.model.StdBoard;
import fr.univrouen.model.StdPawn;

/**
 * Fabrique pour créer les instances des implémentations standards.
 * Respecte le principe SOLID d'Inversion des Dépendances.
 * Les interfaces sont manipulées partout, seule la Factory crée les implémentations.
 */
public class QuoridorFactory {
    
    /**
     * Crée une nouvelle instance de plateau vide.
     * @return une interface Board (implémentation StdBoard)
     */
    public static Board createBoard() {
        return new StdBoard();
    }
    
    /**
     * Crée une nouvelle instance de plateau avec une taille spécifiée.
     * @param size la taille du plateau
     * @return une interface Board (implémentation StdBoard)
     */
    public static Board createBoard(int size) {
        return new StdBoard(size);
    }
    
    /**
     * Crée une nouvelle instance de pion.
     * @param id l'identifiant du pion
     * @param x la coordonnée X initiale
     * @param z la coordonnée Z initiale
     * @param goal l'objectif de victoire du pion
     * @return une interface Pawn (implémentation StdPawn)
     */
    public static Pawn createPawn(String id, int x, int z, Pawn.Goal goal) {
        return new StdPawn(id, x, z, goal);
    }
    
    /**
     * Crée une nouvelle instance de contrôleur de jeu avec un plateau fourni.
     * @param board le plateau du jeu
     * @return une interface GameController (implémentation StdGameController)
     */
    public static GameController createGameController(Board board) {
        return new StdGameController(board);
    }
    
    /**
     * Crée une nouvelle instance de contrôleur de jeu avec un plateau par défaut.
     * @return une interface GameController (implémentation StdGameController)
     */
    public static GameController createGameController() {
        return new StdGameController(createBoard());
    }
}
