package dataaccess;

import model.GameData;

import java.util.ArrayList;

public class GameDAO {

    public static GameData createGame() {
        return new GameData();
    }

    public static GameData getGame(int gameID) {
        return new GameData();
    }

    public static ArrayList<GameData> listGames() {
        return new ArrayList<>();
    }

    public static void updateGame(int gameID) {

    }

    public static void clear() {

    }
}
