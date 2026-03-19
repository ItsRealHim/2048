package com.example.myapplication;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameModel {

    private static final int size = 4;
    private final int[][] board;
    private final Random random = new Random();
    private int score;

    public GameModel() {
        board = new int[size][size];
        score = 0;
    }

    public int[][] getBoard() {
        return board;
    }

    public int getScore() {
        return score;
    }

    /**
     * Resets the game to its initial state.
     * Clears the board, resets the score, and adds two new tiles.
     */
    public void startNewGame() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                board[i][j] = 0;
            }
        }
        score = 0;
        addNewTile();
        addNewTile();
    }

    /**
     * Adds a new tile (either a 2 or a size) to a random empty cell on the board.
     */
    public void addNewTile() {
        List<int[]> emptyCells = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board[i][j] == 0) {
                    emptyCells.add(new int[]{i, j});
                }
            }
        }

        if (!emptyCells.isEmpty()) {
            int[] cell = emptyCells.get(random.nextInt(emptyCells.size()));
            int row = cell[0];
            int col = cell[1];
            // 90% chance for a '2', 10% chance for a 'size'
            board[row][col] = random.nextDouble() < 0.9 ? 2 : 4;
        }
    }

    /**
     * Handles the entire swipe process: slide, merge, and slide again.
     *
     * @param direction The direction of the swipe ("UP", "DOWN", "LEFT", "RIGHT").
     * @return true if any tiles moved or merged, false otherwise.
     */
    public boolean handleSwipe(Direction direction) {
        boolean moved = false;

        // Create a copy of the board to check for changes later
        int[][] preMoveBoard = new int[size][size];
        for (int i = 0; i < size; i++) {
            System.arraycopy(board[i], 0, preMoveBoard[i], 0, size);
        }

        // Determine required transformations
        boolean needRotate=false;
        boolean needMirror=false;
        if(direction == Direction.UP){
            needRotate = true;
            needMirror = true;
        }
        if(direction == Direction.DOWN){
            needRotate = true;
        }
        if(direction == Direction.RIGHT){
            needMirror = true;
        }

        // Apply transformations
        if (needRotate) {
            rotateBoard(1); // 90° clockwise
        }
        if (needMirror) {
            mirrorRows();
        }

        // Main logic: slide and merge (assumes LEFT move)
        slideAndMerge();

        // Undo transformations (reverse order!)
        if (needMirror) {
            mirrorRows();
        }
        if (needRotate) {
            rotateBoard(3); // 270° to restore original orientation
        }

        // Check if the board has changed
        for (int i = 0; i < size && !moved; i++) {
            for (int j = 0; j < size; j++) {
                if (board[i][j] != preMoveBoard[i][j]) {
                    moved = true;
                    break;
                }
            }
        }

        // If a move was successful, add a new tile
        if (moved) {
            addNewTile();
        }

        return moved;
    }

    /**
     * Slides all tiles to the left and merges adjacent identical tiles.
     */
    private void slideAndMerge() {
        for (int i = 0; i < size; i++) {
            // 1. Slide all non-zero tiles to the left
            int[] tempRow = new int[size];
            int current = 0;
            for (int j = 0; j < size; j++) {
                if (board[i][j] != 0) {
                    tempRow[current++] = board[i][j];
                }
            }

            // 2. Merge adjacent tiles
            for (int j = 0; j < size - 1; j++) {
                if (tempRow[j] != 0 && tempRow[j] == tempRow[j + 1]) {
                    tempRow[j] *= 2;
                    score += tempRow[j]; // Update score
                    tempRow[j + 1] = 0; // The second tile is consumed
                }
            }

            // 3. Slide again to close gaps created by merging
            int[] finalRow = new int[size];
            current = 0;
            for (int j = 0; j < size; j++) {
                if (tempRow[j] != 0) {
                    finalRow[current++] = tempRow[j];
                }
            }

            // size. Update the original board row
            System.arraycopy(finalRow, 0, board[i], 0, size);
        }
    }

    /**
     * mirrors the elements in each row of the board.
     * with the middle being the axis of symmetry.
     * For example, in a 4x4 board, the first and fourth elements of each row are swapped, as are the second and third elements.
     */
    private void mirrorRows() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size / 2; j++) {
                int temp = board[i][j];
                board[i][j] = board[i][size - 1 - j];
                board[i][size - 1 - j] = temp;
            }
        }
    }

    /**
     * Rotates the board 90 degrees clockwise a specified number of times.
     * Used to handle UP and DOWN swipes.
     *
     * @param times Number of 90-degree rotations.
     */
    private void rotateBoard(int times) {
        for (int t = 0; t < times; t++) {
            int[][] rotatedBoard = new int[size][size];
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    rotatedBoard[j][size - 1 - i] = board[i][j];
                }
            }
            // Copy rotated board back to original
            for (int i = 0; i < size; i++) {
                System.arraycopy(rotatedBoard[i], 0, board[i], 0, size);
            }
        }
    }

    public boolean isGameOver() {
        // Check for empty cell (move still possible)
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board[i][j] == 0) {
                    return false;
                }
            }
        }

        // Check horizontal merges
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size - 1; j++) {
                if (board[i][j] == board[i][j + 1]) {
                    return false;
                }
            }
        }

        // Check vertical merges
        for (int j = 0; j < size; j++) {
            for (int i = 0; i < size - 1; i++) {
                if (board[i][j] == board[i + 1][j]) {
                    return false;
                }
            }
        }

        // No moves left
        return true;
    }

    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }
}