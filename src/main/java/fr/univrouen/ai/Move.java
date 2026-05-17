package fr.univrouen.ai;

import fr.univrouen.model.Orientation;

/**
 * Représente une action atomique du jeu (déplacement de pion ou pose de barrière).
 * 
 * Cette classe est immuable et contient toutes les informations nécessaires pour
 * appliquer ou annuler un coup.
 */
public class Move {
    /**
     * Type d'action possible au Quoridor.
     */
    public enum Type {
        PAWN,
        WALL
    }

    private final Type type;
    private final String playerId;
    private final int x;
    private final int z;
    private final Orientation orientation;

    private Move(Type type, String playerId, int x, int z, Orientation orientation) {
        this.type = type;
        this.playerId = playerId;
        this.x = x;
        this.z = z;
        this.orientation = orientation;
    }

    public static Move pawnMove(String playerId, int x, int z) {
        return new Move(Type.PAWN, playerId, x, z, null);
    }

    public static Move wallMove(String playerId, int x, int z, Orientation orientation) {
        if (orientation == null) {
            throw new IllegalArgumentException("orientation cannot be null");
        }
        return new Move(Type.WALL, playerId, x, z, orientation);
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

    public boolean isPawnMove() {
        return type == Type.PAWN;
    }

    public boolean isWallMove() {
        return type == Type.WALL;
    }

    @Override
    public String toString() {
        if (type == Type.PAWN) {
            return "Move{pawn " + playerId + " -> (" + x + "," + z + ")}";
        }
        return "Move{wall " + playerId + " @ (" + x + "," + z + ") " + orientation + "}";
    }
}
