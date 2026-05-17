package fr.univrouen;

import fr.univrouen.model.Board;
import fr.univrouen.model.Orientation;
import fr.univrouen.model.Pawn;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MovementRulesTest {
    @Test
    void testSimpleMoveBlockedByWall() {
        Board board = QuoridorFactory.createBoard(9);
        Pawn p1 = QuoridorFactory.createPawn("p1", 4, 0, (x, z) -> z == 8);
        Pawn p2 = QuoridorFactory.createPawn("p2", 4, 8, (x, z) -> z == 0);
        board.addPawn(p1);
        board.addPawn(p2);

        assertTrue(board.placeWallForPlayer("p1", 4, 0, Orientation.HORIZONTAL));
        assertFalse(board.canMove("p1", 4, 1));
    }

    @Test
    void testJumpOverPawn() {
        Board board = QuoridorFactory.createBoard(9);
        Pawn p1 = QuoridorFactory.createPawn("p1", 4, 4, (x, z) -> z == 8);
        Pawn p2 = QuoridorFactory.createPawn("p2", 4, 5, (x, z) -> z == 0);
        board.addPawn(p1);
        board.addPawn(p2);

        assertTrue(board.canMove("p1", 4, 6));
    }

    @Test
    void testDiagonalOnlyWhenJumpBlocked() {
        Board board = QuoridorFactory.createBoard(9);
        Pawn p1 = QuoridorFactory.createPawn("p1", 4, 4, (x, z) -> z == 8);
        Pawn p2 = QuoridorFactory.createPawn("p2", 4, 5, (x, z) -> z == 0);
        board.addPawn(p1);
        board.addPawn(p2);

        assertFalse(board.canMove("p1", 5, 5));
        assertTrue(board.canMove("p1", 4, 6));

        assertTrue(board.placeWallForPlayer("p1", 4, 5, Orientation.HORIZONTAL));
        assertFalse(board.canMove("p1", 4, 6));
        assertTrue(board.canMove("p1", 5, 5));
    }
}
