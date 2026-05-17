package fr.univrouen.ai;

import fr.univrouen.model.GameSettings;
import fr.univrouen.model.StdBoard;

import java.util.List;

/**
 * Moteur central de prise de décision pour les joueurs IA au Quoridor.
 * 
 * Cette classe sert de point d'entrée unique pour sélectionner et exécuter différents algorithmes
 * de recherche (Minimax, Alpha-Bêta, Neg-Alpha-Bêta, SSS*) sur un état de jeu donné.
 * Elle gère également la configuration du paramètre de profondeur de recherche.
 */
public class AiEngine {
    public enum Algorithm {
        MINIMAX,
        ALPHA_BETA,
        NEG_ALPHA_BETA,
        SSS_STAR
    }

    private Algorithm algorithm = Algorithm.NEG_ALPHA_BETA;

    public void setAlgorithm(Algorithm algorithm) {
        if (algorithm == null) {
            throw new IllegalArgumentException("algorithm cannot be null");
        }
        this.algorithm = algorithm;
    }

    public Algorithm getAlgorithm() {
        return algorithm;
    }

    public Move chooseMove(StdBoard board, String playerId, List<String> playerOrder, GameSettings settings) {
        if (board == null || playerId == null || settings == null || playerOrder == null) return null;
        int depth = settings.getSearchDepth();

        return switch (algorithm) {
            case MINIMAX -> new MinimaxSearch(playerOrder).search(board, playerId, depth).move();
            case ALPHA_BETA -> new AlphaBetaSearch(playerOrder).search(board, playerId, depth).move();
            case NEG_ALPHA_BETA -> new NegAlphaBetaSearch(playerOrder).search(board, playerId, depth).move();
            case SSS_STAR -> new SssStarSearch(playerOrder).search(board, playerId, depth).move();
        };
    }
}
