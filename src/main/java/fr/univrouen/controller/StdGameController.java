package fr.univrouen.controller;

import fr.univrouen.model.Board;
import fr.univrouen.model.Pawn;
import fr.univrouen.model.Orientation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Implémentation standard du contrôleur de jeu Quoridor.
 * Utilise l'interface Board pour respecter le principe SOLID.
 */
public class StdGameController implements GameController {
    private final Board model;
    private final List<String> playerOrder = new ArrayList<>();
    private int currentIndex = -1;
    private final List<GameListener> listeners = new CopyOnWriteArrayList<>();

    /**
     * Crée un contrôleur avec un plateau fourni.
     * @param model le plateau à utiliser (dépendance injectée)
     */
    public StdGameController(Board model) {
        this.model = model;
    }

    public Board getModel() { 
        return model; 
    }

    @Override
    public void addListener(GameListener l) { 
        listeners.add(l); 
    }

    @Override
    public void removeListener(GameListener l) { 
        listeners.remove(l); 
    }

    @Override
    public void registerPlayer(Pawn p) {
        model.addPawn(p);
        playerOrder.add(p.getId());
        if (currentIndex < 0) currentIndex = 0;
        for (GameListener l : listeners) l.onPlayerRegistered(p.getId());
        notifyTurnChanged();
    }

    @Override
    public boolean movePawn(String pawnId, int toX, int toZ) {
        if (model.hasActionThisTurn()) return false; // already moved/placed wall this turn
        if (!model.canMove(pawnId, toX, toZ)) return false;
        model.movePawn(pawnId, toX, toZ);
        model.markActionTaken();
        for (GameListener l : listeners) l.onPawnMoved(pawnId, toX, toZ);
        
        // Check win condition
        String winner = model.checkWinCondition();
        if (winner != null) {
            for (GameListener l : listeners) l.onGameWon(winner);
            return true; // game over
        }
        
        advanceTurn();
        return true;
    }

    @Override
    public boolean placeWall(String playerId, int x, int z, Orientation orientation) {
        if (model.hasActionThisTurn()) return false; // already moved/placed wall this turn
        boolean ok = model.placeWallForPlayer(playerId, x, z, orientation);
        if (!ok) return false;
        model.markActionTaken();
        for (GameListener l : listeners) l.onWallPlaced(x, z, orientation);
        advanceTurn();
        return true;
    }

    private void advanceTurn() {
        if (playerOrder.isEmpty()) return;
        currentIndex = (currentIndex + 1) % playerOrder.size();
        model.resetActionFlag(); // reset action flag for new turn
        notifyTurnChanged();
    }

    private void notifyTurnChanged() {
        String cur = getCurrentPlayerId();
        for (GameListener l : listeners) l.onTurnChanged(cur);
    }

    @Override
    public String getCurrentPlayerId() {
        if (currentIndex < 0 || playerOrder.isEmpty()) return null;
        return playerOrder.get(currentIndex);
    }

    @Override
    public List<String> getPlayerOrder() { 
        return Collections.unmodifiableList(playerOrder); 
    }

    @Override
    public int getWallsRemaining(String playerId) { 
        return model.getWallsRemaining(playerId); 
    }

    @Override
    public void resetGame() {
        model.reset();
        currentIndex = 0;
        model.resetActionFlag();
        notifyTurnChanged();
    }
}
