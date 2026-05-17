package fr.univrouen.model;

import java.util.*;

public class StdBoard implements Board {
    private final int size;
    // walls: indices 0..size-2 for wall positions
    private final boolean[][] wallH; // horizontal walls between z and z+1: [x][z]
    private final boolean[][] wallV; // vertical walls between x and x+1: [x][z]

    private final Map<String, Pawn> pawns = new HashMap<>();
    private final Map<String, Integer> wallsRemaining = new HashMap<>();
    private boolean actionTakenThisTurn = false;

    public StdBoard() { this(9); }

    public StdBoard(int size) {
        this.size = size;
        // vertical walls exist between columns for each row: (size-1) x size
        this.wallV = new boolean[size-1][size];
        // horizontal walls exist between rows for each column: size x (size-1)
        this.wallH = new boolean[size][size-1];
    }

    public StdBoard(StdBoard other) {
        this.size = other.size;
        this.wallV = new boolean[size-1][size];
        this.wallH = new boolean[size][size-1];

        for (int x = 0; x < wallV.length; x++) {
            System.arraycopy(other.wallV[x], 0, wallV[x], 0, wallV[x].length);
        }
        for (int x = 0; x < wallH.length; x++) {
            System.arraycopy(other.wallH[x], 0, wallH[x], 0, wallH[x].length);
        }

        for (Pawn p : other.pawns.values()) {
            Pawn clone = new StdPawn(p.getId(), p.getX(), p.getZ(), p.getGoal());
            pawns.put(clone.getId(), clone);
        }
        wallsRemaining.putAll(other.wallsRemaining);
        actionTakenThisTurn = other.actionTakenThisTurn;
    }

    @Override
    public int getSize() { return size; }

    @Override
    public void addPawn(Pawn pawn) {
        checkInBounds(pawn.getX(), pawn.getZ());
        pawns.put(pawn.getId(), pawn);
        // default walls per player
        wallsRemaining.put(pawn.getId(), 10);
    }

    @Override
    public Pawn getPawn(String id) { return pawns.get(id); }

    @Override
    public Map<String, Pawn> getPawns() {
        return new HashMap<>(pawns);
    }

    @Override
    public boolean isOccupied(int x, int z) {
        return pawns.values().stream().anyMatch(p -> p.getX()==x && p.getZ()==z);
    }

    private void checkInBounds(int x,int z) {
        if (x < 0 || x >= size || z < 0 || z >= size) throw new IllegalArgumentException("Out of bounds");
    }

