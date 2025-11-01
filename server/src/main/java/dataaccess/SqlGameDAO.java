package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;

import java.sql.*;
import java.util.HashSet;

public class SqlGameDAO implements GameDAO {

    private final Gson serializer = new Gson();
    private int curID;

    public SqlGameDAO() throws DataAccessException {
        configureDatabase();
    }

    @Override
    public int addGame(String gameName) throws DataAccessException {
        curID++;
        return addGame(curID, gameName, null, null, new ChessGame());
    }

    private int addGame(int gameID, String gameName, String whiteUsername, String blackUsername, ChessGame game) throws DataAccessException {
        String statement = "INSERT INTO game (gameID, whiteUsername, blackUsername, gameName, game) VALUES (?,?,?,?,?)";
        try (var conn = DatabaseManager.getConnection()) {
            var preparedStatement = conn.prepareStatement(statement);
            preparedStatement.setInt(1, gameID);
            preparedStatement.setString(2, whiteUsername);
            preparedStatement.setString(3, blackUsername);
            preparedStatement.setString(4, gameName);

            String gameJson = serializer.toJson(game);
            preparedStatement.setString(5, gameJson);

            preparedStatement.executeUpdate();
            return gameID;
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM game WHERE gameID=?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setInt(1, gameID);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String gameName = rs.getString("gameName");
                        String whiteUsername = rs.getString("whiteUsername");
                        String blackUsername = rs.getString("blackUsername");
                        String gameJson = rs.getString("game");

                        ChessGame game = serializer.fromJson(gameJson, ChessGame.class);
                        return new GameData(gameID, whiteUsername, blackUsername, gameName, game);
                    }
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException("Error in finding auth");
        }
        return null;
    }

    @Override
    public HashSet<GameData> getGames() throws DataAccessException {
        HashSet<GameData> games = new HashSet<>();
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM game";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int foundGameID = rs.getInt("gameID");
                        String foundWhiteUsername = rs.getString("whiteUsername");
                        String foundBlackUsername = rs.getString("blackUsername");
                        String foundGameName = rs.getString("gameName");

                        String gameJson = rs.getString("game");
                        ChessGame foundGame = serializer.fromJson(gameJson, ChessGame.class);
                        games.add(new GameData(foundGameID, foundWhiteUsername, foundBlackUsername, foundGameName, foundGame));
                    }
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException("Error in finding users");
        }
        if (games.isEmpty()) {
            return null;
        }
        return games;
    }

    @Override
    public void updateGame(int gameID, ChessGame.TeamColor teamColor, String username) throws DataAccessException {
        GameData gameData = getGame(gameID);
        if (gameData == null) {
            throw new DataAccessException("Game not found");
        }
        GameData newGameData;
        if (teamColor == ChessGame.TeamColor.WHITE) {
            if (gameData.whiteUsername() != null) {
                throw new DataAccessException.AlreadyTakenException("White player already taken");
            }
            newGameData = new GameData(
                    gameID, username, gameData.blackUsername(), gameData.gameName(), gameData.game());
        } else {
            if (gameData.blackUsername() != null) {
                throw new DataAccessException.AlreadyTakenException("Black player already taken");
            }
            newGameData = new GameData(
                    gameID, gameData.whiteUsername(), username, gameData.gameName(), gameData.game());
        }
        deleteThenAddGame(newGameData);
    }

    private void deleteThenAddGame(GameData newGameData) {
        var delStatement = "DELETE FROM game WHERE gameID=?";
        try (var conn = DatabaseManager.getConnection()) {
            try (var delPreparedStatement = conn.prepareStatement(delStatement)) {
                delPreparedStatement.setInt(1, newGameData.gameID());
                delPreparedStatement.executeUpdate();
            }
            var addStatement = "INSERT INTO game (gameID, whiteUsername, blackUsername, gameName, game) VALUES (?,?,?,?,?)";
            try (var addPreparedStatement = conn.prepareStatement(addStatement)) {
                addPreparedStatement.setInt(1, newGameData.gameID());
                addPreparedStatement.setString(2, newGameData.whiteUsername());
                addPreparedStatement.setString(3, newGameData.blackUsername());
                addPreparedStatement.setString(4, newGameData.gameName());

                String gameJson = serializer.toJson(newGameData.game());
                addPreparedStatement.setString(5, gameJson);

                addPreparedStatement.executeUpdate();
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        curID = 0;
        var statement = "DELETE FROM game";
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement( statement)) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS game (
              `gameID` int NOT NULL,
              `gameName` varchar(256) NOT NULL,
              `whiteUsername` varchar(256) NULL,
              `blackUsername` varchar(256) NULL,
              `game` TEXT DEFAULT NULL,
              PRIMARY KEY (`gameID`),
              INDEX(gameName),
              FOREIGN KEY (whiteUsername) REFERENCES user(username) ON DELETE SET NULL,
              FOREIGN KEY (blackUsername) REFERENCES user(username) ON DELETE SET NULL
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
