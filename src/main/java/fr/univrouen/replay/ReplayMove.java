package fr.univrouen.replay;

import fr.univrouen.model.Orientation;

public class ReplayMove {
    public enum Type {
        PAWN,
        WALL
    }

    private final Type type;
    private final String playerId;
    private final int x;
    private final int z;
    private final Orientation orientation;

    private ReplayMove(Type type, String playerId, int x, int z, Orientation orientation) {
        this.type = type;
        this.playerId = playerId;
        this.x = x;
        this.z = z;
        this.orientation = orientation;
    }

    public static ReplayMove pawnMove(String playerId, int x, int z) {
        return new ReplayMove(Type.PAWN, playerId, x, z, null);
    }

    public static ReplayMove wallMove(String playerId, int x, int z, Orientation orientation) {
        return new ReplayMove(Type.WALL, playerId, x, z, orientation);
    }

    public Type getType() {
        return type;
    }

    public String getPlayerId() {
        return playerId;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public String describe() {
        if (type == Type.PAWN) {
            return playerId + " -> (" + x + "," + z + ")";
        }
        return playerId + " wall " + orientation + " @ (" + x + "," + z + ")";
    }
}
