package model;

import chess.ChessGame;

public record GameData() {
    static int gameID;
    static String whiteUsername;
    static String blackUsername;
    static String gameName;
    static ChessGame game;
}
