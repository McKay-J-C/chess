package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;

public class SqlGameDAO implements GameDAO {

    public SqlGameDAO() throws DataAccessException {
        configureDatabase();
    }

    @Override
    public int addGame(String s) {
        return 1;
    }

    @Override
    public GameData getGame(int gameID) {
        return new GameData(1, "", "", "", new ChessGame());
    }

    @Override
    public HashSet<GameData> listGames() {
        return new HashSet<>();
    }

    @Override
    public void updateGame(int gameID, ChessGame.TeamColor teamColor, String username) {

    }

    @Override
    public HashSet<GameData> getGames() {
        return new HashSet<>();
    }

    @Override
    public void clear() {

    }

    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS game (
              `gameID` int NOT NULL AUTO_INCREMENT,
              `gameName` varchar(256) NOT NULL,
              `whiteUsername` varchar(256) NULL,
              `blackUsername` varchar(256) NULL,
              `game` TEXT DEFAULT NULL,
              PRIMARY KEY (`gameID`),
              INDEX(gameName),
              FOREIGN KEY (whiteUsername) REFERENCES user(username),
              FOREIGN KEY (blackUsername) REFERENCES user(username)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
    };

    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (Connection conn = DatabaseManager.getConnection()) {
            for (String statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Unable to configure database: %s", ex.getMessage()));
        }
    }
}
