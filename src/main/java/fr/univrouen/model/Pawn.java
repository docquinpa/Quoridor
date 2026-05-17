package fr.univrouen.model;

/**
 * Interface représentant un pawn (pion) dans le jeu Quoridor.
 * Un pawn possède une position sur le plateau et un objectif pour gagner.
 */
public interface Pawn {
    
    /**
     * Interface pour définir l'objectif de victoire d'un pawn.
     */
    interface Goal {
        /**
         * Vérifie si l'objectif a été atteint aux coordonnées données.
         * @param x la coordonnée X
         * @param z la coordonnée Z
         * @return true si les coordonnées correspondent à l'objectif, false sinon
         */
        boolean reached(int x, int z);
    }

    /**
     * Retourne l'identifiant unique du pawn.
     * @return l'ID du pawn
     * @postcondition le résultat ne change jamais au cours de la vie du pawn
     */
    String getId();
    
    /**
     * Retourne la coordonnée X actuelle du pawn.
     * @return la position X (0 <= x < taille du plateau)
     */
    int getX();
    
    /**
     * Retourne la coordonnée Z actuelle du pawn.
     * @return la position Z (0 <= z < taille du plateau)
     */
    int getZ();
    
    /**
     * Change la position du pawn sur le plateau.
     * @param x la nouvelle coordonnée X
     * @param z la nouvelle coordonnée Z
     * @precondition 0 <= x,z < taille du plateau
     * @postcondition getX() == x et getZ() == z
     */
    void setPos(int x, int z);
    
    /**
     * Vérifie si le pawn a atteint son objectif.
     * @return true si l'objectif actuel est atteint, false sinon
     * @postcondition aucun changement d'état
     */
    boolean hasReachedGoal();
    
    /**
     * Vérifie si l'objectif est atteint aux coordonnées données.
     * @param tx la coordonnée X à tester
     * @param tz la coordonnée Z à tester
     * @return true si les coordonnées satisfont l'objectif, false sinon
     * @postcondition aucun changement d'état
     */
    boolean goalReachedAt(int tx, int tz);

    /**
     * Retourne l'objectif de victoire de ce pawn.
     * @return l'objectif associé
     * @postcondition aucun changement d'état
     */
    Goal getGoal();
}
