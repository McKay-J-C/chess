package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.HashSet;

public class MemGameDAO implements GameDAO {

    static HashSet<GameData> gameData = new HashSet<>();
    static int curID = 1;

    public static int addGame(String gameName) {
        GameData game = new GameData(curID, null, null, gameName, new ChessGame());
        curID++;
        gameData.add(game);
        return game.gameID();
    }

    public static GameData getGame(int gameID) {
        for (GameData game : gameData) {
            if (game.gameID() == gameID) {
                return game;
            }
        }
        return null;
    }

    public static HashSet<GameData> getGames() {
        return gameData;
    }

    public static void updateGame(int gameID, ChessGame.TeamColor teamColor, String username) {
        for (GameData game : gameData) {
            if (game.gameID() == gameID) {
                gameData.remove(game);
                GameData newGame;
                if (teamColor == ChessGame.TeamColor.WHITE) {
                    newGame = new GameData(gameID, username, game.blackUsername(), game.gameName(), game.game());
                } else {
                    newGame = new GameData(gameID, game.whiteUsername(), username, game.gameName(), game.game());
                }
                gameData.add(newGame);
            }
        }
    }

    public static void clear() {
        gameData = new HashSet<>();
        curID = 1;
    }
}
