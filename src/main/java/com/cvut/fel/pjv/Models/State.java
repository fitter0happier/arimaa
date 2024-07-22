package com.cvut.fel.pjv.Models;

import com.cvut.fel.pjv.Utilities.ValidationHelper;

/**
 * The State class represents the state of a piece during a turn in the game.
 * It stores information about the piece's position, 
 * whether it was pushed or can pull, and any piece that was 
 * lost during the move.
 */
public class State {
    public boolean wasPush;
    public boolean canPull;
    
    public int figToMoveNewRow;
    public int figToMoveNewCol;
    public int powerOfFigToMove;

    public int posWhereIWasRow;
    public int posWhereIWasCol;

    public int oldRow;
    public int oldCol;
    public int newRow;
    public int newCol;

    public Piece lostPiece;
    public int whereLostPieceWasRow;
    public int whereLostPieceWasCol;

    /**
     * Default constructor for the State class.
     */
    public State() {}

    /**
     * Copies the validation information from the given ValidationHelper instance.
     * This includes the ability to pull, whether a push occurred,
     * the new position of the figure to move, and its power.
     *
     * @param validate the ValidationHelper instance from which to copy the information.
     */
    public void copyValidator(ValidationHelper validate) {
        canPull = validate.canPull;
        wasPush = validate.wasPush;
        figToMoveNewRow = validate.figToMoveNewRow;
        figToMoveNewCol = validate.figToMoveNewCol;
        posWhereIWasRow = validate.posWhereIWasRow;
        posWhereIWasCol = validate.posWhereIWasCol;
        powerOfFigToMove = validate.powerOfFigToMove;
    }
}
