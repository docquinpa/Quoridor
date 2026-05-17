package fr.univrouen.ai;

import fr.univrouen.model.StdBoard;

import java.util.ArrayList;
import java.util.List;

/**
 * Implémente la recherche NegaMax avec élagage Alpha-Bêta et cache de transpositions.
 * 
 * Cette variante compacte de l'Alpha-Bêta s'appuie sur la relation symétrique des jeux
 * à somme nulle : max(a, b) = -min(-a, -b). Cela unifie la récursion sous une seule condition.
 */
public class NegAlphaBetaSearch {
    private final MoveGenerator generator = new MoveGenerator();
    private final List<String> playerOrder;
    private final long zobristSeed;

    /**
     * Construit un chercheur NegAlphaBeta avec graine par défaut.
     */
    public NegAlphaBetaSearch(List<String> playerOrder) {
        this(playerOrder, 0x7B9E2F11AL);
    }

    /**
     * Construit un chercheur NegAlphaBeta avec graine spécifique.
     */
    public NegAlphaBetaSearch(List<String> playerOrder, long zobristSeed) {
        this.playerOrder = new ArrayList<>(playerOrder);
        this.zobristSeed = zobristSeed;
    }

    /**
     * Exécute la recherche NegaMax initiale à partir du plateau racine.
     * 
     * @param board Le plateau de jeu de départ.
     * @param currentPlayerId Le joueur actif.
     * @param depth Profondeur maximale.
     * @return Le meilleur coup trouvé et sa valeur d'évaluation relative.
     */
    public SearchResult search(StdBoard board, String currentPlayerId, int depth) {
        if (board == null || currentPlayerId == null) return new SearchResult(null, 0);
        ZobristHasher hasher = new ZobristHasher(board.getSize(), playerOrder, zobristSeed);
        TranspositionTable table = new TranspositionTable();
        List<Move> moves = generator.generateMoves(board, currentPlayerId);
        if (moves.isEmpty()) return new SearchResult(null, 0);

        Move bestMove = null;
        int alpha = Integer.MIN_VALUE / 4;
        int beta = Integer.MAX_VALUE / 4;
        int bestScore = Integer.MIN_VALUE;
        int color = 1; // 1 représente le point de vue du joueur racine (Max)

        // Parcours du premier niveau de l'arbre
        for (Move move : moves) {
            StdBoard next = board.copy();
            SearchUtils.applyMove(next, move);
            String nextPlayer = PlayerOrder.nextPlayer(playerOrder, currentPlayerId);
            
            // On inverse les bornes et on négative le score retourné du successeur (-color)
            int score = -negamax(next, nextPlayer, depth - 1, -beta, -alpha, currentPlayerId, -color, hasher, table);
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
            alpha = Math.max(alpha, score);
            if (alpha >= beta) break; // Coupure Alpha-Beta racine
        }

        return new SearchResult(bestMove, bestScore);
    }

    /**
     * Fonction récursive unifiée Negamax avec gestion des transpositions.
     * 
     * @param board Le plateau courant.
     * @param currentPlayerId Le joueur actif à ce niveau de l'arbre.
     * @param depth Profondeur restante.
     * @param alpha Borne inférieure.
     * @param beta Borne supérieure.
     * @param rootPlayerId Joueur Max racine d'origine.
     * @param color Variable d'inversion (+1 ou -1) pour alterner la perspective de score.
     * @param hasher Hash de Zobrist.
     * @param table Table de transposition.
     * @return Le score d'évaluation récursif.
     */
    private int negamax(StdBoard board, String currentPlayerId, int depth, int alpha, int beta,
                        String rootPlayerId, int color, ZobristHasher hasher, TranspositionTable table) {
        // 1. Conditions terminales
        String winner = board.checkWinCondition();
        if (winner != null) {
            int raw = winner.equals(rootPlayerId) ? 100000 : -100000;
            return color * raw;
        }
        if (depth <= 0) {
            String opponentId = PlayerOrder.opponentOf(playerOrder, rootPlayerId);
            int eval = Evaluation.evaluate(board, rootPlayerId, opponentId);
            return color * eval; // Multiplié par color pour refléter le point de vue du joueur actif
        }

        int alphaOrig = alpha;
        int betaOrig = beta;
        
        // 2. Interrogation de la Table de Transpositions
        long key = hasher.hash(board, currentPlayerId);
        TranspositionTable.Entry entry = table.get(key);
        if (entry != null && entry.getDepth() >= depth) {
            if (entry.getBound() == TranspositionTable.Bound.EXACT) return entry.getScore();
            if (entry.getBound() == TranspositionTable.Bound.LOWER) alpha = Math.max(alpha, entry.getScore());
            if (entry.getBound() == TranspositionTable.Bound.UPPER) beta = Math.min(beta, entry.getScore());
            if (alpha >= beta) return entry.getScore();
        }

        int bestScore = Integer.MIN_VALUE;
        List<Move> moves = generator.generateMoves(board, currentPlayerId);
        if (moves.isEmpty()) {
            String opponentId = PlayerOrder.opponentOf(playerOrder, rootPlayerId);
            int eval = Evaluation.evaluate(board, rootPlayerId, opponentId);
            return color * eval;
        }

        // 3. Boucle récursive compacte (pas de distinction MAX/MIN)
        for (Move move : moves) {
            StdBoard next = board.copy();
            SearchUtils.applyMove(next, move);
            String nextPlayer = PlayerOrder.nextPlayer(playerOrder, currentPlayerId);
            
            // Calcul par récurrence : NegaMax(s) = Max( -NegaMax(s') )
            int score = -negamax(next, nextPlayer, depth - 1, -beta, -alpha, rootPlayerId, -color, hasher, table);
            if (score > bestScore) bestScore = score;
            alpha = Math.max(alpha, score);
            if (alpha >= beta) break; // Coupure Alpha-Beta
        }

        // 4. Enregistrement en cache
        TranspositionTable.Bound bound;
        if (bestScore <= alphaOrig) {
            bound = TranspositionTable.Bound.UPPER;
        } else if (bestScore >= betaOrig) {
            bound = TranspositionTable.Bound.LOWER;
        } else {
            bound = TranspositionTable.Bound.EXACT;
        }
        table.store(key, depth, bestScore, bound, null);

        return bestScore;
    }
}

