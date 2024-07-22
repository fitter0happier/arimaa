package com.cvut.fel.pjv.Utilities;

import com.cvut.fel.pjv.Controllers.GameController;
import com.cvut.fel.pjv.Models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;

public class ValidationHelperTest {

    @Mock
    private GameController game;

    @Mock
    private Board board;

    private ValidationHelper validationHelper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        this.board = new Board();
        board.initBoard();
        validationHelper = new ValidationHelper(game, board);
    }

    // we check if we can move into the same cell
    @Test
    public void testCheckIfMoveValid_SameCell() {
        int[] oldPos = { 0, 0 };
        int[] newPos = { 0, 0 };
        boolean result = validationHelper.checkIfMoveValid(oldPos, newPos, Colors.GOLD, 0, false);
        assertFalse(result);
    }

    // we check if we can move into busy cell
    @Test
    public void testCheckIfMoveValid_CellOccupied() {
        int[] oldPos = { 0, 0 };
        int[] newPos = { 0, 1 };
        boolean result = validationHelper.checkIfMoveValid(oldPos, newPos, Colors.GOLD, 0, false);
        assertFalse(result);
    }

    // we check illegal move
    @Test
    public void testCheckIfMoveValid_IllegalMove() {
        int[] oldPos = { 0, 0 };
        int[] newPos = { 2, 2 };
        boolean result = validationHelper.checkIfMoveValid(oldPos, newPos, Colors.GOLD, 0, false);
        assertFalse(result);
    }

    // we check legal move
    @Test
    public void testCheckIfMoveValid_NormalMove() {
        int[] oldPos = { 6, 0 };
        int[] newPos = { 5, 0 };
        boolean result = validationHelper.checkIfMoveValid(oldPos, newPos, Colors.GOLD, 0, false);
        assertTrue(result);
    }

    // we check we can move frozen figure
    @Test
    public void testCheckIfMoveValid_Freeze() {
        int[] oldPos = { 0, 0 };
        int[] newPos = { 1, 0 };
        Piece goldRabbit = new Piece(Figures.RABBIT, Colors.GOLD);
        Piece silverElephant = new Piece(Figures.ELEPHANT, Colors.SILVER);
        board.board[0][0] = goldRabbit;
        board.board[1][0] = null;
        board.board[0][1] = silverElephant;
        boolean result = validationHelper.checkIfMoveValid(oldPos, newPos, Colors.GOLD, 0, false);
        assertFalse(result);
    }

    // we check if we can finish the turn without making any changes
    @Test
    public void testValidateEndTurn_NoChange() {
        validationHelper.boardCopy = new Piece[8][8]; // copy is empty
        board.board = new Piece[8][8]; // board is also empty

        boolean result = validationHelper.validateEndTurn();
        assertFalse(result);
    }

    // we check if we can make the move with changes
    @Test
    public void testValidateEndTurn_WithChange() {
        validationHelper.boardCopy = new Piece[8][8]; // copy is empty
        Piece[][] boardState = new Piece[8][8];
        boardState[0][0] = new Piece(Figures.RABBIT, Colors.GOLD);
        board.board = boardState;

        boolean result = validationHelper.validateEndTurn();
        assertTrue(result);
    }

    // we check if trap function returns anything if no one was trapped
    @Test
    public void testTrap_NoTrap() {
        board.trap = new int[] { 3, 3, 5, 5 };
        board.board[3][3] = null;
        board.board[3][5] = null;
        board.board[5][3] = null;
        board.board[5][5] = null;

        String result = validationHelper.trap();
        assertEquals("", result);
    }

    // we check the correct return of trap function
    @Test
    public void testTrap_WithTrap() {
        board.trap = new int[] { 3, 3, 5, 5 };
        Piece trappedPiece = new Piece(Figures.RABBIT, Colors.GOLD);
        board.board[3][3] = trappedPiece;
        board.board[2][3] = null;
        board.board[4][3] = null;
        board.board[3][2] = null;
        board.board[3][4] = null;

        String result = validationHelper.trap();
        assertFalse(result.isEmpty());
        assertEquals("Rd5x ", result);
    }

    // we check if there is any winner if no win conditions were met
    @Test
    public void testCheckWinner_NoWinner() {
        Piece[][] boardState = new Piece[8][8];
        boardState[0][0] = new Piece(Figures.RABBIT, Colors.SILVER);
        boardState[7][7] = new Piece(Figures.RABBIT, Colors.GOLD);
        board.board = boardState;

        Colors result = validationHelper.checkWinner(Colors.GOLD);
        assertNull(result);
    }

    // we check if the game is won if the gold rabbit is in the last row
    @Test
    public void testCheckWinner_GoldRabbitWins() {
        Piece[][] boardState = new Piece[8][8];
        boardState[0][0] = new Piece(Figures.RABBIT, Colors.GOLD);
        board.board = boardState;

        Colors result = validationHelper.checkWinner(Colors.GOLD);
        assertEquals(Colors.GOLD, result);
    }

    // we check if the game is won if the silver rabbit is in the last row
    @Test
    public void testCheckWinner_SilverRabbitWins() {
        Piece[][] boardState = new Piece[8][8];
        boardState[7][7] = new Piece(Figures.RABBIT, Colors.SILVER);
        board.board = boardState;

        Colors result = validationHelper.checkWinner(Colors.GOLD);
        assertEquals(Colors.SILVER, result);
    }

    // we check if the gold wins if no rabbits are left and it's his turn
    @Test
    public void testCheckWinner_NoGoldRabbits() {
        Piece[][] boardState = new Piece[8][8];
        board.board = boardState;

        Colors result = validationHelper.checkWinner(Colors.GOLD);
        assertEquals(Colors.GOLD, result);
    }

    /// we check if the silver wins if no rabbits are left and it's his turn
    @Test
    public void testCheckWinner_NoSilverRabbits() {
        Piece[][] boardState = new Piece[8][8];
        board.board = boardState;

        Colors result = validationHelper.checkWinner(Colors.SILVER);
        assertEquals(Colors.SILVER, result);
    }
}
