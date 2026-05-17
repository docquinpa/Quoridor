package fr.univrouen;

import fr.univrouen.model.Board;
import fr.univrouen.model.Orientation;
import fr.univrouen.model.Pawn;
import fr.univrouen.model.Pathfinding;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PathfindingTest {
    @Test
    void testInitialDistanceStraightLine() {
        Board board = QuoridorFactory.createBoard(9);
        Pawn p1 = QuoridorFactory.createPawn("p1", 4, 0, (x, z) -> z == 8);
        Pawn p2 = QuoridorFactory.createPawn("p2", 4, 8, (x, z) -> z == 0);
        board.addPawn(p1);
        board.addPawn(p2);

        int dist = Pathfinding.shortestPathLength(board, p1);
        assertEquals(8, dist);
    }

    @Test
    void testDistanceIncreasesAfterWall() {
        Board board = QuoridorFactory.createBoard(9);
        Pawn p1 = QuoridorFactory.createPawn("p1", 4, 0, (x, z) -> z == 8);
        Pawn p2 = QuoridorFactory.createPawn("p2", 4, 8, (x, z) -> z == 0);
        board.addPawn(p1);
        board.addPawn(p2);

        boolean placed = board.placeWallForPlayer("p1", 4, 0, Orientation.HORIZONTAL);
        assertTrue(placed);

        int dist = Pathfinding.shortestPathLength(board, p1);
        assertTrue(dist > 8);
        assertTrue(dist < Integer.MAX_VALUE);
    }
}
