package fr.univrouen.ai;

import fr.univrouen.model.Board;
import fr.univrouen.model.Orientation;
import fr.univrouen.model.Pawn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Implémente le Hachage de Zobrist pour coder l'état global du plateau Quoridor.
 * 
 * Cet algorithme produit une clé de hachage de type 'long' (64 bits) à l'aide d'opérations XOR.
 * Il permet d'identifier de manière quasi-unique une position et de la retrouver instantanément
 * dans la table de transposition (cache de recherche).
 */
public class ZobristHasher {
    private final int size;
    private final List<String> playerOrder;
    private final Map<String, Integer> playerIndex = new HashMap<>();
    
    // Tables contenant des nombres de 64 bits pseudo-aléatoires générés de manière fixe.
    private final long[][][] pawnTable;
    private final long[][] wallHTable;
    private final long[][] wallVTable;
    private final long[] turnTable;

    /**
     * Initialise les tables de Zobrist avec des nombres aléatoires générés à partir d'une graine (seed) stable.
     * 
     * @param size Taille du plateau (typiquement 9).
     * @param playerOrder Liste ordonnée des identifiants des joueurs pour indexer le tour de rôle.
     * @param seed Graine de générateur pseudo-aléatoire pour assurer la reproductibilité des clés de hachage.
     */
    public ZobristHasher(int size, List<String> playerOrder, long seed) {
        this.size = size;
        this.playerOrder = playerOrder;
        for (int i = 0; i < playerOrder.size(); i++) {
            playerIndex.put(playerOrder.get(i), i);
        }

        Random random = new Random(seed);
        
        // 1. Initialisation de la table tridimensionnelle pour les positions des pions :
        // Dimensions : [Nombre de joueurs][Coordonnée X][Coordonnée Z]
        pawnTable = new long[playerOrder.size()][size][size];
        for (int p = 0; p < playerOrder.size(); p++) {
            for (int x = 0; x < size; x++) {
                for (int z = 0; z < size; z++) {
                    pawnTable[p][x][z] = random.nextLong();
                }
            }
        }

        // 2. Table pour les barrières horizontales :
        // Dimensions : [Coordonnée X][Coordonnée Z]
        wallHTable = new long[size][size - 1];
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size - 1; z++) {
                wallHTable[x][z] = random.nextLong();
            }
        }

        // 3. Table pour les barrières verticales :
        // Dimensions : [Coordonnée X][Coordonnée Z]
        wallVTable = new long[size - 1][size];
        for (int x = 0; x < size - 1; x++) {
            for (int z = 0; z < size; z++) {
                wallVTable[x][z] = random.nextLong();
            }
        }

        // 4. Table pour coder le joueur actif courant
        turnTable = new long[playerOrder.size()];
        for (int i = 0; i < playerOrder.size(); i++) {
            turnTable[i] = random.nextLong();
        }
    }

    /**
     * Génère la clé de hachage de Zobrist complète pour une position de plateau donnée.
     * 
     * @param board Le plateau de jeu actuel.
     * @param currentPlayerId L'identifiant du joueur dont c'est le tour.
     * @return La signature 64 bits de l'état actuel du jeu.
     */
    public long hash(Board board, String currentPlayerId) {
        long h = 0L;
        
        // A. Applique par XOR la position de chaque pion présent sur le plateau
        for (String playerId : playerOrder) {
            Pawn pawn = board.getPawn(playerId);
            if (pawn == null) continue;
            Integer idx = playerIndex.get(playerId);
            if (idx == null) continue;
            h ^= pawnTable[idx][pawn.getX()][pawn.getZ()];
        }

        // B. Applique par XOR la présence de chaque barrière horizontale posée
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size - 1; z++) {
                if (board.hasWall(x, z, Orientation.HORIZONTAL)) {
                    h ^= wallHTable[x][z];
                }
            }
        }
        
        // C. Applique par XOR la présence de chaque barrière verticale posée
        for (int x = 0; x < size - 1; x++) {
            for (int z = 0; z < size; z++) {
                if (board.hasWall(x, z, Orientation.VERTICAL)) {
                    h ^= wallVTable[x][z];
                }
            }
        }

        // D. Applique par XOR l'identifiant du joueur dont c'est le tour de jeu
        Integer turnIdx = playerIndex.get(currentPlayerId);
        if (turnIdx != null) {
            h ^= turnTable[turnIdx];
        }

        return h;
    }
}

