package fr.univrouen.model;

import java.util.ArrayDeque;
import java.util.Queue;

public final class Pathfinding {
    private Pathfinding() {
    }

    public static int shortestPathLength(Board board, Pawn pawn) {
        if (board == null || pawn == null) return Integer.MAX_VALUE;

        int size = board.getSize();
        boolean[][] visited = new boolean[size][size];
        Queue<int[]> queue = new ArrayDeque<>();

        queue.add(new int[]{pawn.getX(), pawn.getZ(), 0});
        visited[pawn.getX()][pawn.getZ()] = true;

        while (!queue.isEmpty()) {
            int[] cur = queue.poll();
            int x = cur[0];
            int z = cur[1];
            int dist = cur[2];

            if (pawn.goalReachedAt(x, z)) {
                return dist;
            }

            visitNeighbor(board, size, visited, queue, x, z, x + 1, z, dist);
            visitNeighbor(board, size, visited, queue, x, z, x - 1, z, dist);
            visitNeighbor(board, size, visited, queue, x, z, x, z + 1, dist);
            visitNeighbor(board, size, visited, queue, x, z, x, z - 1, dist);
        }

        return Integer.MAX_VALUE;
    }

    private static void visitNeighbor(Board board, int size, boolean[][] visited, Queue<int[]> queue,
                                      int x, int z, int nx, int nz, int dist) {
        if (nx < 0 || nx >= size || nz < 0 || nz >= size) return;
        if (visited[nx][nz]) return;
        if (board.isBlockedBetween(x, z, nx, nz)) return;

        visited[nx][nz] = true;
        queue.add(new int[]{nx, nz, dist + 1});
    }
}
