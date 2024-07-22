package com.cvut.fel.pjv.Utilities;

import com.cvut.fel.pjv.Controllers.GameController;
import com.cvut.fel.pjv.Models.Board;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The AI class represents the artificial intelligence for the game.
 * It generates and makes moves based on the current game state.
 */
public class AI {
    private Logger logger;

    private GameController game;
    private Board board;
    private Colors color;

    private final int boardSize = 8;

    private Random random;

    /**
     * Constructs an AI instance.
     *
     * @param game  the GameController instance managing the game.
     * @param board the Board instance representing the game board.
     * @param color the color representing the AI player's pieces.
     */
    public AI(GameController game, Board board, Colors color) {
        if (game.logs) {
            this.logger = LoggerFactory.getLogger(AI.class);
        }

        this.game = game;
        this.board = board;
        this.color = color;
        this.random = new Random();
    }

    /**
     * Makes a turn for the AI by selecting a
     * random valid move from the list of generated moves.
     */
    public void makeTurn() {
        List<int[]> moves = generateMoves();
        if (game.logs && moves.isEmpty()) {
            logger.warn("No valid moves available for AI.");
            return;
        }
        // we get random move from list of possible moves and return it
        int randomIndex = random.nextInt(0, moves.size());
        int[] turn = moves.get(randomIndex);
        game.setPieceTypeAtPosition(turn[0], turn[1], turn[2], turn[3]);

        if (game.logs) {
            logger.debug("AI made a turn: {},{},{} -> {},{}", turn[0], turn[1], turn[2], turn[3]);
        }
    }

    /**
     * Generates a list of all possible valid moves for the AI.
     *
     * @return a list of valid moves, where each move is represented as an array of
     *         four integers:
     *         {rowFrom, colFrom, rowTo, colTo}.
     */
    private List<int[]> generateMoves() {
        // we create the list to store all potential moves

        List<int[]> moves = new ArrayList<>();

        // we now need to iterate over each possible combination of rows/cols
        for (int rowFrom = 0; rowFrom < boardSize; ++rowFrom) {
            for (int colFrom = 0; colFrom < boardSize; ++colFrom) {
                if (board.board[rowFrom][colFrom] != null) {
                    for (int rowTo = 0; rowTo < boardSize; ++rowTo) {
                        for (int colTo = 0; colTo < boardSize; ++colTo) {
                            int[] oldPos = new int[] { rowFrom, colFrom };
                            int[] newPos = new int[] { rowTo, colTo };
                            if (board.board[rowTo][colTo] == null
                                    && game.validateTurnNormal(oldPos, newPos, color, true)) {
                                // if the turn is legal, we add it to potential turns

                                moves.add(new int[] { rowFrom, colFrom, rowTo, colTo });
                            }

                        }
                    }
                }
            }
        }

        return moves;
    }
}
