package fr.univrouen.replay;

import java.util.Collections;
import java.util.List;

public class ReplayGame {
    private final String name;
    private final List<ReplayMove> moves;

    public ReplayGame(String name, List<ReplayMove> moves) {
        this.name = name;
        this.moves = List.copyOf(moves);
    }

    public String getName() {
        return name;
    }

    public List<ReplayMove> getMoves() {
        return Collections.unmodifiableList(moves);
    }
}
