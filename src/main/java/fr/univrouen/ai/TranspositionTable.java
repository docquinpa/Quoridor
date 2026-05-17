package fr.univrouen.ai;

import java.util.HashMap;
import java.util.Map;

/**
 * Gère le cache global d'états de jeu (table de transpositions).
 * 
 * Ce cache permet d'éviter l'évaluation répétée d'états identiques atteints par
 * des chemins de recherche différents. Il stocke les bornes mathématiques associées.
 */
public class TranspositionTable {
    
    /**
     * Définit la nature mathématique du score stocké pour une clé de hachage Zobrist.
     */
    public enum Bound {
        /** Le score stocké est la valeur minimax exacte de l'état (pas de coupure). */
        EXACT,
        /** Échec haut (fail-high). Le score réel est supérieur ou égal à la valeur enregistrée. */
        LOWER,
        /** Échec bas (fail-low). Le score réel est inférieur ou égal à la valeur enregistrée. */
        UPPER
    }

    /**
     * Représente une entrée en cache pour une position.
     */
    public static class Entry {
        private final int depth;
        private final int score;
        private final Bound bound;
        private final Move bestMove;

        /**
         * Construit une nouvelle entrée de transposition.
         * 
         * @param depth La profondeur d'exploration restante lors de l'enregistrement de ce nœud.
         * @param score Le score calculé pour l'état.
         * @param bound Le type de borne mathématique (EXACT, LOWER ou UPPER).
         * @param bestMove Le meilleur coup résolu à ce nœud (utilisé pour l'ordonnancement futur).
         */
        public Entry(int depth, int score, Bound bound, Move bestMove) {
            this.depth = depth;
            this.score = score;
            this.bound = bound;
            this.bestMove = bestMove;
        }

        public int getDepth() {
            return depth;
        }

        public int getScore() {
            return score;
        }

        public Bound getBound() {
            return bound;
        }

        public Move getBestMove() {
            return bestMove;
        }
    }

    // Stockage interne associant une clé Zobrist 64 bits à son entrée en cache
    private final Map<Long, Entry> table = new HashMap<>();

    /**
     * Récupère l'entrée associée à une clé Zobrist si elle existe.
     * 
     * @param key Clé de signature Zobrist 64 bits de la position.
     * @return L'entrée de cache, ou null si la position est inconnue.
     */
    public Entry get(long key) {
        return table.get(key);
    }

    /**
     * Insère directement une entrée pré-construite dans la table de transposition.
     */
    public void put(long key, Entry entry) {
        table.put(key, entry);
    }

    /**
     * Construit et insère une nouvelle entrée dans le cache de transpositions.
     * 
     * @param key Clé de signature Zobrist 64 bits.
     * @param depth Profondeur à laquelle l'état a été exploré.
     * @param score Score obtenu.
     * @param bound Nature de la borne calculée.
     * @param bestMove Meilleur coup identifié pour cet état.
     */
    public void store(long key, int depth, int score, Bound bound, Move bestMove) {
        table.put(key, new Entry(depth, score, bound, bestMove));
    }

    /**
     * Vide intégralement le cache (utile entre deux tours de jeu complets).
     */
    public void clear() {
        table.clear();
    }
}

