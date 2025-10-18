package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.ArrayList;

public class GameDAO {

    public static GameData createGame() {
        return new GameData(1, "", "", "", new ChessGame());
    }

    public static GameData getGame(int gameID) {
        return new GameData(1, "", "", "", new ChessGame());
    }

    public static ArrayList<GameData> listGames() {
        return new ArrayList<>();
    }

    public static void updateGame(int gameID) {

    }

    public static void clear() {

    }
}
