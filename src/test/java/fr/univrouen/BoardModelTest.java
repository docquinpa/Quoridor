package fr.univrouen;

import fr.univrouen.model.Board;
import fr.univrouen.model.Orientation;
import fr.univrouen.model.Pawn;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests pour la logique du plateau Quoridor.
 * Utilise les interfaces (Board, Pawn) pour respecter SOLID.
 */
public class BoardModelTest {

    @Test
    public void testPawnMoveAndWall() {
        Board board = QuoridorFactory.createBoard(9);

        Pawn p1 = QuoridorFactory.createPawn("p1", 4, 0, (x,z) -> z==8);
        Pawn p2 = QuoridorFactory.createPawn("p2", 4, 8, (x,z) -> z==0);
        board.addPawn(p1);
        board.addPawn(p2);

        // simple move forward
        assertTrue(board.canMove("p1", 4, 1));
        board.movePawn("p1", 4, 1);
        assertEquals(1, p1.getZ());

        // place a vertical wall that doesn't block all paths
        boolean placed = board.placeWallForPlayer("p1", 3, 1, Orientation.VERTICAL);
        assertTrue(placed);

        // try to place a wall that overlaps -> should be false
        boolean placed2 = board.placeWallForPlayer("p1", 3, 1, Orientation.VERTICAL);
        assertFalse(placed2);
    }

    @Test
    public void testDiagonalJumpWhenStraightBlocked() {
        Board board = QuoridorFactory.createBoard(9);
        // p1 at (4,4), p2 at (5,4)
        Pawn p1 = QuoridorFactory.createPawn("p1", 4, 4, (x,z) -> z==8);
        Pawn p2 = QuoridorFactory.createPawn("p2", 5, 4, (x,z) -> z==0);
        board.addPawn(p1);
        board.addPawn(p2);

        // place vertical wall to block direct jump from (5,4) to (6,4)
        boolean placed = board.placeWallForPlayer("p1", 5, 4, Orientation.VERTICAL);
        assertTrue(placed);

        // p1 cannot jump straight to (6,4)
        assertFalse(board.canMove("p1", 6, 4));

        // but should be allowed to move diagonally to (5,5) if not blocked
        assertTrue(board.canMove("p1", 5, 5));
    }
}
