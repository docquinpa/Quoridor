package fr.univrouen.ai;

import fr.univrouen.model.StdBoard;

import java.util.ArrayList;
import java.util.List;

/**
 * Implémente l'algorithme de recherche SSS* émulé par la procédure MTD-f.
 * 
 * SSS* est un algorithme de recherche de type Best-First (meilleur d'abord).
 * Pour contourner la complexité mémoire exponentielle du SSS* classique (maintien
 * de la liste OPEN), cette implémentation utilise MTD-f (Memory-enhanced Test Driver).
 * MTD-f effectue des appels successifs à Alpha-Bêta avec des fenêtres de recherche nulles
 * (largeur nulle) de type [beta-1, beta] et converge rapidement vers la valeur minimax réelle
 * grâce à la persistance des données dans la table de transpositions.
 */
public class SssStarSearch {
    private final MoveGenerator generator = new MoveGenerator();
    private final List<String> playerOrder;
    private final long zobristSeed;

    /**
     * Construit un chercheur SSS* (via MTD-f) avec graine par défaut pour Zobrist.
     * 
     * @param playerOrder Liste ordonnée des identifiants des joueurs de la partie.
     */
    public SssStarSearch(List<String> playerOrder) {
        this(playerOrder, 0x3C8D17A5DL);
    }

    /**
     * Construit un chercheur SSS* (via MTD-f) avec graine de hachage spécifique.
     * 
     * @param playerOrder Liste ordonnée des identifiants des joueurs de la partie.
     * @param zobristSeed Graine de signature Zobrist.
     */
    public SssStarSearch(List<String> playerOrder, long zobristSeed) {
        this.playerOrder = new ArrayList<>(playerOrder);
        this.zobristSeed = zobristSeed;
    }

    /**
     * Lance la recherche Best-First SSS* (via MTD-f) à la racine de l'arbre.
     * 
     * @param board Le plateau de jeu actuel.
     * @param currentPlayerId Le joueur dont c'est le tour de décider.
     * @param depth Profondeur maximale.
     * @return SearchResult avec le meilleur coup et sa valeur d'évaluation.
     */
    public SearchResult search(StdBoard board, String currentPlayerId, int depth) {
        if (board == null || currentPlayerId == null) return new SearchResult(null, 0);
        ZobristHasher hasher = new ZobristHasher(board.getSize(), playerOrder, zobristSeed);
        TranspositionTable table = new TranspositionTable();

        List<Move> moves = generator.generateMoves(board, currentPlayerId);
        if (moves.isEmpty()) return new SearchResult(null, 0);

        Move bestMove = null;
        int bestScore = Integer.MIN_VALUE;

        // Exploration du premier niveau de l'arbre de jeu
        for (Move move : moves) {
            StdBoard next = board.copy();
            SearchUtils.applyMove(next, move);
            String nextPlayer = PlayerOrder.nextPlayer(playerOrder, currentPlayerId);
            
            // Appel de la routine de convergence MTD-f (le score retourné est inversé)
            int score = -mtdf(next, nextPlayer, depth - 1, currentPlayerId, -1, hasher, table);
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }

        return new SearchResult(bestMove, bestScore);
    }

    /**
     * Routine MTD-f (Memory-enhanced Test Driver) de convergence par fenêtres de recherche nulles.
     * 
     * Cette fonction fait osciller l'estimation 'g' et resserre les bornes 'lower' et 'upper'
     * jusqu'à ce qu'elles coïncident, révélant la valeur minimax exacte de la position.
     * 
     * @param board Le plateau actuel.
     * @param currentPlayerId Le joueur actif à ce niveau.
     * @param depth Profondeur restante.
     * @param rootPlayerId Le joueur Max racine.
     * @param color Variable d'alternance de signe (+1 ou -1).
     * @param hasher L'objet de hachage Zobrist.
     * @param table La table de transposition.
     * @return Le score exact évalué et convergé.
     */
    private int mtdf(StdBoard board, String currentPlayerId, int depth, String rootPlayerId, int color,
                     ZobristHasher hasher, TranspositionTable table) {
        int g = 0; // Estimation de départ
        int upper = Integer.MAX_VALUE / 4;
        int lower = Integer.MIN_VALUE / 4;

        // Boucle de convergence de l'estimation du score
        while (lower < upper) {
            // Définition de la fenêtre de recherche nulle de largeur 1 : [beta - 1, beta]
            int beta = (g == lower) ? g + 1 : g;
            
            // Recherche NegaMax avec fenêtre de recherche nulle
            g = alphaBetaWindow(board, currentPlayerId, depth, beta - 1, beta, rootPlayerId, color, hasher, table);
            
            // Ajustement des bornes en fonction du résultat (fail-low ou fail-high)
            if (g < beta) {
                upper = g; // La valeur réelle est inférieure à beta (échec bas)
            } else {
                lower = g; // La valeur réelle est supérieure ou égale à beta (échec haut)
            }
        }

        return g;
    }

    /**
     * Fonction de recherche NegaMax à fenêtre nulle exploitant intensément la table de transpositions.
     * 
     * @param board Le plateau simulé.
     * @param currentPlayerId Le joueur actif.
     * @param depth Profondeur restante.
     * @param alpha Borne inférieure de la fenêtre nulle.
     * @param beta Borne supérieure de la fenêtre nulle.
     * @param rootPlayerId Le joueur Max de départ.
     * @param color Coefficient de perspective (+1 ou -1).
     * @param hasher Hachage Zobrist.
     * @param table Table de transposition.
     * @return Le score d'évaluation à fenêtre nulle.
     */
    private int alphaBetaWindow(StdBoard board, String currentPlayerId, int depth, int alpha, int beta,
                                String rootPlayerId, int color, ZobristHasher hasher, TranspositionTable table) {
        // A. Cas d'arrêts
        String winner = board.checkWinCondition();
        if (winner != null) {
            int raw = winner.equals(rootPlayerId) ? 100000 : -100000;
            return color * raw;
        }
        if (depth <= 0) {
            String opponentId = PlayerOrder.opponentOf(playerOrder, rootPlayerId);
            int eval = Evaluation.evaluate(board, rootPlayerId, opponentId);
            return color * eval;
        }

        int alphaOrig = alpha;
        int betaOrig = beta;
        
        // B. Interrogation et élagage immédiat via la table de transposition (critique pour MTD-f)
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

        // C. Exploration récursive unifiée
        for (Move move : moves) {
            StdBoard next = board.copy();
            SearchUtils.applyMove(next, move);
            String nextPlayer = PlayerOrder.nextPlayer(playerOrder, currentPlayerId);
            
            // Appel récursif avec inversion de bornes : [-beta, -alpha]
            int score = -alphaBetaWindow(next, nextPlayer, depth - 1, -beta, -alpha, rootPlayerId, -color, hasher, table);
            if (score > bestScore) bestScore = score;
            alpha = Math.max(alpha, score);
            if (alpha >= beta) break; // Coupure Alpha-Beta
        }

        // D. Enregistrement en cache
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

