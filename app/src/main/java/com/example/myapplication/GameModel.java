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

    public void startNewGame() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                board[i][j] = 0;
            }
        }
        score = 0;
        addNewTile();
        addNewTile();
    }

    /**
     * Adds a new tile (2 or 4) in a random empty cell.
     * Returns a TileChange representing the new tile, or null if board full.
     */
    public TileChange addNewTile() {
        List<int[]> emptyCells = new ArrayList<>();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (board[i][j] == 0) emptyCells.add(new int[]{i, j});
            }
        }
        if (emptyCells.isEmpty()) return null;

        int[] cell = emptyCells.get(random.nextInt(emptyCells.size()));
        int row = cell[0], col = cell[1];
        int value = random.nextDouble() < 0.9 ? 2 : 4;
        board[row][col] = value;

        return new TileChange(-1, -1, row, col, 0, value, TileChange.Type.NEW);
    }

    /**
     * Handles a swipe and returns a list of TileChanges (for animation).
     */
    public List<TileChange> handleSwipe(Direction direction) {
        boolean needRotate = false;
        boolean needMirror = false;
        switch (direction) {
            case UP: {
                needRotate = true;
                needMirror = true;
            }
            case DOWN:
                needRotate = true;
            case RIGHT:
                needMirror = true;
            case LEFT: { /* nothing */ }
        }

        // Apply transformations for uniform LEFT slide
        if (needRotate) rotateClockwise();
        if (needMirror) mirrorRows();

        List<TileChange> changes = new ArrayList<>();
        for (int i = 0; i < SIZE; i++) {
            changes.addAll(slideAndMergeRow(i));
        }

        // Undo transformations
        if (needMirror) mirrorRows();
        if (needRotate) rotateCounterClockwise();

        // Transform TileChange coordinates back to original orientation
        for (TileChange change : changes) {
            int[] from = reverseTransform(change.fromRow, change.fromCol, direction);
            int[] to = reverseTransform(change.toRow, change.toCol, direction);
            change.fromRow = from[0];
            change.fromCol = from[1];
            change.toRow = to[0];
            change.toCol = to[1];
        }

        // Add new tile if any move occurred
        if (!changes.isEmpty()) {
            TileChange newTile = addNewTile();
            if (newTile != null) changes.add(newTile);
        }

        return changes;
    }

    /**
     * Slide and merge a single row to the left.
     * Returns list of TileChanges (MOVE/MERGE).
     */
    private List<TileChange> slideAndMergeRow(int rowIndex) {
        List<TileChange> changes = new ArrayList<>();
        int[] tempRow = new int[SIZE];
        int pos = 0;

        // Slide non-zero tiles
        for (int j = 0; j < SIZE; j++) {
            if (board[rowIndex][j] != 0) tempRow[pos++] = board[rowIndex][j];
        }

        int[] mergedRow = new int[SIZE];
        int target = 0;
        for (int i = 0; i < SIZE; i++) {
            if (tempRow[i] == 0) continue;

            if (target > 0 && mergedRow[target - 1] == tempRow[i]) {
                int oldValue = mergedRow[target - 1];
                mergedRow[target - 1] *= 2;
                score += mergedRow[target - 1];

                changes.add(new TileChange(
                        rowIndex, i,
                        rowIndex, target - 1,
                        tempRow[i],
                        mergedRow[target - 1],
                        TileChange.Type.MERGE
                ));
            } else {
                if (i != target) {
                    changes.add(new TileChange(
                            rowIndex, i,
                            rowIndex, target,
                            tempRow[i],
                            tempRow[i],
                            TileChange.Type.MOVE
                    ));
                }
                mergedRow[target] = tempRow[i];
                target++;
            }
        }

        // Update the board
        System.arraycopy(mergedRow, 0, board[rowIndex], 0, SIZE);
        return changes;
    }

    /**
     * Mirror the board horizontally
     */
    private void mirrorRows() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE / 2; j++) {
                int temp = board[i][j];
                board[i][j] = board[i][SIZE - 1 - j];
                board[i][SIZE - 1 - j] = temp;
            }
        }
    }

    /**
     * Rotate the board 90° clockwise
     */
    private void rotateClockwise() {
        int[][] rotated = new int[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                rotated[j][SIZE - 1 - i] = board[i][j];
        for (int i = 0; i < SIZE; i++) System.arraycopy(rotated[i], 0, board[i], 0, SIZE);
    }

    /**
     * Rotate the board 90° counter-clockwise
     */
    private void rotateCounterClockwise() {
        int[][] rotated = new int[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                rotated[SIZE - 1 - j][i] = board[i][j];
        for (int i = 0; i < SIZE; i++) System.arraycopy(rotated[i], 0, board[i], 0, SIZE);
    }

    /**
     * Reverse transform coordinates from rotated/mirrored board to original
     */
    private int[] reverseTransform(int row, int col, Direction dir) {
        int r = row, c = col;
        switch (dir) {
            case UP: {
                r = col;
                c = SIZE - 1 - row;
                c = SIZE - 1 - c;
            } // rotate + mirror
            case DOWN: {
                int tmp = r;
                r = SIZE - 1 - c;
                c = tmp;
            }       // rotate back
            case RIGHT:
                c = SIZE - 1 - c;                                 // mirror
            case LEFT: { /* nothing */ }
        }
        return new int[]{r, c};
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
     * TileChange class to represent moves, merges, and new tiles
     */
    public static class TileChange {
        public final int oldValue, newValue;
        public final Type type;
        public int fromRow, fromCol;
        public int toRow, toCol;

        public TileChange(int fromRow, int fromCol, int toRow, int toCol,
                          int oldValue, int newValue, Type type) {
            this.fromRow = fromRow;
            this.fromCol = fromCol;
            this.toRow = toRow;
            this.toCol = toCol;
            this.oldValue = oldValue;
            this.newValue = newValue;
            this.type = type;
        }

        @Override
        public String toString() {
            return type + ": (" + fromRow + "," + fromCol + ") -> (" +
                    toRow + "," + toCol + "), " + oldValue + " -> " + newValue;
        }

        public enum Type {MOVE, MERGE, NEW}
    }
}