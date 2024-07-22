package com.cvut.fel.pjv.Controllers;

import com.cvut.fel.pjv.Models.Board;
import com.cvut.fel.pjv.Utilities.*;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GameControllerTest {

    // we check if the timer correctly substracts one second from reserve if the
    // normal time is finished
    @Test
    public void testHandleTimeReserveDepletion() {
        GameController gameController = new GameController(false);
        gameController.reserveGold = 5000;
        gameController.reserveSilver = 5000;
        gameController.timeForTurn = 0;
        gameController.createBoard(false);
        gameController.beginGame();

        gameController.handleTime();
        assertEquals(4000, gameController.reserveGold);
    }

    // we check if the game correctly updates the normal time for turn
    @Test
    public void testHandleTimeUpdateTimeForTurn() throws InterruptedException {
        GameController gameController = new GameController(false);
        gameController.createBoard(false);
        gameController.timeForTurn = 1000;
        gameController.beginGame();
        gameController.handleTime();
        assertEquals(0, gameController.timeForTurn);
    }

    // we check if AI turns actually change the board
    @Test
    void testAIPlay() {
        GameController gameController = new GameController(false);
        gameController.createBoard(false);
        gameController.setVsAI();
        gameController.beginGame();
        gameController.whoseTurn = Colors.SILVER;

        String initialBoardState = boardToString(gameController.board);
        gameController.handleAITurn();
        String boardStateAfterAIMove = boardToString(gameController.board);

        assertNotEquals(initialBoardState, boardStateAfterAIMove);
    }

    // helper function to compare boards before and after AI turn
    private String boardToString(Board board) {
        StringBuilder boardOutput = new StringBuilder();
        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
                if (board.board[i][j] == null) {
                    boardOutput.append("xx ");
                } else {
                    boardOutput.append(board.board[i][j].getColor().ordinal())
                            .append(board.board[i][j].getType().ordinal()).append(" ");
                }
            }
            boardOutput.append("\n");
        }
        return boardOutput.toString();
    }
}