    // Movement: allow orthogonal moves if no wall blocks; allow simple jump over adjacent pawn
    @Override
    public boolean canMove(String pawnId, int toX, int toZ) {
        Pawn p = pawns.get(pawnId);
        if (p == null) return false;
        checkInBounds(toX, toZ);
        int x = p.getX(), z = p.getZ();
        if (x==toX && z==toZ) return false;

        int dx = toX - x;
        int dz = toZ - z;

        // orthogonal adjacent
        if (Math.abs(dx) + Math.abs(dz) == 1) {
            if (isBlockedBetween(x, z, toX, toZ)) return false;
            return !isOccupied(toX, toZ);
        }

        // simple jump over adjacent pawn in same direction (two cells)
        if (Math.abs(dx) + Math.abs(dz) == 2 && (Math.abs(dx)==2 || Math.abs(dz)==2)) {
            int midX = x + Integer.signum(dx);
            int midZ = z + Integer.signum(dz);
            if (!isOccupied(midX, midZ)) return false;
            // both steps must not be blocked by walls
            if (isBlockedBetween(x, z, midX, midZ)) return false;
            if (isBlockedBetween(midX, midZ, toX, toZ)) return false;
            return !isOccupied(toX, toZ);
        }

        // diagonal move: only allowed when a straight jump is blocked
        if (Math.abs(dx) == 1 && Math.abs(dz) == 1) {
            if (isOccupied(toX, toZ)) return false;

            int adjX = x + dx;
            int adjZ = z;
            if (isWithin(adjX, adjZ) && isOccupied(adjX, adjZ)) {
                if (!isBlockedBetween(x, z, adjX, adjZ)) {
                    int jumpX = x + 2 * dx;
                    int jumpZ = z;
                    if (isJumpBlocked(adjX, adjZ, jumpX, jumpZ) && !isBlockedBetween(adjX, adjZ, toX, toZ)) {
                        return true;
                    }
                }
            }

            int adj2X = x;
            int adj2Z = z + dz;
            if (isWithin(adj2X, adj2Z) && isOccupied(adj2X, adj2Z)) {
                if (!isBlockedBetween(x, z, adj2X, adj2Z)) {
                    int jumpX = x;
                    int jumpZ = z + 2 * dz;
                    if (isJumpBlocked(adj2X, adj2Z, jumpX, jumpZ) && !isBlockedBetween(adj2X, adj2Z, toX, toZ)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public void movePawn(String pawnId, int toX, int toZ) {
        if (!canMove(pawnId, toX, toZ)) throw new IllegalArgumentException("Invalid move");
        Pawn p = pawns.get(pawnId);
        p.setPos(toX, toZ);
    }

    @Override
    public boolean isBlockedBetween(int x1,int z1,int x2,int z2) {
        // assume adjacent
        if (x2==x1+1 && z2==z1) { // east
            if (x1 >= size-1) return true;
            return wallV[x1][z1];
        }
        if (x2==x1-1 && z2==z1) { // west
            if (x2 < 0) return true;
            return wallV[x2][z2];
        }
        if (z2==z1+1 && x2==x1) { // south
            if (z1 >= size-1) return true;
            return wallH[x1][z1];
        }
        if (z2==z1-1 && x2==x1) { // north
            if (z2 < 0) return true;
            return wallH[x2][z2];
        }
        return true; // non-adjacent default blocked
    }

    @Override
    public boolean hasWall(int x, int z, Orientation orientation) {
        if (!isWallAnchorInBounds(x, z)) return false;
        return hasWallAnchor(x, z, orientation);
    }

    private boolean isWithin(int x,int z) {
        return x >= 0 && x < size && z >= 0 && z < size;
    }

    private boolean isJumpBlocked(int adjX, int adjZ, int jumpX, int jumpZ) {
        if (!isWithin(jumpX, jumpZ)) return true;
        if (isBlockedBetween(adjX, adjZ, jumpX, jumpZ)) return true;
        return isOccupied(jumpX, jumpZ);
    }

    // Check which pawn (if any) has reached their goal
    @Override
    public String checkWinCondition() {
        for (Pawn p : pawns.values()) {
            if (p.hasReachedGoal()) return p.getId();
        }
        return null;
    }

    // Reset the game to initial state (for new game)
    @Override
    public void reset() {
        // Clear walls
        for (int i = 0; i < wallH.length; i++) {
            for (int j = 0; j < wallH[i].length; j++) {
                wallH[i][j] = false;
            }
        }
        for (int i = 0; i < wallV.length; i++) {
            for (int j = 0; j < wallV[i].length; j++) {
                wallV[i][j] = false;
            }
        }
        // reset walls per player
        for (String id : wallsRemaining.keySet()) {
            wallsRemaining.put(id, 10);
        }
        resetActionFlag();
    }

    @Override
    public boolean hasActionThisTurn() { return actionTakenThisTurn; }

    @Override
    public void markActionTaken() { actionTakenThisTurn = true; }

    @Override
    public void resetActionFlag() { actionTakenThisTurn = false; }

    @Override
    public boolean placeWallForPlayer(String playerId, int x, int z, Orientation orientation) {
        if (!wallsRemaining.containsKey(playerId)) return false;
        int remaining = wallsRemaining.get(playerId);
        if (remaining <= 0) return false;
        if (orientation == null) return false;
        if (!canPlaceWallSegments(x, z, orientation)) return false;

        StdBoard copy = this.copy();
        copy.placeWallSegments(x, z, orientation);
        if (!copy.allPlayersHavePath()) return false;

        placeWallSegments(x, z, orientation);
        wallsRemaining.put(playerId, remaining - 1);
        return true;
    }

    @Override
    public int getWallsRemaining(String playerId) {
        return wallsRemaining.getOrDefault(playerId, 0);
    }

    @Override
    public Map<String, Integer> getWallsRemainingMap() {
        return new HashMap<>(wallsRemaining);
    }

    private boolean isWallAnchorInBounds(int x, int z) {
        return x >= 0 && z >= 0 && x < size - 1 && z < size - 1;
    }

    private boolean hasWallAnchor(int x, int z, Orientation orientation) {
        if (!isWallAnchorInBounds(x, z)) return false;
        if (orientation == Orientation.HORIZONTAL) {
            return wallH[x][z] && wallH[x + 1][z];
        }
        return wallV[x][z] && wallV[x][z + 1];
    }

    private boolean canPlaceWallSegments(int x, int z, Orientation orientation) {
        if (!isWallAnchorInBounds(x, z)) return false;

        if (orientation == Orientation.HORIZONTAL) {
            if (wallH[x][z] || wallH[x + 1][z]) return false;
            if (hasWallAnchor(x, z, Orientation.VERTICAL)) return false;
        } else {
            if (wallV[x][z] || wallV[x][z + 1]) return false;
            if (hasWallAnchor(x, z, Orientation.HORIZONTAL)) return false;
        }

        return true;
    }

    private void placeWallSegments(int x, int z, Orientation orientation) {
        if (orientation == Orientation.HORIZONTAL) {
            wallH[x][z] = true;
            wallH[x + 1][z] = true;
        } else {
            wallV[x][z] = true;
            wallV[x][z + 1] = true;
        }
    }

    private boolean allPlayersHavePath() {
        for (Pawn pawn : pawns.values()) {
            if (Pathfinding.shortestPathLength(this, pawn) == Integer.MAX_VALUE) {
                return false;
            }
        }
        return true;
    }

    public StdBoard copy() {
        return new StdBoard(this);
    }
}
