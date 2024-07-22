package com.cvut.fel.pjv.Utilities;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.Scanner;

import com.cvut.fel.pjv.Controllers.GameController;
import com.cvut.fel.pjv.Models.Board;
import com.cvut.fel.pjv.Models.Piece;
import com.cvut.fel.pjv.Views.BoardView;
import com.cvut.fel.pjv.Views.ControlsView;
import com.cvut.fel.pjv.Views.MainMenuView;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class GameSerializerTest {

    private GameSerializer gameSerializer;

    @Mock
    private GameController gameController;

    @Mock
    private Board board;

    @Mock
    private ControlsView controlsView;

    @Mock
    private BoardView boardView;

    @Mock
    private MainMenuView mainMenuView;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        gameController.reserveGold = 500000;
        gameController.reserveSilver = 500000;
        gameController.board = board;
        gameController.mainMenu = mainMenuView;

        board.clearBoard();

        gameSerializer = new GameSerializer(gameController, board, controlsView, boardView);
    }

    // we check if turn appends to history correctly
    @Test
    public void testAppendToHistory() {
        String turn = "1g";
        gameSerializer.appendToHistory(turn);
        assertTrue(gameSerializer.history.contains(turn));
    }

    // we check if the history is cleared correctly
    @Test
    public void testClearHistory() {
        gameSerializer.appendToHistory("1g");
        gameSerializer.clearHistory();
        assertTrue(gameSerializer.history.isEmpty());
    }

    // we check if we can get the last turn
    @Test
    public void testGetLastTurn() {
        String turn1 = "1g";
        String turn2 = "1s";
        gameSerializer.appendToHistory(turn1);
        gameSerializer.appendToHistory(turn2);
        String lastTurn = gameSerializer.getLastTurn();
        assertEquals(turn2, lastTurn);
        assertFalse(gameSerializer.history.contains(turn2));
    }

    // we check the load by looking at what was written into history
    @Test
    public void testLoadGame() throws IOException {
        File tempFile = File.createTempFile("test", ".txt");
        tempFile.deleteOnExit();
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("1g Rc3\n");
            writer.write("1s Rd4\n");
            writer.write("GOLD 4\n");
            writer.write("SILVER 5\n");
            writer.write("CURRENT 60\n");
        }

        board.board = new Piece[8][8];
        gameSerializer.loadGame(tempFile);

        verify(controlsView, times(1)).changeControls();
        verify(boardView, times(1)).initBoard();

        assertEquals("1g Rc3", gameSerializer.history.get(0));
        assertEquals("1s Rd4", gameSerializer.history.get(1));
    }

    // we check the save by looking into the saved file
    @Test
    public void testSaveGame() throws IOException {

        String filename = Instant.now().toString();
        String filePath = "arimaa/saves/" + filename + ".txt";
        new File("arimaa/saves/").mkdirs();

        gameSerializer.appendToHistory("1g Rc3");
        gameSerializer.appendToHistory("1s Rd4");

        gameSerializer.saveGame(filename, 60);

        File savedFile = new File(filePath);
        assertTrue(savedFile.exists());

        try (Scanner scanner = new Scanner(savedFile)) {
            assertEquals("1g Rc3", scanner.nextLine());
            assertEquals("1s Rd4", scanner.nextLine());
            assertEquals("CURRENT 60", scanner.nextLine());
            assertEquals("GOLD 500000", scanner.nextLine());
            assertEquals("SILVER 500000", scanner.nextLine());
        } finally {
            savedFile.delete();
        }
    }
}
