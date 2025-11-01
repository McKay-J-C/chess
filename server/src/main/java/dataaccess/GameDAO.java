package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.HashSet;

public interface GameDAO {

    default int addGame(String s) {
        return 1;
    }

    default GameData getGame(int gameID) throws DataAccessException {
        return new GameData(1, "", "", "", new ChessGame());
    }

    default HashSet<GameData> listGames() {
        return new HashSet<>();
    }

    default void updateGame(int gameID, ChessGame.TeamColor teamColor, String username) {

    }

    default HashSet<GameData> getGames() {
        return new HashSet<>();
    }

    default void clear() throws DataAccessException {

    }
}
