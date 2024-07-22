package com.cvut.fel.pjv.Models;

import com.cvut.fel.pjv.Utilities.Colors;
import com.cvut.fel.pjv.Utilities.Figures;

/**
 * The Board class represents the game board for a strategy game.
 * It holds the pieces on the board and provides methods to initialize, clear, and query the board state.
 */
public class Board {
    /**
     * The 2D array representing the board with pieces.
     */
    public Piece[][] board;

    /**
     * The size of the board, fixed at 8x8.
     */
    public final int boardSize = 8;

    /**
     * The trap positions on the board.
     */
    public int[] trap = {2, 5};

    /**
     * Constructs a new Board and initializes the board array.
     */
    public Board() {
        this.board = new Piece[boardSize][boardSize];
    }

    /**
     * Clears the board by setting all positions to null.
     */
    public void clearBoard() {
        for (int i = 0; i < boardSize; ++i) {
            for (int j = 0; j < boardSize; ++j) {
                board[i][j] = null;
            }
        }
    }

    /**
     * Initializes the board with a default setup of pieces.
     */
    public void initBoard() {
        
        //rabbits
        for (int i = 0; i < boardSize; ++i) {
            board[0][i] = new Piece(Figures.RABBIT, Colors.SILVER);
            board[boardSize - 1][i] = new Piece(Figures.RABBIT, Colors.GOLD);
        }

        //silver figures
        board[1][0] = board[1][boardSize - 1] = new Piece(Figures.DOG, Colors.SILVER);
        board[1][1] = board[1][boardSize - 2] = new Piece(Figures.HORSE, Colors.SILVER);
        board[1][2] = board[1][boardSize - 3] = new Piece(Figures.CAT, Colors.SILVER);
        board[1][3] = new Piece(Figures.CAMEL, Colors.SILVER);
        board[1][4] = new Piece(Figures.ELEPHANT, Colors.SILVER);

        //gold figures
        board[boardSize - 2][0] = board[boardSize - 2][boardSize - 1] = new Piece(Figures.DOG, Colors.GOLD);
        board[boardSize - 2][1] = board[boardSize - 2][boardSize - 2] = new Piece(Figures.HORSE, Colors.GOLD);
        board[boardSize - 2][2] = board[boardSize - 2][boardSize - 3] = new Piece(Figures.CAT, Colors.GOLD);
        board[boardSize - 2][3] = new Piece(Figures.ELEPHANT, Colors.GOLD);
        board[boardSize - 2][4] = new Piece(Figures.CAMEL, Colors.GOLD);
    }

    /**
     * Returns the type of piece at a specific position on the board.
     *
     * @param row the row index of the position.
     * @param col the column index of the position.
     * @return the type of the piece at the given position, or null if the position is empty.
     */
    public Figures returnPieceTypeAtPosition(int row, int col) {
        if (board[row][col] == null) {
            return null;
        }

        return board[row][col].getType();
    }

    /**
     * Returns the color of the piece at a specific position on the board.
     *
     * @param row the row index of the position.
     * @param col the column index of the position.
     * @return the color of the piece at the given position, or null if the position is empty.
     */
    public Colors returnPieceColorAtPosition(int row, int col) {
        if (board[row][col] == null) {
            return null;
        }

        return board[row][col].getColor();
    }
}
