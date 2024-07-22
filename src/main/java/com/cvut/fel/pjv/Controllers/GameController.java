package com.cvut.fel.pjv.Controllers;

import com.cvut.fel.pjv.Models.Board;
import com.cvut.fel.pjv.Models.Piece;
import com.cvut.fel.pjv.Models.State;

import com.cvut.fel.pjv.Utilities.AI;
import com.cvut.fel.pjv.Utilities.Colors;
import com.cvut.fel.pjv.Utilities.ValidationHelper;
import com.cvut.fel.pjv.Utilities.Figures;
import com.cvut.fel.pjv.Utilities.GameSerializer;

import com.cvut.fel.pjv.Views.BoardView;
import com.cvut.fel.pjv.Views.MainMenuView;
import com.cvut.fel.pjv.Views.NewGameView;
import com.cvut.fel.pjv.Views.ControlsView;

import java.util.List;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * The GameController class manages the state and flow of the game. It handles
 * interactions between the model,
 * view, and utility classes, processes player inputs, controls game timing, and
 * manages the game state.
 */
public class GameController {
    private Logger logger;

    // views
    public BoardView boardView;
    public Board board;

    public ControlsView controlsView;

    public MainMenuView mainMenu;
    public NewGameView newGame;

    // helper classes
    public ValidationHelper validate;
    public GameSerializer serializer;

    // turn counters and switch
    public Colors whoseTurn = Colors.GOLD;
    public int turnCounter = 0;
    public int totalGoldTurnCount = 2;
    public int totalSilverTurnCount = 2;

    // timer info
    private Thread gameTimer;
    public boolean running = false;
    public int reserveGold = 50000;
    public int reserveSilver = 50000;
    public int timeForTurn = 10000;

    // AI utilities
    public AI ai;
    public boolean vsAI = false;
    private Random random;

    private final int boardSize = 8;

    // step back utilities
    private List<State> statesDuringTurn;
    private StringBuilder turn;

    public boolean logs;

