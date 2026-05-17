package fr.univrouen;

import fr.univrouen.model.Board;
import fr.univrouen.model.Orientation;
import fr.univrouen.model.Pawn;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WallPlacementTest {
    @Test
    void testWallOverlapRejected() {
        Board board = QuoridorFactory.createBoard(9);
        Pawn p1 = QuoridorFactory.createPawn("p1", 4, 0, (x, z) -> z == 8);
        Pawn p2 = QuoridorFactory.createPawn("p2", 4, 8, (x, z) -> z == 0);
        board.addPawn(p1);
        board.addPawn(p2);

        assertTrue(board.placeWallForPlayer("p1", 3, 2, Orientation.HORIZONTAL));
        assertFalse(board.placeWallForPlayer("p1", 3, 2, Orientation.HORIZONTAL));
    }

    @Test
    void testWallCrossingRejected() {
        Board board = QuoridorFactory.createBoard(9);
        Pawn p1 = QuoridorFactory.createPawn("p1", 4, 0, (x, z) -> z == 8);
        Pawn p2 = QuoridorFactory.createPawn("p2", 4, 8, (x, z) -> z == 0);
        board.addPawn(p1);
        board.addPawn(p2);

        assertTrue(board.placeWallForPlayer("p1", 3, 2, Orientation.HORIZONTAL));
        assertFalse(board.placeWallForPlayer("p1", 3, 2, Orientation.VERTICAL));
    }
}
