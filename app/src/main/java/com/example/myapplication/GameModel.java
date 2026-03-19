package com.example.myapplication;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Game logic for 2048.
 * Handles board state, tile movements, merges, and generates TileChange objects for animation.
 */
public class GameModel {

    public static final int SIZE = 4;

    private final int[][] board;
    private final Random random = new Random();
    private int score;

    // ============ CONSTRUCTORS ============

    /**
     * Create a new game with an empty board.
     */
    public GameModel() {
        board = new int[SIZE][SIZE];
        score = 0;
    }

    // ============ GETTERS ============

    public int[][] getBoard() {
        return board;
    }

    public int getScore() {
        return score;
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

    // ============ GAME STATE ============

    /**
     * Start a new game: clear the board and add two initial tiles.
     */
    public void startNewGame() {
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                board[i][j] = 0;
        score = 0;
        addNewTile();
        addNewTile();
    }

    // ============ TILE MANAGEMENT ============

    /**
     * Add a random tile (2 with 90% probability, 4 with 10%).
     * Returns null if board is full.
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

    // ============ SWIPE HANDLING ============

    /**
     * Handle a swipe in the given direction.
     * Returns a list of TileChange objects describing all animations needed.
     */
    public List<TileChange> handleSwipe(Direction direction) {
        List<TileChange> changes = new ArrayList<>();
        int[][] preMove = copyBoard(board);

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

        for (int i = 0; i < SIZE; i++) {
            List<TileChange> rowChanges = slideAndMergeRow(i);
            changes.addAll(rowChanges);
        }

        if (mirror) mirrorRows();
        if (rotations != 0) rotateBoard((4 - rotations) % 4);

        for (int i = 0; i < changes.size(); i++) {
            TileChange change = changes.get(i);
            if (change.type == TileChange.Type.NEW) continue;

            int[] fromCoords = {change.fromRow, change.fromCol};
            int[] toCoords = {change.toRow, change.toCol};

            if (mirror) {
                fromCoords = untransformCoordsByMirror(fromCoords);
                toCoords = untransformCoordsByMirror(toCoords);
            }
            if (rotations != 0) {
                fromCoords = untransformCoordsByRotation(fromCoords, rotations);
                toCoords = untransformCoordsByRotation(toCoords, rotations);
            }

            TileChange transformedChange = new TileChange(
                    fromCoords[0], fromCoords[1], toCoords[0], toCoords[1],
                    change.oldValue, change.newValue, change.type
            );
            changes.set(i, transformedChange);
        }

        if (!boardsEqual(preMove, board)) {
            TileChange newTile = addNewTile();
            if (newTile != null) changes.add(newTile);
        }

        return changes;
    }

    /**
     * Slide and merge tiles in a single row (assumes row is in LEFT orientation).
     */
    private List<TileChange> slideAndMergeRow(int rowIndex) {
        List<TileChange> changes = new ArrayList<>();
        int[] temp = new int[SIZE];
        int pos = 0;

        // Phase 1: Slide non-zero tiles left and track their original positions
        int[] tempOriginCol = new int[SIZE];
        for (int j = 0; j < SIZE; j++) {
            if (board[rowIndex][j] != 0) {
                temp[pos] = board[rowIndex][j];
                tempOriginCol[pos] = j;
                pos++;
            }
        }

        // Phase 2: Merge adjacent equal tiles
        boolean[] merged = new boolean[SIZE];
        for (int j = 0; j < SIZE - 1; j++) {
            if (temp[j] != 0 && temp[j] == temp[j + 1]) {
                temp[j] *= 2;
                score += temp[j];
                temp[j + 1] = 0;
                merged[j] = true;
            }
        }

        // Phase 3: Slide again to close gaps and track final positions
        int[] finalRow = new int[SIZE];
        int[] finalOriginCol = new int[SIZE];
        boolean[] finalMerged = new boolean[SIZE];
        pos = 0;
        for (int j = 0; j < SIZE; j++) {
            if (temp[j] != 0) {
                finalRow[pos] = temp[j];
                finalOriginCol[pos] = tempOriginCol[j];
                finalMerged[pos] = merged[j];
                pos++;
            }
        }

        // Phase 4: Generate TileChange objects
        for (int targetCol = 0; targetCol < SIZE; targetCol++) {
            if (finalRow[targetCol] == 0) break;

            int oldValue = board[rowIndex][finalOriginCol[targetCol]];
            int newValue = finalRow[targetCol];
            int fromCol = finalOriginCol[targetCol];

            TileChange.Type type;
            if (finalMerged[targetCol]) {
                type = TileChange.Type.MERGE;
            } else if (fromCol != targetCol) {
                type = TileChange.Type.MOVE;
            } else {
                continue;
            }

            changes.add(new TileChange(rowIndex, fromCol, rowIndex, targetCol, oldValue, newValue, type));
        }

        System.arraycopy(finalRow, 0, board[rowIndex], 0, SIZE);

        return changes;
    }


    // ============ BOARD TRANSFORMATIONS ============

    /**
     * Mirror all rows horizontally (flip left-right).
     */
    private void mirrorRows() {
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE / 2; j++) {
                int t = board[i][j];
                board[i][j] = board[i][SIZE - 1 - j];
                board[i][SIZE - 1 - j] = t;
            }
    }

    /**
     * Rotate board 90 degrees clockwise, multiple times.
     */
    private void rotateBoard(int times) {
        for (int t = 0; t < times; t++) {
            int[][] rotated = new int[SIZE][SIZE];
            for (int i = 0; i < SIZE; i++)
                for (int j = 0; j < SIZE; j++)
                    rotated[j][SIZE - 1 - i] = board[i][j];
            for (int i = 0; i < SIZE; i++)
                System.arraycopy(rotated[i], 0, board[i], 0, SIZE);
        }
    }

    // ============ UTILITIES ============

    /**
     * Create a deep copy of the board.
     */
    private int[][] copyBoard(int[][] board) {
        int[][] copy = new int[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++)
            System.arraycopy(board[i], 0, copy[i], 0, SIZE);
        return copy;
    }

    /**
     * Check if two boards are identical.
     */
    private boolean boardsEqual(int[][] a, int[][] b) {
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                if (a[i][j] != b[i][j]) return false;
        return true;
    }

    /**
     * Reverse a rotation transformation to get original coordinates.
     */
    private int[] untransformCoordsByRotation(int[] coords, int rotations) {
        int row = coords[0];
        int col = coords[1];
        int inverseRotations = (4 - rotations) % 4;

        for (int i = 0; i < inverseRotations; i++) {
            int newRow = col;
            int newCol = SIZE - 1 - row;
            row = newRow;
            col = newCol;
        }
        return new int[]{row, col};
    }

    /**
     * Reverse a mirror transformation to get original coordinates.
     */
    private int[] untransformCoordsByMirror(int[] coords) {
        int row = coords[0];
        int col = SIZE - 1 - coords[1];
        return new int[]{row, col};
    }


    public enum Direction {UP, DOWN, LEFT, RIGHT}

    /**
     * Describes a tile change for UI animations.
     * Includes movement, merging, and new tile spawning.
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

        @SuppressLint("DefaultLocale")
        @NonNull
        @Override
        public String toString() {
            return String.format("TileChange(%s: (%d,%d) -> (%d,%d), %d -> %d)",
                    type, fromRow, fromCol, toRow, toCol, oldValue, newValue);
        }

        public enum Type {MOVE, MERGE, NEW}
    }
}