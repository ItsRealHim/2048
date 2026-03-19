package com.example.myapplication;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameModel {

    public static final int SIZE = 4;
    private final int[][] board;
    private final Random random = new Random();
    private int score;

    public GameModel() {
        board = new int[SIZE][SIZE];
        score = 0;
    }

    public int[][] getBoard() {
        return board;
    }

    public int getScore() {
        return score;
    }

    /**
     * Resets the game
     */
    public void startNewGame() {
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                board[i][j] = 0;

        score = 0;
        addNewTile();
        addNewTile();
    }

    /**
     * Adds a random new tile (2 or 4) and returns a TileChange for animation
     */
    public TileChange addNewTile() {
        List<int[]> empty = new ArrayList<>();
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                if (board[i][j] == 0)
                    empty.add(new int[]{i, j});

        if (empty.isEmpty()) return null;

        int[] cell = empty.get(random.nextInt(empty.size()));
        int row = cell[0];
        int col = cell[1];
        int value = random.nextDouble() < 0.9 ? 2 : 4;
        board[row][col] = value;

        return new TileChange(-1, -1, row, col, 0, value, TileChange.Type.NEW);
    }

    /**
     * Handles swipe, returns list of TileChanges
     */
    public List<TileChange> handleSwipe(Direction direction) {
        List<TileChange> changes = new ArrayList<>();
        int[][] preMove = copyBoard(board);

        // Transform board for LEFT logic
        boolean mirror = false;
        int rotations = 0;
        switch (direction) {
            case UP:
                rotations = 3;
                break;
            case DOWN:
                rotations = 1;
                break;
            case RIGHT:
                mirror = true;
                break;
            case LEFT:
                break;
        }

        if (rotations != 0) rotateBoard(rotations);
        if (mirror) mirrorRows();

        // Slide & merge per row and record TileChanges
        for (int i = 0; i < SIZE; i++) {
            int[] oldRow = board[i].clone();
            List<TileChange> rowChanges = slideAndMergeRow(i, oldRow);
            changes.addAll(rowChanges);
        }

        // Undo transformations
        if (mirror) mirrorRows();
        if (rotations != 0) rotateBoard((4 - rotations) % 4);

        // Add a new tile if any move happened
        boolean moved = !boardsEqual(preMove, board);
        if (moved) {
            TileChange newTile = addNewTile();
            if (newTile != null) changes.add(newTile);
        }

        return changes;
    }

    /**
     * Slides & merges a single row and returns TileChanges
     */
    private List<TileChange> slideAndMergeRow(int rowIndex, int[] originalRow) {
        List<TileChange> changes = new ArrayList<>();
        int[] temp = new int[SIZE];
        int pos = 0;

        // 1. Slide non-zero tiles
        for (int j = 0; j < SIZE; j++) {
            if (board[rowIndex][j] != 0) {
                temp[pos++] = board[rowIndex][j];
            }
        }

        // 2. Merge tiles
        for (int j = 0; j < SIZE - 1; j++) {
            if (temp[j] != 0 && temp[j] == temp[j + 1]) {
                temp[j] *= 2;
                score += temp[j];
                temp[j + 1] = 0;
            }
        }

        // 3. Slide again to close gaps
        int[] finalRow = new int[SIZE];
        pos = 0;
        for (int j = 0; j < SIZE; j++) {
            if (temp[j] != 0) finalRow[pos++] = temp[j];
        }

        // 4. Record TileChanges
        int targetCol = 0;
        for (int j = 0; j < SIZE; j++) {
            if (originalRow[j] != 0) {
                int oldValue = originalRow[j];
                int newValue = finalRow[targetCol];
                if (oldValue != newValue || j != targetCol) {
                    TileChange.Type type = oldValue == newValue ? TileChange.Type.MOVE : TileChange.Type.MERGE;
                    changes.add(new TileChange(rowIndex, j, rowIndex, targetCol, oldValue, newValue, type));
                }
                targetCol++;
            }
        }

        // Update the actual board row
        System.arraycopy(finalRow, 0, board[rowIndex], 0, SIZE);

        return changes;
    }

    /**
     * Copies a 2D board
     */
    private int[][] copyBoard(int[][] b) {
        int[][] c = new int[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) System.arraycopy(b[i], 0, c[i], 0, SIZE);
        return c;
    }

    /**
     * Checks if two boards are equal
     */
    private boolean boardsEqual(int[][] a, int[][] b) {
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                if (a[i][j] != b[i][j]) return false;
        return true;
    }

    private void mirrorRows() {
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE / 2; j++) {
                int t = board[i][j];
                board[i][j] = board[i][SIZE - 1 - j];
                board[i][SIZE - 1 - j] = t;
            }
    }

    private void rotateBoard(int times) {
        for (int t = 0; t < times; t++) {
            int[][] r = new int[SIZE][SIZE];
            for (int i = 0; i < SIZE; i++)
                for (int j = 0; j < SIZE; j++)
                    r[j][SIZE - 1 - i] = board[i][j];
            for (int i = 0; i < SIZE; i++) System.arraycopy(r[i], 0, board[i], 0, SIZE);
        }
    }

    public boolean isGameOver() {
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                if (board[i][j] == 0) return false;

        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE - 1; j++)
                if (board[i][j] == board[i][j + 1]) return false;

        for (int j = 0; j < SIZE; j++)
            for (int i = 0; i < SIZE - 1; i++)
                if (board[i][j] == board[i + 1][j]) return false;

        return true;
    }

    public enum Direction {UP, DOWN, LEFT, RIGHT}

    /**
     * Describes a tile change for UI animations
     */
    public static class TileChange {
        public final int fromRow, fromCol;
        public final int toRow, toCol;
        public final int oldValue, newValue;
        public final Type type;

        public TileChange(int fromRow, int fromCol, int toRow, int toCol, int oldValue, int newValue, Type type) {
            this.fromRow = fromRow;
            this.fromCol = fromCol;
            this.toRow = toRow;
            this.toCol = toCol;
            this.oldValue = oldValue;
            this.newValue = newValue;
            this.type = type;
        }

        public enum Type {MOVE, MERGE, NEW}
    }
}