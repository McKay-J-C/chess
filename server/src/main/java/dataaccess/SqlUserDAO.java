package dataaccess;

import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

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
        if (getUser(username) != null) {
            throw new DataAccessException.AlreadyTakenException("Error: already taken");
        }
        String statement = "INSERT INTO user (username, password, email) VALUES (?, ?, ?)";
        try (var conn = DatabaseManager.getConnection()) {
            var preparedStatement = conn.prepareStatement(statement);
            preparedStatement.setString(1, username);
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

            preparedStatement.setString(2, hashedPassword);
            preparedStatement.setString(3, email);
            preparedStatement.executeUpdate();

            AuthDAO authDAO = new SqlAuthDAO();
            return authDAO.createAuth(username);
        } catch (SQLException e) {
            throw new DataAccessException("Error: problem creating user");
        }
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
            throw new DataAccessException("Error: error in finding user");
        }
        return null;
    }

    @Override
    public void clear() throws DataAccessException {
        var statement = "DELETE FROM user";
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement( statement)) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException("Error: error in deleting users");
        }
    }

    @Override
    public HashSet<UserData> getUsers() throws DataAccessException {
        HashSet<UserData> users = new HashSet<>();
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM user";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String foundUsername = rs.getString("username");
                        String foundPassword = rs.getString("password");
                        String foundEmail = rs.getString("email");
                        users.add(new UserData(foundUsername, foundPassword, foundEmail));
                    }
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException("Error: error in finding users");
        }
        return users;
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
            throw new DataAccessException("Error: error in configuring database");
        }
    }

    @Override
    public boolean verifyUser(String username, String providedClearTextPassword) throws DataAccessException {
        // read the previously hashed password from the database
        UserDAO userDAO = new SqlUserDAO();
        UserData userData = userDAO.getUser(username);
        if (userData == null) {
            throw new DataAccessException("Error: Username not found");
        }
        String hashedPassword = userData.password();
        return BCrypt.checkpw(providedClearTextPassword, hashedPassword);
    }

}
