package dataaccess;

import model.GameData;

import java.util.ArrayList;

public class GameDAO {

    static GameData createGame() {
        return new GameData();
    }

    static GameData getGame(int gameID) {
        return new GameData();
    }

    static ArrayList<GameData> listGames() {
        return new ArrayList<>();
    }

    static void updateGame(int gameID) {

    }

    static void clear() {

    }
}
