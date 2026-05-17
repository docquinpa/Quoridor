package fr.univrouen.ai;

import fr.univrouen.model.StdBoard;

/**
 * Classe utilitaire pour appliquer de manière sûre et uniforme les coups (déplacements de pion ou pose de barrière)
 * sur une instance de plateau de jeu (`StdBoard`).
 * 
 * Cette approche évite la répétition de code dans le module de rechercheAlpha-Bêta.
 */
public final class SearchUtils {
    private SearchUtils() {
    }

    public static void applyMove(StdBoard board, Move move) {
        if (move == null) return;
        if (move.isPawnMove()) {
            board.movePawn(move.getPlayerId(), move.getX(), move.getZ());
            return;
        }
        board.placeWallForPlayer(move.getPlayerId(), move.getX(), move.getZ(), move.getOrientation());
    }
}
