package com.cvut.fel.pjv.Utilities;

import com.cvut.fel.pjv.Controllers.GameController;
import com.cvut.fel.pjv.Models.Board;
import com.cvut.fel.pjv.Models.Piece;
import com.cvut.fel.pjv.Models.State;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The ValidationHelper class provides methods for validating moves and checking
 * game rules.
 * It assists in determining the legality of moves, checking for winners, and
 * handling various game states.
 * 
 * This class contains methods for validating the legality of moves, including
 * push and pull operations,
 * checking for winners based on specific game conditions, and managing the
 * state of the game.
 */
public class ValidationHelper {
    private Logger logger;

    private GameController game;
    private Board board;

    public boolean wasPush;
    public boolean canPull;

    public int figToMoveNewRow;
    public int figToMoveNewCol;
    public int powerOfFigToMove;

    public int posWhereIWasRow;
    public int posWhereIWasCol;
    public Figures figureWhoWasThere;

    public Piece lostPiece;
    public int whereLostPieceWasRow;
    public int whereLostPieceWasCol;

    private final int boardSize = 8;
    public Piece[][] boardCopy;

    public int boardID = 0;

    /**
     * Constructs a ValidationHelper object with the specified game controller and
     * board.
     * 
     * @param game  The GameController instance controlling the game.
     * @param board The Board instance representing the game board.
     */
    public ValidationHelper(GameController game, Board board) {
        if (game.logs) {
            this.logger = LoggerFactory.getLogger(ValidationHelper.class);
        }

        this.game = game;
        this.board = board;

        this.wasPush = false;
        this.canPull = false;

        this.boardCopy = new Piece[boardSize][boardSize];
    }

