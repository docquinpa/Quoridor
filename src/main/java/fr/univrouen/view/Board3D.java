package fr.univrouen.view;

import fr.univrouen.model.Board;
import javafx.scene.paint.Color;

/**
 * Interface représentant la vue 3D du plateau Quoridor.
 * Gère l'affichage du plateau, des pawns et des murs dans l'environnement 3D JavaFX.
 * Implémentée par StdBoard3D.
 */
public interface Board3D {
    
    /**
     * Affiche un pawn à une position donnée avec une couleur spécifiée.
     * @param px la coordonnée X en pixels
     * @param pz la coordonnée Z en pixels
     * @param color la couleur du pawn
     * @precondition px et pz doivent correspondre à des coordonnées valides de la grille
     * @postcondition un pawn visuel est ajouté à la scène à la position donnée
     */
    void spawnPawn(double px, double pz, Color color);
    
    /**
     * Retourne le modèle de plateau associé à cette vue.
     * @return l'interface Board contenant l'état du jeu
     * @postcondition aucun changement d'état
     */
    Board getBoardModel();
    
    /**
     * Retourne l'identifiant du joueur actuel.
     * @return l'ID du joueur dont c'est le tour, ou null s'il n'y a pas de joueur actif
     * @postcondition aucun changement d'état
     */
    String getCurrentPlayerId();
    
    /**
     * Retourne l'ordre des joueurs dans la partie.
     * @return une liste immuable des identifiants des joueurs dans l'ordre de jeu
     * @postcondition aucun changement d'état
     */
    java.util.List<String> getPlayerOrder();
}