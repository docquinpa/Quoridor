package fr.univrouen.ai;

import java.util.List;

/**
 * Gère la rotation circulaire des joueurs dans une partie de Quoridor.
 * 
 * Utile pour déterminer le joueur actif lors de l'exploration de l'arbre de jeu.
 */
public final class PlayerOrder {
    private PlayerOrder() {
    }

    public static String nextPlayer(List<String> order, String currentPlayer) {
        if (order == null || order.isEmpty()) return null;
        int idx = order.indexOf(currentPlayer);
        if (idx < 0) return order.get(0);
        return order.get((idx + 1) % order.size());
    }

    public static String opponentOf(List<String> order, String currentPlayer) {
        return nextPlayer(order, currentPlayer);
    }
}
