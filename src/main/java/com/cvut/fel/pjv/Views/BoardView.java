package com.cvut.fel.pjv.Views;

import com.cvut.fel.pjv.Controllers.GameController;
import com.cvut.fel.pjv.Utilities.Figures;
import com.cvut.fel.pjv.Utilities.Colors;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * The view responsible for displaying the Arimaa game board and handling user
 * interactions related to the board.
 * This class extends JFrame to create a graphical user interface.
 */
public class BoardView extends JFrame {
    private final int boardSize;
    private GameController game;

    // 2D array of JButtons representing pieces
    private JButton[][] pieceLabels;

    public boolean areFiguresSet;

    private JPanel panel;
    private Image backImg;

    private boolean firstClick;
    private int selectedRow;
    private int selectedCol;

    /**
     * Constructs a new BoardView instance with the specified game controller.
     * 
     * @param game The GameController instance associated with this view.
     */
    public BoardView(GameController game) {
        super("Arimaa Game Board");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(650, 650);
        this.setLocationRelativeTo(null);

        this.game = game;
        this.firstClick = true; // flag to check if the player has already chosen the figure
        this.boardSize = 8;
        this.areFiguresSet = false; // flag to check if the figures are already set
        this.pieceLabels = new JButton[boardSize][boardSize];

        // we get the image for background
        ImageIcon imageIcon = new ImageIcon(getClass().getResource("/PNG/BoardStoneBig.jpg"));
        backImg = imageIcon.getImage();

        // we create the panel with chosen background
        panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backImg != null) {
                    g.drawImage(backImg, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };

        panel.setLayout(new GridLayout(boardSize, boardSize));
        add(panel);
    }

    /**
     * Initializes the game board GUI components.
     */
    public void initBoard() {
        // clean up the panel
        panel.removeAll();

        // add JButtons for each square of the board
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                JButton pieceLabel = new JButton();
                pieceLabel.setOpaque(false);
                pieceLabel.setContentAreaFilled(false);
                pieceLabel.setBorderPainted(false);
                panel.add(pieceLabel);

                // add action that will handle the logic of choice
                pieceLabel.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (!areFiguresSet) {
                            // if figures are not set, we are trying to swap them
                            if (firstClick && pieceLabel.getIcon() != null) {
                                pieceSelected(pieceLabel);
                                firstClick = false;

                            } else {
                                // if the swap is finished, we are back to choosing figures
                                if (pieceSwapped(pieceLabel)) {
                                    firstClick = true;
                                }
                            }

                        } else {
                            // figures are set, so we are trying to make a move
                            if (firstClick && pieceLabel.getIcon() != null) {
                                pieceSelected(pieceLabel);
                                firstClick = false;

                            } else {
                                if (pieceMoved(pieceLabel)) {
                                    firstClick = true;

                                } else {
                                    // if the empty cell was chosen, we are still in the Figure Selected state
                                    pieceSelected(pieceLabel);
                                }
                            }
                        }
                    }
                });
                pieceLabels[row][col] = pieceLabel;
            }
        }

        this.setVisible(true);
    }

    /**
     * Retrieves the file path for the image corresponding to the given piece type.
     * 
     * @param pieceType The type of the game piece.
     * @return The file path for the image of the specified piece.
     */
    private String getImagePathForPiece(String pieceType) {
        String filepath = "/PNG/Figures/" + pieceType + ".png";
        return filepath;
    }

    /**
     * Updates the game board with the current state of the game.
     */
    public void updateBoard() {
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                Figures pieceType = game.getPieceTypeAtPosition(row, col);
                Colors pieceColor = game.getPieceColorAtPosition(row, col);
                StringBuilder piece = new StringBuilder();

                // if the figure exists on that cell, we start forming the string to find the
                // picture
                if (pieceType != null) {
                    piece.append(pieceColor.ordinal());
                    piece.append(pieceType.ordinal());
                }

                if (piece.length() > 0) {
                    // we search for file in the directory and add it to the JButton
                    String imagePath = getImagePathForPiece(piece.toString());
                    ImageIcon icon = new ImageIcon(getClass().getResource(imagePath));
                    pieceLabels[row][col].setIcon(icon);

                } else {
                    pieceLabels[row][col].setIcon(null);
                }
            }
        }
    }

    /**
     * Handles the selection of a game piece on the board.
     * 
     * @param pieceLabel The JButton representing the selected piece.
     */
    private void pieceSelected(JButton pieceLabel) {
        // we search for chosen piece and return its coordinates on the board
        for (int i = 0; i < boardSize; ++i) {
            for (int j = 0; j < boardSize; ++j) {
                if (pieceLabels[i][j] == pieceLabel) {
                    selectedRow = i;
                    selectedCol = j;
                    break;
                }
            }
        }
    }

    /**
     * Moves a selected game piece to the specified position on the board.
     * 
     * @param pieceLabel The JButton representing the destination position for the
     *                   piece.
     * @return True if the piece was successfully moved, otherwise false.
     */
    private boolean pieceMoved(JButton pieceLabel) {
        if (pieceLabel.getIcon() != null) { // if the click was on another figure, we do not consider it
            return false;
        }

        for (int i = 0; i < boardSize; ++i) {
            for (int j = 0; j < boardSize; ++j) {
                if (pieceLabels[i][j] == pieceLabel) {
                    // the position was found, so now we handle the logic of the turn
                    game.setPieceTypeAtPosition(selectedRow, selectedCol, i, j);
                    break;
                }
            }
        }

        return true;
    }

    /**
     * Swaps a selected game piece with another piece on the board.
     * 
     * @param pieceLabel The JButton representing the destination position for the
     *                   piece.
     * @return True if the pieces were successfully swapped, otherwise false.
     */
    private boolean pieceSwapped(JButton pieceLabel) {
        if (pieceLabel.getIcon() == null) { // if we are setting the figures and clicked the empty cell, we do not
                                            // consider it
            return false;
        }

        for (int i = 0; i < boardSize; ++i) {
            for (int j = 0; j < boardSize; ++j) {
                if (pieceLabels[i][j] == pieceLabel) {
                    // we handle the swap
                    game.setPieceTypeAtPosition(selectedRow, selectedCol, i, j);
                    break;
                }
            }
        }

        return true;
    }

    /**
     * Disables all game board components and indicates that the game has ended.
     */
    public void handeEndGame() {
        for (int i = 0; i < boardSize; ++i) {
            for (int j = 0; j < boardSize; ++j) {
                pieceLabels[i][j].setEnabled(false);
            }
        }
        areFiguresSet = false;
    }
}