    /**
     * Checks if the move from the old position to the new position is valid.
     * This method verifies if the move adheres to the game's rules and
     * restrictions.
     * 
     * @param oldPos     The coordinates of the piece's old position.
     * @param newPos     The coordinates of the piece's new position.
     * @param whoseTurn  The color of the player whose turn it is.
     * @param turnsSoFar The number of turns elapsed in the current turn.
     * @param aiCheck    A flag indicating whether the move validation is for an AI
     *                   player.
     * @return True if the move is valid, otherwise false.
     */
    public boolean checkIfMoveValid(int[] oldPos, int[] newPos, Colors whoseTurn, int turnsSoFar, boolean aiCheck) {
        if (game.logs) {
            logger.debug("Checking if move is valid...");
        }

        if (turnsSoFar == 4) { // if we have already made 4 turns, we can't do another move
            return false;
        }

        int oldRow = oldPos[0];
        int oldCol = oldPos[1];

        int newRow = newPos[0];
        int newCol = newPos[1];

        // check if the same cell was clicked, the move is illegal
        if (oldRow == newRow && oldCol == newCol) {
            return false;
        }

        // check if somebody is already on that field, the move is illegal
        if (board.board[newRow][newCol] != null) {
            return false;
        }

        // if it is not the move to the front, back, left, right, move is illegal
        if (!((oldRow == newRow - 1 && oldCol == newCol)
                || (oldRow == newRow + 1 && oldCol == newCol)
                || (oldRow == newRow && oldCol == newCol + 1)
                || (oldRow == newRow && oldCol == newCol - 1))) {
            return false;
        }

        if (turnsSoFar == 0) {
            // if this is the start of the turn, we store how the board looked to check for
            // same position later

            for (int i = 0; i < boardSize; ++i) {
                for (int j = 0; j < boardSize; ++j) {
                    boardCopy[i][j] = board.board[i][j];
                }
            }
        }

        // check if you have pushed/pulled before and now have to finish it
        if (wasPush) {
            // check if one of the figures that could have done the push was chosen
            if (newRow != figToMoveNewRow
                    || newCol != figToMoveNewCol
                    || board.board[oldRow][oldCol].getType().ordinal() < powerOfFigToMove) {
                return false;
            }

            // we can't do the pull immediately after the push
            if (!aiCheck)
                canPull = false;
        }

        // check what color is the figure that was clicked
        Colors figColor = board.returnPieceColorAtPosition(oldRow, oldCol);

        // if it is not the current color, the player is trying to push/pull
        if (whoseTurn != figColor) {
            Figures enemyFigureType = board.board[oldRow][oldCol].getType();

            // first we assume the pull
            if (newRow == posWhereIWasRow
                    && newCol == posWhereIWasCol
                    && enemyFigureType.ordinal() < figureWhoWasThere.ordinal()
                    && canPull) {
                if (!aiCheck)
                    canPull = false;
                return true;
            }

            // now we check for push

            // if this is the last turn, push can't be initiated
            if (turnsSoFar == 3) {
                return false;
            }

            int row = oldRow;
            int col = oldCol;

            // here we check if any of the figures in the proximity of enemy piece could
            // have done the push
            if ((row != 0 && board.board[row - 1][col] != null && board.board[row - 1][col].getColor() == whoseTurn
                    && board.board[row - 1][col].getType().ordinal() > enemyFigureType.ordinal()
                    && !this.freezing(row - 1, col, whoseTurn))
                    || (row != board.boardSize - 1 && board.board[row + 1][col] != null
                            && board.board[row + 1][col].getColor() == whoseTurn
                            && board.board[row + 1][col].getType().ordinal() > enemyFigureType.ordinal()
                            && !this.freezing(row + 1, col, whoseTurn))
                    || (col != 0 && board.board[row][col - 1] != null
                            && board.board[row][col - 1].getColor() == whoseTurn
                            && board.board[row][col - 1].getType().ordinal() > enemyFigureType.ordinal()
                            && !this.freezing(row, col - 1, whoseTurn))
                    || (col != board.boardSize - 1 && board.board[row][col + 1] != null
                            && board.board[row][col + 1].getColor() == whoseTurn
                            && board.board[row][col + 1].getType().ordinal() > enemyFigureType.ordinal()
                            && !this.freezing(row, col + 1, whoseTurn))) {
                if (!aiCheck) {
                    // we store the position where the pushed piece was and its power
                    wasPush = true;
                    figToMoveNewRow = oldRow;
                    figToMoveNewCol = oldCol;
                    powerOfFigToMove = Math.min(enemyFigureType.ordinal() + 1, Figures.ELEPHANT.ordinal());
                }

                return true;
            }

            return false;

            // it is current color, we check normal rules
        } else {
            // check what type is the figure that was clicked
            Figures figType = board.returnPieceTypeAtPosition(oldRow, oldCol);

            // if it is Rabbit and we are trying to move back, the move is illegal
            if (figType == Figures.RABBIT) {

                if (whoseTurn == Colors.SILVER && oldRow > newRow) {
                    return false;

                } else if (whoseTurn == Colors.GOLD && oldRow < newRow) {
                    return false;
                }
            }

            // check for freezing
            if (this.freezing(oldRow, oldCol, whoseTurn)) {
                return false;
            }

            // finally we store the position where the figure was and its power to
            // potentially make the pull
            posWhereIWasRow = oldRow;
            posWhereIWasCol = oldCol;
            figureWhoWasThere = board.board[oldRow][oldCol].getType();
            if (!wasPush) {
                if (!aiCheck)
                    canPull = true;
            }
        }

        // remove all flags
        if (!aiCheck)
            wasPush = false;
        lostPiece = null;

        return true;
    }

    /**
     * Checks if the specified position is frozen, if a stronger enemy piece is
     * nearby.
     * 
     * @param row       The row index of the position to check.
     * @param col       The column index of the position to check.
     * @param whoseTurn The color of the player whose turn it is.
     * @return True if the position is frozen, otherwise false.
     */
    private boolean freezing(int row, int col, Colors whoseTurn) {
        // first check if there is a friendly figure in the vicinity
        if ((row != 0 && board.board[row - 1][col] != null && board.board[row - 1][col].getColor() == whoseTurn)
                || (row != board.boardSize - 1 && board.board[row + 1][col] != null
                        && board.board[row + 1][col].getColor() == whoseTurn)
                || (col != 0 && board.board[row][col - 1] != null && board.board[row][col - 1].getColor() == whoseTurn)
                || (col != board.boardSize - 1 && board.board[row][col + 1] != null
                        && board.board[row][col + 1].getColor() == whoseTurn))

        {
            return false;
        }

        // if not, check if there is a stronger enemy piece in the vicinity
        Figures myType = board.board[row][col].getType();

        if ((row != 0 && board.board[row - 1][col] != null && board.board[row - 1][col].getColor() != whoseTurn
                && board.board[row - 1][col].getType().ordinal() > myType.ordinal())
                || (row != board.boardSize - 1 && board.board[row + 1][col] != null
                        && board.board[row + 1][col].getColor() != whoseTurn
                        && board.board[row + 1][col].getType().ordinal() > myType.ordinal())
                || (col != 0 && board.board[row][col - 1] != null && board.board[row][col - 1].getColor() != whoseTurn
                        && board.board[row][col - 1].getType().ordinal() > myType.ordinal())
                || (col != board.boardSize - 1 && board.board[row][col + 1] != null
                        && board.board[row][col + 1].getColor() != whoseTurn
                        && board.board[row][col + 1].getType().ordinal() > myType.ordinal())) {
            return true;
        }

        return false;
    }