    /**
     * Initializes the GameController with logging options.
     * Sets up the main menu, new game view, board view, and controls view.
     * Initializes the board, validation helper, serializer, and AI.
     * Starts the game timer thread.
     *
     * @param logs indicates whether logging is enabled.
     */
    public GameController(boolean logs) {
        this.logs = logs;

        if (logs) {
            this.logger = LoggerFactory.getLogger(GameController.class);
        }

        this.mainMenu = new MainMenuView(this);
        this.newGame = new NewGameView(this);

        this.boardView = new BoardView(this);
        this.board = new Board();
        this.controlsView = new ControlsView(this);

        this.random = new Random();

        this.validate = new ValidationHelper(this, board);
        this.serializer = new GameSerializer(this, board, controlsView, boardView);

        this.ai = new AI(this, board, Colors.SILVER);

        this.turn = new StringBuilder();

        this.statesDuringTurn = new ArrayList<>();

        // game timer thread, always runs and tries to launch the function
        this.gameTimer = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1);
                } catch (Exception e) {
                    if (logs) {
                        logger.error("Thread interrupted", e);
                    }
                }

                handleTime();
            }
        });

        // start timer thread
        gameTimer.start();

        if (logs) {
            logger.info("GameController initialized with logging enabled.");
        }
    }

    /**
     * Starts the game by displaying the main menu.
     */
    public void start() {
        if (logs) {
            logger.info("Starting the game...");
        }
        mainMenu.setVisible(true);
    }

    /**
     * Begins a new game by setting up the board
     * and initializing the game state.
     */
    public void beginGame() {
        if (logs) {
            logger.info("Beginning new game...");
        }
        boardView.areFiguresSet = true;
        // display initial time
        updateTime(timeForTurn / 1000);

        // start building initial gold position string for history
        StringBuilder goldPos = new StringBuilder();
        goldPos.append("1g ");
        for (int i = boardSize - 1; i > 5; --i) {
            for (int j = 0; j < boardSize; ++j) {
                // append each position after converting to Arimaa notation
                String position = board.board[i][j].getNotation();
                position += (char) ('a' + j);
                position += (boardSize - i) + " ";
                goldPos.append(position);
            }
        }
        serializer.appendToHistory(goldPos.toString());
        goldPos.append("\n");
        // add to history
        controlsView.addToHistory(goldPos.toString());

        // repeat for silver
        StringBuilder silverPos = new StringBuilder();
        silverPos.append("1s ");
        for (int i = 1; i >= 0; --i) {
            for (int j = 0; j < boardSize; ++j) {
                String position = board.board[i][j].getNotation();
                position += (char) ('a' + j);
                position += (boardSize - i) + " ";
                silverPos.append(position);
            }
        }
        serializer.appendToHistory(silverPos.toString());
        silverPos.append("\n");
        controlsView.addToHistory(silverPos.toString());

        // add 2g before starting, because later we won't be able to
        controlsView.addToHistory("2g ");
        turn.append("2g ");

        // start game timer
        running = true;

        if (logs) {
            logger.info("New game setup completed.");
        }
    }

    /**
     * Creates and initializes the board,
     * either for a new game or from a loaded state.
     *
     * @param loading indicates if the board is being created from a loaded state.
     */
    public void createBoard(boolean loading) {
        // unless we are loading the game, we call method to put figures in initial
        // position
        if (!loading) {
            board.initBoard();
            boardView.initBoard();
        }

        // display the board
        boardView.setVisible(true);
        boardView.updateBoard();
        controlsView.setVisible(true);

        if (logs) {
            logger.info("Board created and displayed.");
        }
    }

    /**
     * Updates the display of the
     * remaining time for the current player's turn.
     *
     * @param left the time left in seconds.
     */
    private void updateTime(int left) {
        // timer calls this every second to update display of time
        controlsView.updateTime(left, whoseTurn);
        if (logs) {
            logger.debug("Updated time: {} seconds left for {}", left, whoseTurn);
        }
    }

    /**
     * Validates a player's turn based on the current game state and rules.
     *
     * @param oldPos    the starting position of the piece.
     * @param newPos    the ending position of the piece.
     * @param whoseTurn the color of the player making the move.
     * @param aiCheck   indicates if the validation is for an AI move.
     * @return true if the move is valid, false otherwise.
     */
    public boolean validateTurnNormal(int[] oldPos, int[] newPos, Colors whoseTurn, boolean aiCheck) {
        // we call validator to check the turn
        boolean valid = validate.checkIfMoveValid(oldPos, newPos, whoseTurn, turnCounter, aiCheck);
        if (logs) {
            logger.debug("Turn validation for move from {} to {}: {}", oldPos, newPos, valid);
        }
        return valid;
    }

    /**
     * Returns the type of piece at a given position on the board.
     *
     * @param row the row index.
     * @param col the column index.
     * @return the type of the piece.
     */
    public Figures getPieceTypeAtPosition(int row, int col) {
        return board.returnPieceTypeAtPosition(row, col);
    }

    /**
     * Returns the color of the piece at a given position on the board.
     *
     * @param row the row index.
     * @param col the column index.
     * @return the color of the piece.
     */
    public Colors getPieceColorAtPosition(int row, int col) {
        return board.returnPieceColorAtPosition(row, col);
    }

    /**
     * Finishes the current player's turn,
     * updates the game state, and checks for a winner.
     */
    public void finishPlayerTurn() {
        // first, we check if at least one turn was made, then if the finish is valid,
        // i.e. no unfinished pushes, etc.

        if (turnCounter >= 1 && validate.validateEndTurn()) {
            if (logs) {
                logger.info("Finishing player turn for {}", whoseTurn);
            }

            // we check if somebody won the game
            Colors winner = validate.checkWinner(whoseTurn);
            if (winner != null) {
                // we switch current turn to winner and stop the timer, then call the win
                // handler

                whoseTurn = winner;
                running = false;
                handleWin();
                return;
            }

            // switching turn logic

            if (whoseTurn == Colors.GOLD) {
                reserveGold += timeForTurn; // time left from normal turn time
                totalGoldTurnCount += 1;
                whoseTurn = Colors.SILVER;
                serializer.appendToHistory(turn.toString()); // add turn to history
                controlsView.addToHistory("\n"); // and write it out
                turn.setLength(0); // remove eveything from turn builder
                controlsView.addToHistory(totalSilverTurnCount + "s "); // prepend another turn

            } else {
                reserveSilver += timeForTurn;
                totalSilverTurnCount += 1;
                whoseTurn = Colors.GOLD;
                serializer.appendToHistory(turn.toString());
                controlsView.addToHistory("\n");
                turn.setLength(0);
                controlsView.addToHistory(totalGoldTurnCount + "g ");
            }

            // reset timer and turn counter
            turnCounter = 0;
            timeForTurn = 10000;
            statesDuringTurn.clear(); // we don't need to step back anymore

            if (whoseTurn == Colors.SILVER && vsAI) {
                // if the game is vs AI, we need to handle its turn
                handleAITurn();
            }

            if (logs) {
                logger.info("Turn finished. Next turn for {}", whoseTurn);
            }
        }
    }

    /**
     * Sets the piece type at a given position on the board.
     * Uses validator to check the legality of the move.
     * Saves the state for potential step back.
     *
     * @param oldRow the starting row index.
     * @param oldCol the starting column index.
     * @param newRow the ending row index.
     * @param newCol the ending column index.
     */
    public void setPieceTypeAtPosition(int oldRow, int oldCol, int newRow, int newCol) {
        // check if we are placing the figures at the start
        if (!boardView.areFiguresSet) {
            if (board.returnPieceColorAtPosition(oldRow, oldCol) == board.returnPieceColorAtPosition(newRow, newCol)) {
                // if color is the same, we do the swap
                Piece temp = board.board[newRow][newCol];
                board.board[newRow][newCol] = board.board[oldRow][oldCol];
                board.board[oldRow][oldCol] = temp;
                boardView.updateBoard();
            }

        } else {
            // we are playing the game

            // record position and save state for potential step back
            int[] oldPos = new int[] { oldRow, oldCol };
            int[] newPost = new int[] { newRow, newCol };

            State currTurnState = new State();
            currTurnState.oldRow = oldRow;
            currTurnState.oldCol = oldCol;
            currTurnState.newRow = newRow;
            currTurnState.newCol = newCol;
            currTurnState.copyValidator(validate);

            // if we pass validator, we do the turn
            if (validateTurnNormal(oldPos, newPost, whoseTurn, false)) {
                String position = board.board[oldRow][oldCol].getNotation();

                board.board[newRow][newCol] = board.board[oldRow][oldCol];
                board.board[oldRow][oldCol] = null;

                // preappend turn counter + color to history
                if (turnCounter == 0) {
                    if (whoseTurn == Colors.GOLD) {
                        turn.append(totalGoldTurnCount + "g ");

                    } else {
                        turn.append(totalSilverTurnCount + "s ");
                    }
                }

                // record turn in Arimaa notation
                position += (char) ('a' + oldCol);
                position += (boardSize - oldRow);
                if (oldRow > newRow) {
                    position += "n ";
                } else if (oldRow < newRow) {
                    position += "s ";
                } else if (oldCol > newCol) {
                    position += "w ";
                } else if (oldCol < newCol) {
                    position += "e ";
                }
                turn.append(position);
                controlsView.addToHistory(position);

                // check trap, record lost piece in state if it exists
                String trap = validate.trap();
                currTurnState.lostPiece = validate.lostPiece;
                if (currTurnState.lostPiece != null) {
                    currTurnState.whereLostPieceWasRow = validate.whereLostPieceWasRow;
                    currTurnState.whereLostPieceWasCol = validate.whereLostPieceWasCol;
                }

                // add to states that we can return to
                statesDuringTurn.add(currTurnState);

                // add trap to history
                turn.append(trap);
                controlsView.addToHistory(trap);

                boardView.updateBoard();

                turnCounter++;

                if (logs) {
                    logger.debug("Piece moved from ({}, {}) to ({}, {}). Current turn count: {}", oldRow, oldCol,
                            newRow, newCol, turnCounter);
                }
            }

            // if logger is on, we record the current state of the board
            // from Board class
            if (logs) {
                logger.debug("Board state after move:");
                StringBuilder boardOutput = new StringBuilder();
                for (int i = 0; i < boardSize; ++i) {
                    for (int j = 0; j < boardSize; ++j) {
                        if (board.board[i][j] == null) {
                            boardOutput.append("xx ");
                        } else {
                            boardOutput.append(board.board[i][j].getColor().ordinal())
                                    .append(board.board[i][j].getType().ordinal()).append(" ");
                        }
                    }
                    boardOutput.append("\n");
                }
                logger.debug("\n" + boardOutput.toString());
            }
        }
    }

    /**
     * Handles the winning scenario,
     * updating the view and resetting the game state.
     */
    public void handleWin() {
        if (logs) {
            logger.info("Player {} won the game!", whoseTurn);
        }
        // each view has to handle the win, we also reset board and history
        controlsView.handleWin(whoseTurn);
        boardView.handeEndGame();
        board.clearBoard();
        serializer.clearHistory();

        // set all the parameters to initial values
        reserveGold = 50000;
        reserveSilver = 50000;
        timeForTurn = 10000;
        whoseTurn = Colors.GOLD;
        turnCounter = 0;
        totalGoldTurnCount = 2;
        totalSilverTurnCount = 2;
        vsAI = false;
        running = false;
    }

    /**
     * Sets the game mode to player vs AI.
     */
    public void setVsAI() {
        vsAI = true;
        if (logs) {
            logger.info("Game mode set to player vs AI.");
        }
    }

    /**
     * Handles the AI's turn by making a
     * random number of moves and validating the end of the turn.
     */
    public void handleAITurn() {
        if (logs) {
            logger.info("AI is making its turn...");
        }
        // we get the random number of moves for AI
        int numberOfMoves = random.nextInt(4) + 1;
        for (int i = 0; i < numberOfMoves; ++i) {
            // AI makes its turn

            ai.makeTurn();
        }

        // here we assume AI did not finish the push but tries to finish the turn
        if (!validate.validateEndTurn()) {
            // we force him to finish the push

            ai.makeTurn();
        }

        finishPlayerTurn();
    }

    /**
     * Handles the game timing, updating
     * the time left for the current player's turn
     * and managing turn transitions.
     */
    public void handleTime() {
        if (!running) {
            // we simply return and do not handle timer logic
            return;
        }

        LocalTime startTime = LocalTime.now();

        while (true) {
            // until the second has passed, we sit in infinite loop
            LocalTime currentTime = LocalTime.now();
            Duration elapsed = Duration.between(startTime, currentTime);
            if (elapsed.getSeconds() >= 1) {
                break;
            }
        }

        if (timeForTurn != 0) {
            // first we check if there is normal time left and update it
            timeForTurn -= 1000;
            updateTime(timeForTurn / 1000);

        } else {
            // no normal time left, we use reserve time
            if (whoseTurn == Colors.GOLD) {
                reserveGold -= 1000;
                updateTime(reserveGold / 1000);
                if (reserveGold == 0) {
                    // if the reserve time is zero, the player is lost, we handle the win
                    whoseTurn = Colors.SILVER;
                    handleWin();
                }

            } else {
                reserveSilver -= 1000;
                updateTime(reserveSilver / 1000);
                if (reserveSilver == 0) {
                    whoseTurn = Colors.GOLD;
                    handleWin();
                }
            }
        }
    }

    /**
     * Steps back one move in the current turn,
     * reverting the game state to the previous state.
     */
    public void stepBack() {
        if (turnCounter == 0) {
            // if we are at the start of the turn, we simply return

            if (logs) {
                logger.warn("Cannot step back. No moves made yet in the current turn.");
            }
            return;
        }

        // we get the latest state

        State previousState = statesDuringTurn.removeLast();

        // if the piece was lost during current turn, we need to restore it
        if (previousState.lostPiece != null) {
            board.board[previousState.whereLostPieceWasRow][previousState.whereLostPieceWasCol] = previousState.lostPiece;
            previousState.lostPiece = null;
        }

        // we return the figure to the previous position
        board.board[previousState.oldRow][previousState.oldCol] = board.board[previousState.newRow][previousState.newCol];
        board.board[previousState.newRow][previousState.newCol] = null;

        // validator has to return to previous state as well, in order to force push if
        // necessary
        validate.returnToState(previousState);
        boardView.updateBoard();
        controlsView.removeFromHistory();

        // decrease turn counter
        turnCounter--;

        if (logs) {
            logger.info("Stepped back one move. Current turn counter: {}", turnCounter);
        }
    }
}
