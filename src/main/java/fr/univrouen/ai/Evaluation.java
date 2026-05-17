package fr.univrouen.ai;

import fr.univrouen.model.Board;
import fr.univrouen.model.Pawn;
import fr.univrouen.model.Pathfinding;

/**
 * Classe utilitaire gérant l'évaluation statique d'une position sur le plateau de Quoridor.
 * 
 * L'évaluation repose sur un équilibre entre la progression géométrique vers la ligne
 * de victoire (calculée par BFS) et la préservation de la réserve de barrières de chaque joueur.
 */
public final class Evaluation {
    /** Score extrême attribué lors d'une victoire absolue (distance au but = 0) */
    private static final int WIN_SCORE = 100000;

    private Evaluation() {
        // Constructeur privé pour empêcher l'instanciation de cette classe utilitaire.
    }

    /**
     * Évalue statiquement l'état actuel du plateau du point de vue d'un joueur.
     * 
     * Formule appliquée : Score = (DistOpp - DistMe) * 10 + (WallsMe - WallsOpp) * 2
     * 
     * @param board Le plateau de jeu actuel.
     * @param playerId L'identifiant du joueur actif (Max).
     * @param opponentId L'identifiant de son adversaire direct (Min).
     * @return Le score d'évaluation (entier positif si favorable à playerId, négatif sinon).
     */
    public static int evaluate(Board board, String playerId, String opponentId) {
        if (board == null || playerId == null || opponentId == null) return 0;

        Pawn me = board.getPawn(playerId);
        Pawn opp = board.getPawn(opponentId);
        if (me == null || opp == null) return 0;

        // 1. Calcul des plus courts chemins vers la ligne d'arrivée respective via BFS
        int myDist = Pathfinding.shortestPathLength(board, me);
        int oppDist = Pathfinding.shortestPathLength(board, opp);

        // 2. Gestion des cas terminaux (victoire / défaite)
        if (myDist == 0) return WIN_SCORE;
        if (oppDist == 0) return -WIN_SCORE;

        // 3. Récupération du nombre de barrières restantes en réserve
        int myWalls = board.getWallsRemaining(playerId);
        int oppWalls = board.getWallsRemaining(opponentId);

        // 4. Pondération des critères :
        // - La progression géométrique est prépondérante (facteur 10).
        // - Le différentiel de barrières est secondaire (facteur 2), incitant à l'économie de murs.
        int distScore = (oppDist - myDist) * 10;
        int wallScore = (myWalls - oppWalls) * 2;

        return distScore + wallScore;
    }
}

