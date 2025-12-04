package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.HashSet;

public interface GameDAO {

    default int addGame(String gameName) throws DataAccessException {
        return 1;
    }

    default GameData getGame(int gameID) throws DataAccessException {
        return new GameData(1, "", "", "", new ChessGame());
    }

    default HashSet<GameData> getGames() throws DataAccessException {
        return new HashSet<>();
    }

    default void updateGame(int gameID, ChessGame.TeamColor teamColor, String username) throws DataAccessException {
    }

    default void updateMove(String gameJson, int gameID) throws DataAccessException {
    }

    default void deleteThenAddGame(GameData newGameData) throws DataAccessException {
    }

    default void clear() throws DataAccessException {
    }

    default int getCurID() {
        return 0;
    }
}
