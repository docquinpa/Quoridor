package fr.univrouen.ai;

import fr.univrouen.model.Board;
import fr.univrouen.model.Pawn;
import fr.univrouen.model.Pathfinding;

import java.util.HashMap;
import java.util.Map;

/**
 * Gère le calcul optimisé (incrémental) de l'évaluation en évitant d'exécuter
 * des parcours BFS inutiles.
 * 
 * Lors d'un déplacement de pion simple, seul le chemin du joueur actif est affecté.
 * Lors d'une pose de barrière, l'ensemble des chemins des joueurs doit être réévalué.
 */
public final class IncrementalHeuristic {
    private IncrementalHeuristic() {
        // Constructeur privé pour empêcher l'instanciation de cette classe utilitaire.
    }

    /**
     * Initialise l'état heuristique complet à partir d'un plateau donné.
     * 
     * @param board Le plateau de départ.
     * @return Un nouvel objet HeuristicState contenant les distances initiales de chaque pion.
     */
    public static HeuristicState from(Board board) {
        Map<String, Integer> distances = new HashMap<>();
        if (board != null) {
            for (Pawn pawn : board.getPawns().values()) {
                distances.put(pawn.getId(), Pathfinding.shortestPathLength(board, pawn));
            }
        }
        Map<String, Integer> walls = board == null ? new HashMap<>() : board.getWallsRemainingMap();
        return new HeuristicState(distances, walls);
    }

    /**
     * Représente les valeurs d'évaluation pré-calculées d'une position donnée (distances BFS et barrières restants).
     */
    public static final class HeuristicState {
        private final Map<String, Integer> distances;
        private final Map<String, Integer> wallsRemaining;

        private HeuristicState(Map<String, Integer> distances, Map<String, Integer> wallsRemaining) {
            this.distances = distances;
            this.wallsRemaining = wallsRemaining;
        }

        /**
         * Calcule le nouvel état après application d'un coup.
         * 
         * - Si le coup est un déplacement de pion, seul le BFS du pion déplacé est mis à jour.
         * - Si le coup est une pose de barrière, tous les BFS sont recalculés.
         * 
         * @param board Le nouveau plateau après l'application du coup.
         * @param move Le coup appliqué.
         * @return Un nouvel état heuristique mis à jour de façon optimale.
         */
        public HeuristicState updateAfter(Board board, Move move) {
            Map<String, Integer> nextDistances = new HashMap<>(distances);
            Map<String, Integer> nextWalls = board == null ? new HashMap<>() : board.getWallsRemainingMap();

            if (board != null) {
                if (move != null && move.isPawnMove()) {
                    // Optimisation incrémentale : seul le joueur actif s'est déplacé,
                    // donc seule sa distance a changé. La distance de l'adversaire reste identique.
                    Pawn pawn = board.getPawn(move.getPlayerId());
                    if (pawn != null) {
                        nextDistances.put(pawn.getId(), Pathfinding.shortestPathLength(board, pawn));
                    }
                } else {
                    // Pose de barrière : le nouvel obstacle peut altérer les chemins de tous les joueurs.
                    // On recalcule obligatoirement toutes les distances.
                    for (Pawn pawn : board.getPawns().values()) {
                        nextDistances.put(pawn.getId(), Pathfinding.shortestPathLength(board, pawn));
                    }
                }
            }

            return new HeuristicState(nextDistances, nextWalls);
        }

        /**
         * Évalue la position actuelle à partir des données mises en cache (distances BFS).
         * 
         * @param playerId L'identifiant du joueur actif (Max).
         * @param opponentId L'identifiant de son adversaire direct (Min).
         * @return Le score d'évaluation calculé.
         */
        public int evaluate(String playerId, String opponentId) {
            if (playerId == null || opponentId == null) return 0;

            int myDist = distances.getOrDefault(playerId, Integer.MAX_VALUE);
            int oppDist = distances.getOrDefault(opponentId, Integer.MAX_VALUE);
            int myWalls = wallsRemaining.getOrDefault(playerId, 0);
            int oppWalls = wallsRemaining.getOrDefault(opponentId, 0);

            if (myDist == 0) return 100000;
            if (oppDist == 0) return -100000;

            int distScore = (oppDist - myDist) * 10;
            int wallScore = (myWalls - oppWalls) * 2;
            return distScore + wallScore;
        }
    }
}

