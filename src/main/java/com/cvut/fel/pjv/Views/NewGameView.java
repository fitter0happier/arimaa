package com.cvut.fel.pjv.Views;

import com.cvut.fel.pjv.Controllers.GameController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * The view responsible for displaying options for starting a new game in the
 * Arimaa application, allowing the user to choose between playing versus
 * another player or versus the computer.
 * This class extends JFrame to create a graphical user interface.
 */
public class NewGameView extends JFrame {

    public GameController game;

    /**
     * Constructs a new NewGameView instance with the specified game controller.
     * 
     * @param game The GameController instance associated with this view.
     */
    public NewGameView(GameController game) {
        super("Arimaa New Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 400);
        setLocationRelativeTo(null);

        this.game = game;

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 1));

        JButton vsPlayerButton = new JButton("VS Player");
        JButton vsComputerButton = new JButton("VS Computer");

        // button to choose option against real opponent
        vsPlayerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // we start the game and hide this window
                game.createBoard(false);
                setVisible(false);
            }
        });

        // button to chose option against AI opponent
        vsComputerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // we start the game vs AI
                game.createBoard(false);
                game.setVsAI();
                setVisible(false);
            }
        });

        panel.add(vsPlayerButton);
        panel.add(vsComputerButton);

        add(panel);
    }

}
