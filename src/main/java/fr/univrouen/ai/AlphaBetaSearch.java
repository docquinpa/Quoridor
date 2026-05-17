package fr.univrouen.ai;

import fr.univrouen.model.StdBoard;

import java.util.ArrayList;
import java.util.List;

/**
 * Implémente l'algorithme de recherche avec élagage Alpha-Bêta couplé à une table de transpositions.
 * 
 * Cet algorithme explore l'arbre de jeu jusqu'à une profondeur déterminée en éliminant les branches
 * inutiles qui ne changent pas le résultat optimal.
 */
public class AlphaBetaSearch {
    private final MoveGenerator generator = new MoveGenerator();
    private final List<String> playerOrder;
    private final long zobristSeed;

    /**
     * Construit un nouveau chercheur Alpha-Bêta avec une graine par défaut pour Zobrist.
     * 
     * @param playerOrder Liste ordonnée des identifiants des joueurs.
     */
    public AlphaBetaSearch(List<String> playerOrder) {
        this(playerOrder, 0x5A17B3C92L);
    }

    /**
     * Construit un chercheur Alpha-Bêta avec une graine spécifique.
     * 
     * @param playerOrder Liste ordonnée des identifiants des joueurs.
     * @param zobristSeed Graine de hachage Zobrist.
     */
    public AlphaBetaSearch(List<String> playerOrder, long zobristSeed) {
        this.playerOrder = new ArrayList<>(playerOrder);
        this.zobristSeed = zobristSeed;
    }

    /**
     * Lance la recherche du meilleur coup à partir de la racine pour le joueur courant.
     * 
     * @param board Le plateau de jeu actuel.
     * @param currentPlayerId Le joueur dont c'est le tour.
     * @param depth La profondeur de recherche autorisée.
     * @return Un objet SearchResult contenant le meilleur coup identifié et son score associé.
     */
    public SearchResult search(StdBoard board, String currentPlayerId, int depth) {
        if (board == null || currentPlayerId == null) return new SearchResult(null, 0);
        ZobristHasher hasher = new ZobristHasher(board.getSize(), playerOrder, zobristSeed);
        TranspositionTable table = new TranspositionTable();
        return alphaBetaRoot(board, currentPlayerId, depth, currentPlayerId, hasher, table);
    }

    /**
     * Évalue le nœud racine. Cette fonction isole le premier niveau pour pouvoir renvoyer le Move choisi.
     */
    private SearchResult alphaBetaRoot(StdBoard board, String currentPlayerId, int depth, String rootPlayerId,
                                       ZobristHasher hasher, TranspositionTable table) {
        // 1. Génération des coups candidats légaux
        List<Move> moves = generator.generateMoves(board, currentPlayerId);
        if (moves.isEmpty()) {
            String opponentId = PlayerOrder.opponentOf(playerOrder, rootPlayerId);
            return new SearchResult(null, Evaluation.evaluate(board, rootPlayerId, opponentId));
        }

        int bestScore = Integer.MIN_VALUE;
        Move bestMove = null;
        int alpha = Integer.MIN_VALUE / 4;
        int beta = Integer.MAX_VALUE / 4;

        // 2. Évaluation de chaque branche fille candidate
        for (Move move : moves) {
            StdBoard next = board.copy();
            SearchUtils.applyMove(next, move);
            String nextPlayer = PlayerOrder.nextPlayer(playerOrder, currentPlayerId);
            
            // Appel récursif de recherche Alpha-Beta
            int score = alphaBeta(next, nextPlayer, depth - 1, alpha, beta, rootPlayerId, hasher, table);
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
            alpha = Math.max(alpha, score);
            if (alpha >= beta) break; // Coupure Alpha-Beta racine (théorique)
        }

        return new SearchResult(bestMove, bestScore);
    }

