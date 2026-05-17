package fr.univrouen.model;

public class StdPawn implements Pawn {
    private final String id;
    private int x;
    private int z;
    private final Goal goal;

    public StdPawn(String id, int x, int z, Goal goal) {
        this.id = id;
        this.x = x;
        this.z = z;
        this.goal = goal;
    }

    @Override
    public String getId() { return id; }

    @Override
    public int getX() { return x; }

    @Override
    public int getZ() { return z; }

    @Override
    public void setPos(int x, int z) { 
        this.x = x; 
        this.z = z; 
    }

    @Override
    public boolean hasReachedGoal() { 
        return goal.reached(x, z); 
    }

    // Check whether the provided coordinates satisfy this pawn's goal.
    @Override
    public boolean goalReachedAt(int tx, int tz) {
        return goal.reached(tx, tz);
    }

    @Override
    public Goal getGoal() {
        return goal;
    }
}