    /**
     * Validates the end of a player's turn.
     * This method verifies if the current game state allows ending the player's
     * turn.
     * 
     * @return True if the turn can be ended, otherwise false.
     */
    public boolean validateEndTurn() {
        // first we check if the position is the same as at the start
        boolean boardIsTheSame = true;
        for (int i = 0; i < boardSize; ++i) {
            for (int j = 0; j < boardSize; ++j) {
                if (board.board[i][j] != boardCopy[i][j]) {
                    boardIsTheSame = false;
                    break;
                }
            }
        }

        if (game.logs) {
            logger.debug("Validating end of turn...");
            if (boardIsTheSame) {
                logger.debug("The position did not change!");
            }
        }

        if (boardIsTheSame) {
            // if it is, we inform the player, he has to step back
            JOptionPane.showMessageDialog(null, "The position did not change!", "Error",
                    JOptionPane.INFORMATION_MESSAGE);
            return false;
        }

        if (wasPush) {
            // if the push was not finished, we inform the player about it
            JOptionPane.showMessageDialog(null, "You have to finish push!", "Error",
                    JOptionPane.INFORMATION_MESSAGE);
            return false;
        }

        return true;
    }

    /**
     * Determines if there is a trapped piece on the game board.
     * 
     * @return A string representing the position of the trapped piece, or an empty
     *         string if no trap exists.
     */
    public String trap() {
        if (game.logs) {
            logger.debug("Checking for trap...");
        }

        // we check each permutataion of trap coordinates
        for (int i = 0; i < 2; ++i) {
            for (int j = 0; j < 2; ++j) {
                int row = board.trap[i];
                int col = board.trap[j];

                if (board.board[row][col] == null) {
                    continue;
                }

                Colors color = board.board[row][col].getColor();

                // if the figure is on trap square, but there is a friendly piece nearby
                if ((board.board[row - 1][col] != null && board.board[row - 1][col].getColor() == color)
                        || (board.board[row + 1][col] != null && board.board[row + 1][col].getColor() == color)
                        || (board.board[row][col - 1] != null && board.board[row][col - 1].getColor() == color)
                        || (board.board[row][col + 1] != null && board.board[row][col + 1].getColor() == color))

                {
                    continue;
                }

                // the figure has to be removed, we record its coordinates to add trap to
                // history
                String position = board.board[row][col].getNotation();
                position += (char) (97 + col);
                position += (boardSize - row) + "x ";

                // we also has to store it in case of step back
                lostPiece = board.board[row][col];
                whereLostPieceWasRow = row;
                whereLostPieceWasCol = col;

                // we remove the piece
                board.board[row][col] = null;

                return position;
            }
        }

        return "";
    }