    /**
     * Fonction récursive d'exploration Alpha-Bêta avec intégration de la table de transposition.
     * 
     * @param board Le plateau simulé.
     * @param currentPlayerId Le joueur devant jouer à ce niveau de l'arbre.
     * @param depth Profondeur restante à explorer.
     * @param alpha Borne inférieure (meilleur score garanti de Max).
     * @param beta Borne supérieure (meilleur score espéré de Min).
     * @param rootPlayerId Le joueur Max de départ (pour lequel on calcule l'évaluation).
     * @param hasher L'objet de hachage Zobrist.
     * @param table La table de transposition.
     * @return Le score de la position du point de vue de rootPlayerId.
     */
    private int alphaBeta(StdBoard board, String currentPlayerId, int depth, int alpha, int beta,
                          String rootPlayerId, ZobristHasher hasher, TranspositionTable table) {
        // A. Vérification des conditions terminales directes
        String winner = board.checkWinCondition();
        if (winner != null) {
            return winner.equals(rootPlayerId) ? 100000 : -100000;
        }
        if (depth <= 0) {
            String opponentId = PlayerOrder.opponentOf(playerOrder, rootPlayerId);
            return Evaluation.evaluate(board, rootPlayerId, opponentId);
        }

        int alphaOrig = alpha;
        int betaOrig = beta;
        
        // B. Interrogation de la Table de Transpositions
        long key = hasher.hash(board, currentPlayerId);
        TranspositionTable.Entry entry = table.get(key);
        if (entry != null && entry.getDepth() >= depth) {
            // Si la position a déjà été résolue à une profondeur supérieure ou égale :
            if (entry.getBound() == TranspositionTable.Bound.EXACT) return entry.getScore();
            if (entry.getBound() == TranspositionTable.Bound.LOWER) alpha = Math.max(alpha, entry.getScore());
            if (entry.getBound() == TranspositionTable.Bound.UPPER) beta = Math.min(beta, entry.getScore());
            if (alpha >= beta) return entry.getScore(); // Coupure immédiate du cache
        }

        // C. Détermination du type de joueur actif (Maximisant ou Minimisant)
        boolean maximizing = currentPlayerId.equals(rootPlayerId);
        int bestScore = maximizing ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        Move bestMove = null;

        List<Move> moves = generator.generateMoves(board, currentPlayerId);
        if (moves.isEmpty()) {
            String opponentId = PlayerOrder.opponentOf(playerOrder, rootPlayerId);
            return Evaluation.evaluate(board, rootPlayerId, opponentId);
        }

        // D. Exploration récursive des enfants
        for (Move move : moves) {
            StdBoard next = board.copy();
            SearchUtils.applyMove(next, move);
            String nextPlayer = PlayerOrder.nextPlayer(playerOrder, currentPlayerId);
            int score = alphaBeta(next, nextPlayer, depth - 1, alpha, beta, rootPlayerId, hasher, table);

            if (maximizing) {
                // Phase MAX
                if (score > bestScore) {
                    bestScore = score;
                    bestMove = move;
                }
                alpha = Math.max(alpha, score);
                if (alpha >= beta) break; // Coupure Bêta : Min a une meilleure option ailleurs, inutile d'explorer
            } else {
                // Phase MIN
                if (score < bestScore) {
                    bestScore = score;
                    bestMove = move;
                }
                beta = Math.min(beta, score);
                if (alpha >= beta) break; // Coupure Alpha : Max a une meilleure option ailleurs, inutile d'explorer
            }
        }

        // E. Enregistrement des résultats et de la borne dans la Table de Transpositions
        TranspositionTable.Bound bound;
        if (bestScore <= alphaOrig) {
            bound = TranspositionTable.Bound.UPPER;
        } else if (bestScore >= betaOrig) {
            bound = TranspositionTable.Bound.LOWER;
        } else {
            bound = TranspositionTable.Bound.EXACT;
        }
        table.store(key, depth, bestScore, bound, bestMove);

        return bestScore;
    }
}

