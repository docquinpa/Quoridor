package fr.univrouen.replay;

import fr.univrouen.QuoridorFactory;
import fr.univrouen.ai.AiEngine;
import fr.univrouen.ai.Move;
import fr.univrouen.model.GameSettings;
import fr.univrouen.model.Orientation;
import fr.univrouen.model.Pawn;
import fr.univrouen.model.StdBoard;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class ReplayLoader {
    private static final String[] DEFAULT_REPLAYS = {};
    private static final int AI_DUEL_MAX_MOVES = 200;

    private ReplayLoader() {
    }

    public static List<ReplayGame> loadAll() {
        List<ReplayGame> games = new ArrayList<>();
        ReplayGame duel = generateAiDuel();
        if (duel != null) games.add(duel);
        for (String path : DEFAULT_REPLAYS) {
            ReplayGame game = loadFromResource(path);
            if (game != null) games.add(game);
        }
        return games;
    }

    public static ReplayGame loadFromResource(String path) {
        InputStream stream = ReplayLoader.class.getResourceAsStream(path);
        if (stream == null) return null;

        List<ReplayMove> moves = new ArrayList<>();
        String name = "Partie";

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
                if (trimmed.startsWith("name=")) {
                    name = trimmed.substring(5).trim();
                    continue;
                }
                ReplayMove move = parseMove(trimmed, lineNumber);
                if (move != null) moves.add(move);
            }
        } catch (Exception ex) {
            return null;
        }

        return new ReplayGame(name, moves);
    }

    private static ReplayMove parseMove(String line, int lineNumber) {
        String[] parts = line.split(",");
        if (parts.length < 4) return null;
        String type = parts[0].trim();
        String playerId = parts[1].trim();

        try {
            int x = Integer.parseInt(parts[2].trim());
            int z = Integer.parseInt(parts[3].trim());

            if ("P".equalsIgnoreCase(type)) {
                return ReplayMove.pawnMove(playerId, x, z);
            }
            if ("W".equalsIgnoreCase(type)) {
                if (parts.length < 5) return null;
                String raw = parts[4].trim().toUpperCase();
                Orientation orientation = "H".equals(raw) ? Orientation.HORIZONTAL : Orientation.VERTICAL;
                return ReplayMove.wallMove(playerId, x, z, orientation);
            }
        } catch (NumberFormatException ex) {
            return null;
        }

        return null;
    }

    private static ReplayGame generateAiDuel() {
        StdBoard board = new StdBoard(9);
        Pawn p1 = QuoridorFactory.createPawn("p1", 4, 0, (x, z) -> z == 8);
        Pawn p2 = QuoridorFactory.createPawn("p2", 4, 8, (x, z) -> z == 0);
        board.addPawn(p1);
        board.addPawn(p2);

        GameSettings settings = new GameSettings();
        settings.setDifficulty(GameSettings.Difficulty.MEDIUM);

        AiEngine ai1 = new AiEngine();
        ai1.setAlgorithm(AiEngine.Algorithm.NEG_ALPHA_BETA);

        AiEngine ai2 = new AiEngine();
        ai2.setAlgorithm(AiEngine.Algorithm.ALPHA_BETA);

        List<ReplayMove> moves = new ArrayList<>();
        List<String> order = List.of("p1", "p2");
        String current = "p1";

        for (int turn = 0; turn < AI_DUEL_MAX_MOVES; turn++) {
            AiEngine ai = current.equals("p1") ? ai1 : ai2;
            Move move = ai.chooseMove(board.copy(), current, order, settings);
            if (move == null) break;

            boolean ok = applyMove(board, move);
            if (!ok) break;

            moves.add(toReplayMove(move));

            String winner = board.checkWinCondition();
            if (winner != null) break;
            current = current.equals("p1") ? "p2" : "p1";
        }

        if (moves.isEmpty()) return null;

        String name = "Duel IA (Alpha-Beta vs Nega-Alpha-Beta)";
        return new ReplayGame(name, moves);
    }

    private static boolean applyMove(StdBoard board, Move move) {
        if (move.isPawnMove()) {
            try {
                board.movePawn(move.getPlayerId(), move.getX(), move.getZ());
                return true;
            } catch (IllegalArgumentException ex) {
                return false;
            }
        }
        return board.placeWallForPlayer(move.getPlayerId(), move.getX(), move.getZ(), move.getOrientation());
    }

    private static ReplayMove toReplayMove(Move move) {
        if (move.isPawnMove()) {
            return ReplayMove.pawnMove(move.getPlayerId(), move.getX(), move.getZ());
        }
        return ReplayMove.wallMove(move.getPlayerId(), move.getX(), move.getZ(), move.getOrientation());
    }
}