    /**
     * Checks for a winner based on the current game state.
     * This method examines the board to determine if a winning condition has been
     * met by either player.
     * 
     * @param whoseTurn The color of the player whose turn it is.
     * @return The color of the winning player, or null if no winner has been
     *         determined yet.
     */
    public Colors checkWinner(Colors whoseTurn) {
        if (game.logs) {
            logger.debug("Checking for winner...");
        }

        // first check for gold rabbit in the eigth row
        for (int i = 0; i < boardSize; ++i) {
            if (board.board[0][i] != null && board.board[0][i].getType() == Figures.RABBIT
                    && board.board[0][i].getColor() == Colors.GOLD) {
                return Colors.GOLD;
            }
        }

        // now check for silver rabbit in the first row
        for (int i = 0; i < boardSize; ++i) {
            if (board.board[7][i] != null && board.board[7][i].getType() == Figures.RABBIT
                    && board.board[7][i].getColor() == Colors.SILVER) {
                return Colors.SILVER;
            }
        }

        // check if there are any rabbits left for both sides
        boolean silverRabbitsLeft = false;
        boolean goldRabbitsLeft = false;

        for (int i = 0; i < boardSize; ++i) {
            for (int j = 0; j < boardSize; ++j) {
                if (board.board[i][j] != null && board.board[i][j].getType() == Figures.RABBIT) {
                    if (board.board[i][j].getColor() == Colors.GOLD) {
                        goldRabbitsLeft = true;

                    } else {
                        silverRabbitsLeft = true;
                    }
                }

                if (silverRabbitsLeft && goldRabbitsLeft) {
                    break;
                }
            }
        }

        // no rabbits left, the winner is the current player
        if (!goldRabbitsLeft && !silverRabbitsLeft) {
            return whoseTurn;
        }

        // no silver rabbits left
        if (silverRabbitsLeft == false) {
            return Colors.GOLD;
        }

        // no gold rabbits left
        if (goldRabbitsLeft == false) {
            return Colors.SILVER;
        }

        // we also need to check immobilization rule

        boolean immobilized = true;

        for (int i = 0; i < boardSize; ++i) {
            for (int j = 0; j < boardSize; ++j) {
                if (board.board[i][j] == null)
                    continue;

                // we are looking for enemy figures here
                Figures figType = board.returnPieceTypeAtPosition(i, j);
                if (board.board[i][j].getColor() != whoseTurn) {
                    if (freezing(i, j, board.board[i][j].getColor()))
                        // if the figure is frozen, it is immobilized
                        continue;

                    // we check if any figure that is blocking this figure can be pushed
                    if ((i != 0
                            && board.board[i - 1][j] != null
                            && !canBePushed(i - 1, j, figType, board.board[i - 1][j].getType(),
                                    board.board[i - 1][j].getColor(), whoseTurn))
                            && (i != boardSize - 1
                                    && board.board[i + 1][j] != null
                                    && !canBePushed(i + 1, j, figType, board.board[i + 1][j].getType(),
                                            board.board[i + 1][j].getColor(), whoseTurn))
                            && (j != 0
                                    && board.board[i][j - 1] != null
                                    && !canBePushed(i, j - 1, figType, board.board[i][j - 1].getType(),
                                            board.board[i][j - 1].getColor(), whoseTurn))
                            && (j != boardSize - 1
                                    && board.board[i][j + 1] != null
                                    && !canBePushed(i, j + 1, figType, board.board[i][j + 1].getType(),
                                            board.board[i][j + 1].getColor(), whoseTurn))) {
                        continue;

                    } else {
                        // the piece that still can move was found
                        immobilized = false;
                        break;
                    }

                } else if (board.board[i][j].getColor() == whoseTurn) {
                    // if this is our piece, we do not consider it
                    continue;
                }
            }
        }

        if (immobilized) {
            return whoseTurn;
        }

        return null;
    }

    /**
     * Determines if a piece at the specified position can be pushed by an
     * opponent's piece.
     * 
     * @param row            The row index of the position to check.
     * @param col            The column index of the position to check.
     * @param myFig          The figure type of the player's piece.
     * @param opponentsFig   The figure type of the opponent's piece.
     * @param opponentsColor The color of the opponent's piece.
     * @param myColor        The color of the player's piece.
     * @return True if the piece can be pushed, otherwise false.
     */
    private boolean canBePushed(int row, int col, Figures myFig, Figures opponentsFig, Colors opponentsColor,
            Colors myColor) {
        if (opponentsColor == myColor)
            return false;
        if (opponentsFig.ordinal() >= myFig.ordinal())
            return false;
        if (row != 0 && board.board[row - 1][col] != null)
            return false;
        if (row != boardSize - 1 && board.board[row + 1][col] != null)
            return false;
        if (col != 0 && board.board[row][col - 1] != null)
            return false;
        if (col != boardSize - 1 && board.board[row][col + 1] != null)
            return false;
        return true;
    }

    /**
     * Reverts the validation helper to a previous game state.
     * This method restores the validation helper's state to a previously saved
     * state.
     * 
     * @param state The State object representing the previous game state to return
     *              to.
     */
    public void returnToState(State state) {
        if (game.logs) {
            logger.debug("Returning validator to previous state...");
        }

        canPull = state.canPull;
        wasPush = state.wasPush;
        figToMoveNewRow = state.figToMoveNewRow;
        figToMoveNewCol = state.figToMoveNewCol;
        posWhereIWasRow = state.posWhereIWasRow;
        posWhereIWasCol = state.posWhereIWasCol;
        powerOfFigToMove = state.powerOfFigToMove;
        lostPiece = state.lostPiece;
    }
}
