package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.HashSet;

public class MemGameDAO implements GameDAO {

    HashSet<GameData> gameData = new HashSet<>();
    int curID = 1;

    @Override
    public int addGame(String gameName) {
        GameData game = new GameData(curID, null, null, gameName, new ChessGame());
        curID++;
        gameData.add(game);
        return game.gameID();
    }

    @Override
    public GameData getGame(int gameID) {
        for (GameData game : gameData) {
            if (game.gameID() == gameID) {
                return game;
            }
        }
        return null;
    }

    @Override
    public HashSet<GameData> getGames() {
        return gameData;
    }

    @Override
    public void updateGame(int gameID, ChessGame.TeamColor teamColor, String username) {
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

    @Override
    public void clear() {
        gameData = new HashSet<>();
        curID = 1;
    }
}
