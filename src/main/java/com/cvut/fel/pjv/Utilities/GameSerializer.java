package com.cvut.fel.pjv.Utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.cvut.fel.pjv.Controllers.GameController;
import com.cvut.fel.pjv.Models.Board;
import com.cvut.fel.pjv.Models.Piece;
import com.cvut.fel.pjv.Views.BoardView;
import com.cvut.fel.pjv.Views.ControlsView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The GameSerializer class provides methods for loading and saving game states.
 * It handles serialization and deserialization of game data.
 */
public class GameSerializer {
    private Logger logger;

    private GameController game;
    public Board board;
    private ControlsView controlsView;
    private BoardView boardView;

    private final int boardSize = 8;

    public List<String> history; // list to store all the turns in the Arimaa notation

    /**
     * Constructs a GameSerializer object with the specified game components.
     * 
     * @param game         The GameController instance controlling the game.
     * @param board        The Board instance representing the game board.
     * @param controlsView The ControlsView instance for displaying game controls.
     * @param boardView    The BoardView instance for displaying the game board.
     */
    public GameSerializer(GameController game, Board board, ControlsView controlsView, BoardView boardView) {
        if (game.logs) {
            this.logger = LoggerFactory.getLogger(GameSerializer.class);
        }

        this.game = game;
        this.board = board;
        this.controlsView = controlsView;
        this.boardView = boardView;
        this.history = new ArrayList<>();
    }

    /**
     * Loads a game state from a specified file.
     * 
     * @param save The file containing the saved game state.
     */
    public void loadGame(File save) {
        try {
            if (game.logs) {
                logger.debug("Loading game from file: {}", save.getName());
            }

            // we are loading the game
            controlsView.changeControls();

            // we do not need to set figures in this case
            boardView.areFiguresSet = true;
            history.clear();

            // initialize the boardView
            boardView.initBoard();

            Scanner scanner = new Scanner(save);

            // first we read initial positions of figures and add them to history
            String goldPosition = scanner.nextLine();
            controlsView.addToHistory(goldPosition + "\n");
            appendToHistory(goldPosition);

            String silverPosition = scanner.nextLine();
            controlsView.addToHistory(silverPosition + "\n");
            appendToHistory(silverPosition);

            controlsView.addToHistory("2g ");

            // we split the position into tokens and place the figures accordingly
            String[] tokens = goldPosition.toString().split("\\s+");
            for (String token : tokens) {
                if (token.equals("1g"))
                    continue;
                int col = token.charAt(1) - 'a';
                int row = boardSize - (token.charAt(2) - '0');
                board.board[row][col] = new Piece(token.substring(0, 1));
            }

            // same for silver position
            tokens = silverPosition.toString().split("\\s+");
            for (String token : tokens) {
                if (token.equals("1s"))
                    continue;
                int col = token.charAt(1) - 'a';
                int row = boardSize - (token.charAt(2) - '0');
                board.board[row][col] = new Piece(token.substring(0, 1));
            }

            // now we start replaying the game until we reach the last position
            while (scanner.hasNextLine()) {
                String nextTurn = scanner.nextLine();

                if (game.logs) {
                    logger.debug("Loading turn: {}", nextTurn);
                }

                // if we encounter the AI flag, we turn on vsAI mode
                if (nextTurn.equals("AI")) {
                    game.setVsAI();
                    continue;
                }

                // if we encounter timer info, we set timer parameters
                if (nextTurn.contains("GOLD")) {
                    game.reserveGold = Integer.parseInt(nextTurn.substring("GOLD".length() + 1));
                    continue;
                }

                if (nextTurn.contains("SILVER")) {
                    game.reserveSilver = Integer.parseInt(nextTurn.substring("SILVER".length() + 1));
                    continue;
                }

                if (nextTurn.contains("CURRENT")) {
                    game.timeForTurn = Integer.parseInt(nextTurn.substring("CURRENT".length() + 1));
                    continue;
                }

                // we split the turn into tokens and make moves
                tokens = nextTurn.split("\\s+");
                for (String token : tokens) {
                    if (token.matches("^[0-9]+[sg]$"))
                        continue;
                    int oldCol = token.charAt(1) - 'a';
                    int oldRow = boardSize - (token.charAt(2) - '0');

                    int newCol = oldCol;
                    int newRow = oldRow;

                    // we convert the Arimaa directions into actual numbers
                    switch (token.charAt(3)) {
                        case 'n':
                            newRow -= 1;
                            break;
                        case 'e':
                            newCol += 1;
                            break;
                        case 's':
                            newRow += 1;
                            break;
                        case 'w':
                            newCol -= 1;
                            break;
                    }

                    // we do the turn
                    game.setPieceTypeAtPosition(oldRow, oldCol, newRow, newCol);
                }
                // once we finished reading one turn, we switch controller into another turn
                game.finishPlayerTurn();
            }

            // we also need to start the timer and show the board
            game.mainMenu.setVisible(false);
            game.running = true;
            game.createBoard(true);
            scanner.close();

        } catch (FileNotFoundException e) {
            logger.error("File not found: {}", e.getMessage());
        }
    }

    /**
     * Saves the current game state to a file with the specified filename.
     * 
     * @param filename The name of the file to save the game state to.
     * @param time     The remaining time for the current turn.
     * @throws IOException If an I/O error occurs while writing to the file.
     */
    public void saveGame(String filename, int time) throws IOException {
        if (filename.equals("")) {
            // if we received empty file name, we name it as today's date + time
            filename = Instant.now().toString();
        }

        FileWriter writer = new FileWriter("arimaa/saves/" + filename + ".txt");

        // we write each turn into file
        for (String turn : history) {
            writer.write(turn);
            writer.write("\n");
        }

        // we also provide information about the current timer parameters, as well as
        // whether the game was vs AI
        writer.write("CURRENT " + time + "\n");
        writer.write("GOLD " + game.reserveGold + "\n");

        if (game.vsAI) {
            writer.write("AI\n");
        } else {
            writer.write("SILVER " + game.reserveSilver + "\n");
        }

        writer.close();

        if (game.logs) {
            logger.debug("Game saved as: {}.txt", filename);
        }
    }

    /**
     * Appends a turn to the game history.
     * 
     * @param turn The turn to append to the history.
     */
    public void appendToHistory(String turn) {
        history.add(turn);
    }

    /**
     * Clears the game history.
     */
    public void clearHistory() {
        history.clear();
    }

    /**
     * Retrieves and removes the last turn from the game history.
     * 
     * @return The last turn in the history.
     */
    public String getLastTurn() {
        return history.removeLast();
    }
}
