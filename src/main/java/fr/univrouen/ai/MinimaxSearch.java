package fr.univrouen.ai;

import fr.univrouen.model.StdBoard;

import java.util.ArrayList;
import java.util.List;

/**
 * Algorithme de recherche Minimax standard pour le Quoridor.
 * 
 * Implémente l'exploration exhaustive de l'arbre de jeu jusqu'à une profondeur maximale définie.
 * Utilise une évaluation heuristique pour attribuer une valeur aux feuilles de l'arbre.
 */
public class MinimaxSearch {
    private final MoveGenerator generator = new MoveGenerator();
    private final List<String> playerOrder;

    /**
     * Initialise l'algorithme Minimax avec l'ordre de passage des joueurs.
     * 
     * @param playerOrder Liste ordonnée des identifiants des joueurs de la partie.
     */
    public MinimaxSearch(List<String> playerOrder) {
        this.playerOrder = new ArrayList<>(playerOrder);
    }

    /**
     * Exécute l'exploration Minimax à partir de l'état racine actuel.
     * 
     * @param board Le plateau de jeu de départ.
     * @param currentPlayerId Le joueur dont c'est le tour de décider.
     * @param depth La profondeur maximale de recherche.
     * @return SearchResult contenant le coup recommandé et sa valeur heuristique associée.
     */
    public SearchResult search(StdBoard board, String currentPlayerId, int depth) {
        if (board == null || currentPlayerId == null) return new SearchResult(null, 0);
        return minimax(board, currentPlayerId, depth, currentPlayerId);
    }

    /**
     * Méthode récursive Minimax alternant entre les phases Maximisantes et Minimisantes.
     * 
     * @param board Le plateau à évaluer de manière hypothétique à ce niveau.
     * @param currentPlayerId Le joueur dont c'est le tour de jouer à ce niveau de l'arbre.
     * @param depth La profondeur restante.
     * @param rootPlayerId Le joueur Max d'origine pour qui on évalue la position.
     * @return Un SearchResult contenant le meilleur score propagé.
     */
    private SearchResult minimax(StdBoard board, String currentPlayerId, int depth, String rootPlayerId) {
        // 1. Détection des conditions d'arrêt directes (Victoire / Défaite)
        String winner = board.checkWinCondition();
        if (winner != null) {
            int score = winner.equals(rootPlayerId) ? 100000 : -100000;
            return new SearchResult(null, score);
        }
        
        // 2. Détection de la limite de profondeur
        if (depth <= 0) {
            String opponentId = PlayerOrder.opponentOf(playerOrder, rootPlayerId);
            return new SearchResult(null, Evaluation.evaluate(board, rootPlayerId, opponentId));
        }

        // 3. Génération de l'intégralité des coups légaux possibles
        List<Move> moves = generator.generateMoves(board, currentPlayerId);
        if (moves.isEmpty()) {
            String opponentId = PlayerOrder.opponentOf(playerOrder, rootPlayerId);
            return new SearchResult(null, Evaluation.evaluate(board, rootPlayerId, opponentId));
        }

        // 4. Détermination de la nature du joueur courant (MAX vs MIN)
        boolean maximizing = currentPlayerId.equals(rootPlayerId);
        int bestScore = maximizing ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        Move bestMove = null;

        // 5. Exploration exhaustive (sans aucun élagage)
        for (Move move : moves) {
            StdBoard next = board.copy();
            SearchUtils.applyMove(next, move);
            String nextPlayer = PlayerOrder.nextPlayer(playerOrder, currentPlayerId);
            
            // Appel récursif au niveau inférieur
            int score = minimax(next, nextPlayer, depth - 1, rootPlayerId).score();

            if (maximizing && score > bestScore) {
                // Maximise le gain pour le joueur racine
                bestScore = score;
                bestMove = move;
            } else if (!maximizing && score < bestScore) {
                // Minimise la perte (anticipe le meilleur jeu adverse)
                bestScore = score;
                bestMove = move;
            }
        }

        return new SearchResult(bestMove, bestScore);
    }
}

