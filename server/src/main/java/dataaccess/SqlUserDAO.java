package dataaccess;

import model.AuthData;
import model.UserData;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.sql.*;

public class SqlUserDAO implements UserDAO {

    public SqlUserDAO() throws DataAccessException {
        configureDatabase();
    }

    @Override
    public AuthData createUser(String username, String password, String email) throws SQLException, DataAccessException {
        return null;
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT username, password, email FROM user WHERE username=?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String foundUsername = rs.getString("username");
                        String foundPassword = rs.getString("password");
                        String foundEmail = rs.getString("email");
                        return new UserData(foundUsername, foundPassword, foundEmail);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error in finding user");
        }
        return null;
    }

    @Override
    public void clear() {

    }

    @Override
    public HashSet<UserData> getUsers() {
        return null;
    }

    private final String[] createStatements = {
        """
            CREATE TABLE IF NOT EXISTS user (
              `id` int NOT NULL AUTO_INCREMENT,
              `username` varchar(256) NOT NULL,
              `password` varchar(256) NOT NULL,
              `email` varchar(256) NOT NULL,
              PRIMARY KEY (`id`),
              INDEX(username),
              INDEX(email),
              UNIQUE KEY (username)
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
