package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;

import java.sql.*;
import java.util.HashSet;

public class SqlGameDAO implements GameDAO {

    private final Gson serializer = new Gson();

    public SqlGameDAO() {
        String[] createStatements = {
                """
            CREATE TABLE IF NOT EXISTS game (
              `gameID` int NOT NULL AUTO_INCREMENT,
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
        DatabaseManager.configureDatabase(createStatements);
    }

    @Override
    public int addGame(String gameName) throws DataAccessException {
        return addGame(gameName, null, null, new ChessGame());
    }

    private int addGame(String gameName, String whiteUsername, String blackUsername, ChessGame game) throws DataAccessException {
        String statement = "INSERT INTO game (whiteUsername, blackUsername, gameName, game) VALUES (?,?,?,?)";
        try (var conn = DatabaseManager.getConnection()) {
            var preparedStatement = conn.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, whiteUsername);
            preparedStatement.setString(2, blackUsername);
            preparedStatement.setString(3, gameName);

            String gameJson = serializer.toJson(game);
            preparedStatement.setString(4, gameJson);

            preparedStatement.executeUpdate();
            try (ResultSet rs = preparedStatement.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            throw new DataAccessException("Error: No gameID returned");
        } catch (SQLException e) {
            throw new DataAccessException("Error: Database access error");
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
            throw new DataAccessException("Error: error in finding auth");
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
            throw new DataAccessException("Error: error in finding users");
        }
        return games;
    }

    @Override
    public void updateGame(int gameID, ChessGame.TeamColor teamColor, String username) throws DataAccessException {
        GameData gameData = getGame(gameID);
        if (gameData == null) {
            throw new DataAccessException("Error: Game not found");
        }
        GameData newGameData;
        if (teamColor == ChessGame.TeamColor.WHITE) {
            if (gameData.whiteUsername() != null) {
                throw new DataAccessException.AlreadyTakenException("Error: White player already taken");
            }
            newGameData = new GameData(
                    gameID, username, gameData.blackUsername(), gameData.gameName(), gameData.game());
        } else {
            if (gameData.blackUsername() != null) {
                throw new DataAccessException.AlreadyTakenException("Error: Black player already taken");
            }
            newGameData = new GameData(
                    gameID, gameData.whiteUsername(), username, gameData.gameName(), gameData.game());
        }
        deleteThenAddGame(newGameData);
    }

    @Override
    public void deleteThenAddGame(GameData newGameData) throws DataAccessException {
        var updateStatement = "UPDATE game SET whiteUsername = ?, blackUsername = ? WHERE gameID=?";
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement(updateStatement)) {
                preparedStatement.setString(1, newGameData.whiteUsername());
                preparedStatement.setString(2, newGameData.blackUsername());
                preparedStatement.setInt(3, newGameData.gameID());
                preparedStatement.executeUpdate();
            }
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException("Error: problem in updating game");
        }
    }

    @Override
    public void clear() throws DataAccessException {
        var statement1 = "DELETE FROM game;";
        var statement2 = " ALTER TABLE game AUTO_INCREMENT = 1;";
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement1 = conn.prepareStatement(statement1)) {
                preparedStatement1.executeUpdate();
            }
            try (var preparedStatement2 = conn.prepareStatement(statement2)) {
                preparedStatement2.executeUpdate();
            }
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException("Error: error in deleting games");
        }
    }

}
