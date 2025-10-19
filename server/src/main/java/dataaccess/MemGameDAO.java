package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.ArrayList;
import java.util.HashSet;

public class MemGameDAO implements GameDAO {

    HashSet<GameData> gameData;

    static GameData createGame() {
        return new GameData(1, "", "", "", new ChessGame());
    }

    static GameData getGame(int gameID) {
        return new GameData(1, "", "", "", new ChessGame());
    }

    static ArrayList<GameData> listGames() {
        return new ArrayList<>();
    }

    static void updateGame(int gameID) {

    }

    static void clear() {

    }
}
