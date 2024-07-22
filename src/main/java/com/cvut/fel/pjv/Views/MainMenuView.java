package com.cvut.fel.pjv.Views;

import javax.swing.*;

import com.cvut.fel.pjv.Controllers.GameController;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;

/**
 * The view responsible for displaying the main menu of the Arimaa game,
 * providing options for starting a new game or loading a saved game.
 * This class extends JFrame to create a graphical user interface.
 */
public class MainMenuView extends JFrame {

    public GameController game;

    /**
     * Constructs a new MainMenuView instance with the specified game controller.
     * 
     * @param game The GameController instance associated with this view.
     */
    public MainMenuView(GameController game) {
        super("Arimaa Main Menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 400);
        setLocationRelativeTo(null);

        this.game = game;

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 1));

        JButton newGameButton = new JButton("New Game");
        JButton loadGameButton = new JButton("Load Game");

        // the button to start a new game
        newGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                game.newGame.setVisible(true);
                setVisible(false);
            }
        });

        // the button to load the game
        loadGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // we store the directory where saved games are located
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(new File("arimaa/saves/"));

                // we only allow single file to be chosen
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fileChooser.setMultiSelectionEnabled(false);

                // after the file is chosen, we try to load the game
                int option = fileChooser.showOpenDialog(null);
                if (option == JFileChooser.APPROVE_OPTION) {
                    File selectedSave = fileChooser.getSelectedFile();
                    game.serializer.loadGame(selectedSave);
                }
            }
        });

        panel.add(newGameButton);
        panel.add(loadGameButton);

        add(panel);
    }
}
