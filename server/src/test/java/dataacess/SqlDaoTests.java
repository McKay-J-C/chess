package dataacess;


import dataaccess.*;

import model.UserData;
import org.junit.jupiter.api.*;
import service.ClearService;

import java.sql.SQLException;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SqlDaoTests {

    UserDAO userDAO = new SqlUserDAO();
    GameDAO gameDAO = new SqlGameDAO();
    AuthDAO authDAO = new SqlAuthDAO();
    ClearService clearService = new ClearService(userDAO, authDAO, gameDAO);

    public SqlDaoTests() throws DataAccessException {
    }

    @BeforeEach
    public void clear() throws DataAccessException {
        clearService.clear();
    }

    @Test
    @Order(1)
    @DisplayName("Successful Get User")
    public void successfulGetUser() throws DataAccessException {
        makeUserBob();
        UserData userData = userDAO.getUser("Bob");
        UserData testUserData = new UserData("Bob", "goCougs27", "cs240@gmail.com");
        Assertions.assertEquals(userData, testUserData);
    }

    @Test
    @Order(2)
    @DisplayName("Unsuccessful Get User")
    public void unsuccessfulGetUser() throws DataAccessException {
        makeUserBob();
        UserData userData = userDAO.getUser("hi");
        Assertions.assertNull(userData);
    }

    public void makeUserBob() throws DataAccessException {
        String statement = "INSERT INTO user (username, password, email) VALUES (?, ?, ?)";
        try (var conn = DatabaseManager.getConnection()) {
            var preparedStatement = conn.prepareStatement(statement);
            preparedStatement.setString(1, "Bob");
            preparedStatement.setString(2, "goCougs27");
            preparedStatement.setString(3, "cs240@gmail.com");
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }
}
