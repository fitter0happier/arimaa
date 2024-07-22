package com.cvut.fel.pjv.Models;

import com.cvut.fel.pjv.Utilities.Colors;
import com.cvut.fel.pjv.Utilities.Figures;

/**
 * The Piece class represents a game piece with a specific type and color.
 * It includes methods to get the type, color, and notation of the piece.
 */
public class Piece {

    private Figures type;
    private Colors color;
    
    /**
     * Constructs a Piece with the specified type and color.
     *
     * @param type  the type of the piece.
     * @param color the color of the piece.
     */
    public Piece(Figures type, Colors color) {
        this.color = color;
        this.type = type;
    }

    /**
     * Constructs a Piece from a string notation.
     * The notation determines the type and color of the piece.
     *
     * @param figure the string notation representing the piece.
     */
    public Piece(String figure) {
        if (figure.equals(figure.toLowerCase())) {
            this.color = Colors.SILVER;

        } else {
            this.color = Colors.GOLD;
            figure = figure.toLowerCase();
        }

        switch (figure) {
            case "r":
                this.type = Figures.RABBIT;
                break;
            case "c":
                this.type = Figures.CAT;
                break;
            case "d":
                this.type = Figures.DOG;
                break;
            case "h":
                this.type = Figures.HORSE;
                break;
            case "m":
                this.type = Figures.CAMEL;
                break;
            case "e":
                this.type = Figures.ELEPHANT;
                break;
        }
    }

    public Figures getType() {
        return type;
    }

    public Colors getColor() {
        return color;
    }

    /**
     * Gets the notation of the piece.
     * The notation is a single character representing the type of the piece,
     * and is uppercase for gold pieces and lowercase for silver pieces.
     *
     * @return the notation of the piece.
     */
    public String getNotation() {
        String letter = "";
        switch (type) {
            case RABBIT:
                letter = "R";
                break;
            case CAT:
                letter = "C";
                break;
            case DOG:
                letter = "D";
                break;
            case HORSE:
                letter = "H";
                break;
            case CAMEL:
                letter = "M";
                break;
            case ELEPHANT:
                letter = "E";
                break;
        }

        if (color == Colors.SILVER) {
            return letter.toLowerCase();
        }
        
        return letter;
    }
}
