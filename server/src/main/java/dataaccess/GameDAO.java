package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.ArrayList;
import java.util.HashSet;

public interface GameDAO {

    static GameData addGame() {
        return new GameData(1, "", "", "", new ChessGame());
    }

    static GameData getGame(int gameID) {
        return new GameData(1, "", "", "", new ChessGame());
    }

    static HashSet<GameData> listGames() {
        return new HashSet<>();
    }

    static void updateGame(int gameID) {

    }

    static void clear() {

    }
}
