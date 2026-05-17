package fr.univrouen;

import fr.univrouen.ai.AiEngine;
import fr.univrouen.ai.Evaluation;
import fr.univrouen.ai.Move;
import fr.univrouen.model.GameSettings;
import fr.univrouen.model.Pawn;
import fr.univrouen.model.StdBoard;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiEvaluationTest {
    @Test
    void testEvaluationRewardsCloserPawn() {
        StdBoard board = new StdBoard(9);
        Pawn p1 = QuoridorFactory.createPawn("p1", 4, 0, (x, z) -> z == 8);
        Pawn p2 = QuoridorFactory.createPawn("p2", 4, 8, (x, z) -> z == 0);
        board.addPawn(p1);
        board.addPawn(p2);

        int base = Evaluation.evaluate(board, "p1", "p2");
        assertEquals(0, base);

        board.movePawn("p1", 4, 1);
        int improved = Evaluation.evaluate(board, "p1", "p2");
        assertTrue(improved > base);
    }

    @Test
    void testAiEngineReturnsMove() {
        StdBoard board = new StdBoard(9);
        Pawn p1 = QuoridorFactory.createPawn("p1", 4, 0, (x, z) -> z == 8);
        Pawn p2 = QuoridorFactory.createPawn("p2", 4, 8, (x, z) -> z == 0);
        board.addPawn(p1);
        board.addPawn(p2);

        GameSettings settings = new GameSettings();
        settings.setDifficulty(GameSettings.Difficulty.EASY);

        AiEngine engine = new AiEngine();
        Move move = engine.chooseMove(board, "p1", List.of("p1", "p2"), settings);
        assertNotNull(move);
    }
}
