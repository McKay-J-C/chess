package dataaccess;

import model.AuthData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

import static model.AuthData.generateToken;

public class SqlAuthDAO implements AuthDAO {

    public SqlAuthDAO() {
        String[] createStatements = {
                """
            CREATE TABLE IF NOT EXISTS auth (
              `id` int NOT NULL AUTO_INCREMENT,
              `authToken` varchar(256) NOT NULL,
              `username` varchar(256) NOT NULL,
              PRIMARY KEY (`id`),
              INDEX(authToken),
              INDEX(username),
              FOREIGN KEY (username) REFERENCES user(username) ON DELETE CASCADE
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
        };
        DatabaseManager.configureDatabase(createStatements);
    }

    @Override
    public AuthData createAuth(String username) throws DataAccessException {
        String token = generateToken();
        AuthData auth = new AuthData(token, username);
        String statement = "INSERT INTO auth (username, authToken) VALUES (?, ?)";
        try (var conn = DatabaseManager.getConnection()) {
            var preparedStatement = conn.prepareStatement(statement);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, token);
            preparedStatement.executeUpdate();
            return auth;
        } catch (SQLException e) {
            throw new DataAccessException("Error: error in inserting auth");
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT authToken, username FROM auth WHERE authToken=?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1, authToken);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String foundAuthToken = rs.getString("authToken");
                        String foundUsername = rs.getString("username");
                        return new AuthData(foundAuthToken, foundUsername);
                    }
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException("Error: error in finding auth");
        }
        return null;
    }

    @Override
    public void deleteAuth(AuthData auth) throws DataAccessException {
        var statement = "DELETE FROM auth WHERE authToken=?";
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement( statement)) {
                preparedStatement.setString(1, auth.authToken());
                preparedStatement.executeUpdate();
            }
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException("Error: error deleting auth");
        }
    }

    @Override
    public AuthData authorize(String authToken) throws DataAccessException {
        AuthData authData = this.getAuth(authToken);
        if (authData == null) {
            throw new DataAccessException.UnauthorizedException("Error: unauthorized");
        }
        return authData;
    }

    @Override
    public void clear() throws DataAccessException {
        var statement = "DELETE FROM auth";
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement( statement)) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException("Error: error in deleting auths");
        }
    }

    @Override
    public HashSet<AuthData> getAuths() throws DataAccessException {
        HashSet<AuthData> auths = new HashSet<>();
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM auth";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String foundAuthToken = rs.getString("authToken");
                        String foundUsername = rs.getString("username");
                        auths.add(new AuthData(foundAuthToken, foundUsername));
                    }
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException("Error: error in finding users");
        }
        return auths;
    }

}
