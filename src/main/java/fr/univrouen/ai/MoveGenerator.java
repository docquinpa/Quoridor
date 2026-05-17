package fr.univrouen.ai;

import fr.univrouen.model.Orientation;
import fr.univrouen.model.StdBoard;

import java.util.ArrayList;
import java.util.List;

/**
 * Générateur de coups légaux pour le Quoridor.
 * 
 * Fournit toutes les actions possibles (déplacement de pion et pose de barrière)
 * à partir d'un état de plateau donné, en respectant les contraintes du jeu.
 */
public class MoveGenerator {
    public List<Move> generateMoves(StdBoard board, String playerId) {
        List<Move> moves = new ArrayList<>();
        if (board == null || playerId == null) return moves;

        int size = board.getSize();
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                if (board.canMove(playerId, x, z)) {
                    moves.add(Move.pawnMove(playerId, x, z));
                }
            }
        }

        if (board.getWallsRemaining(playerId) > 0) {
            for (int x = 0; x < size - 1; x++) {
                for (int z = 0; z < size - 1; z++) {
                    if (!board.hasWall(x, z, Orientation.HORIZONTAL) && canPlaceWall(board, playerId, x, z, Orientation.HORIZONTAL)) {
                        moves.add(Move.wallMove(playerId, x, z, Orientation.HORIZONTAL));
                    }
                }
            }
            for (int x = 0; x < size - 1; x++) {
                for (int z = 0; z < size - 1; z++) {
                    if (!board.hasWall(x, z, Orientation.VERTICAL) && canPlaceWall(board, playerId, x, z, Orientation.VERTICAL)) {
                        moves.add(Move.wallMove(playerId, x, z, Orientation.VERTICAL));
                    }
                }
            }
        }

        return moves;
    }

    private boolean canPlaceWall(StdBoard board, String playerId, int x, int z, Orientation orientation) {
        StdBoard copy = board.copy();
        return copy.placeWallForPlayer(playerId, x, z, orientation);
    }
}
