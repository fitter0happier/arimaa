package com.cvut.fel.pjv.Views;

import com.cvut.fel.pjv.Controllers.GameController;
import com.cvut.fel.pjv.Utilities.Colors;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.io.IOException;

/**
 * The view responsible for displaying game controls and information, such as
 * time left, save options, step back functionality,
 * and handling user interactions related to game controls.
 * This class extends JFrame to create a graphical user interface.
 */
public class ControlsView extends JFrame {
    private boolean areFiguresSet = false;
    private boolean isGameFinished = false;

    private JLabel timeLeft;
    private JButton controls;
    private JButton save;
    private JButton stepBack;
    private JTextArea textArea;

    /**
     * Constructs a new ControlsView instance with the specified game controller.
     * 
     * @param game The GameController instance associated with this view.
     */
    public ControlsView(GameController game) {
        super.setTitle("Controls");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(300, 450);

        // we set the location relative to the board
        Point boardLocation = game.boardView.getLocation();
        int thisLocationX = 1100;
        int thisLocationY = boardLocation.y;
        this.setLocation(thisLocationX, thisLocationY);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 1));

        // text area to store the history
        this.textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        panel.add(scrollPane);

        // label to display remaining time
        this.timeLeft = new JLabel();
        timeLeft.setHorizontalAlignment(JLabel.CENTER);
        panel.add(timeLeft);

        // button to save the game
        this.save = new JButton("Save game");
        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // first we store the time before the button press
                    int timeBeforeSave = game.timeForTurn;

                    // we stop the timer
                    game.running = false;

                    // we register the user input for the name and try to save the game
                    String userInput = JOptionPane.showInputDialog("Name of save: ");
                    if (userInput != null)
                        game.serializer.saveGame(userInput, timeBeforeSave);
                    game.running = true;
                } catch (IOException e1) {
                    if (game.logs) {
                        System.err.println("File could not be opened");
                    }
                }
            }
        });
        save.setEnabled(false);
        panel.add(save);

        // button to cancel the last step
        this.stepBack = new JButton("Step back");
        stepBack.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                game.stepBack();
            }
        });
        stepBack.setEnabled(false);
        panel.add(stepBack);

        // button that either begins/restarts the game or finishes turn
        this.controls = new JButton("Is placing finished?");
        controls.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!areFiguresSet) {
                    // we finished placing, so we start the game
                    game.beginGame();
                    timeLeft.setVisible(true);
                    changeControls();

                } else if (isGameFinished) {
                    // we pressed restart

                    // we display main menu to start the game again
                    game.mainMenu.setVisible(true);
                    game.boardView.setVisible(false);

                    // we clear the history
                    textArea.setText("");

                    // we set every parameter to initial values
                    isGameFinished = false;
                    areFiguresSet = false;
                    controls.setText("Is placing finished?");
                    timeLeft.setVisible(false);
                    setVisible(false);

                } else {
                    // we finish the turn
                    game.finishPlayerTurn();
                }
            }
        });

        panel.add(controls);
        add(panel);
    }

    /**
     * Updates the displayed time left for the current player's turn.
     * 
     * @param left      The time remaining in seconds.
     * @param whoseTurn The color of the player whose turn it is.
     */
    public void updateTime(int left, Colors whoseTurn) {
        if (!timeLeft.isVisible()) {
            timeLeft.setVisible(true);
        }

        if (!isGameFinished) {
            // we convert seconds to minutes:seconds
            int minutes = left / 60;
            int seconds = left - (minutes * 60);

            String minutesText = minutes < 10 ? "0" + String.valueOf(minutes) : String.valueOf(minutes);
            String secondsText = seconds < 10 ? "0" + String.valueOf(seconds) : String.valueOf(seconds);

            // we add it to JLabel
            timeLeft.setText(whoseTurn + "\n" + minutesText + ":" + secondsText);
        }
    }

    /**
     * Handles the display of the winner and updates control buttons accordingly.
     * 
     * @param winner The color of the winning player.
     */
    public void handleWin(Colors winner) {
        // if the game is won, we display the winner and finish the game
        timeLeft.setText("The winner is " + winner + "!");
        controls.setText("Restart");
        save.setEnabled(false);
        stepBack.setEnabled(false);
        isGameFinished = true;
    }

    /**
     * Adds a turn to the history log displayed in the text area.
     * 
     * @param turn The string representation of the turn to be added.
     */
    public void addToHistory(String turn) {
        textArea.append(turn);
    }

    /**
     * Removes the last turn from the history log displayed in the text area.
     */
    public void removeFromHistory() {
        // we have to get the entire history
        String entireTurn = textArea.getText();
        String[] tokens = entireTurn.split("\\s+");
        textArea.setText("");

        // we split it into tokens and reconstruct it back excluding the last token
        for (int i = 0; i < tokens.length - 1; ++i) {
            // we also have to remove the trap if it has happend on the last turn
            if (i == tokens.length - 2 && tokens[i + 1].contains("x")) {
                break;
            }

            textArea.append(tokens[i] + " ");

            if (tokens[i + 1].matches("^[0-9]+[sg]$")) {
                textArea.append("\n");
            }
        }
    }

    /**
     * Changes the control buttons and enables/disables them based on game state.
     */
    public void changeControls() {
        // the game has started, we enable save and step back
        controls.setText("Finish turn");
        areFiguresSet = true;
        save.setEnabled(true);
        stepBack.setEnabled(true);
    }
}
