package dataacess;


import dataaccess.*;

import model.UserData;
import org.junit.jupiter.api.*;
import org.mindrot.jbcrypt.BCrypt;
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
        String hashedPassword = BCrypt.hashpw("goCougs27", BCrypt.gensalt());
        UserData testUserData = new UserData("Bob", hashedPassword, "cs240@gmail.com");
        assertEquivalentUsers(userData, testUserData);
    }

    @Test
    @Order(2)
    @DisplayName("Unsuccessful Get User")
    public void unsuccessfulGetUser() throws DataAccessException {
        makeUserBob();
        UserData userData = userDAO.getUser("hi");
        Assertions.assertNull(userData);
    }

    @Test
    @Order(3)
    @DisplayName("Successful Create User")
    public void successfulCreateUser() throws DataAccessException, SQLException {
        createBob();
        UserData userData = userDAO.getUser("Bob");
        UserData testUserData = new UserData("Bob", "goCougs27", "cs240@gmail.com");
        assertEquivalentUsers(userData, testUserData);
    }

    @Test
    @Order(4)
    @DisplayName("Taken Create User")
    public void takenCreateUser() throws DataAccessException, SQLException {
        createBob();
        Assertions.assertThrows(DataAccessException.AlreadyTakenException.class, this::createBob);
    }

    public void createBob() throws DataAccessException, SQLException {
        userDAO.createUser("Bob", "goCougs27", "cs240@gmail.com");
    }

    public void makeUserBob() throws DataAccessException {
        String statement = "INSERT INTO user (username, password, email) VALUES (?, ?, ?)";
        try (var conn = DatabaseManager.getConnection()) {
            var preparedStatement = conn.prepareStatement(statement);
            preparedStatement.setString(1, "Bob");
            String hashed = BCrypt.hashpw("goCougs27", BCrypt.gensalt());
            preparedStatement.setString(2, hashed);
            preparedStatement.setString(3, "cs240@gmail.com");
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    public void assertEquivalentUsers(UserData userData, UserData testUserData) throws DataAccessException {
        Assertions.assertEquals(testUserData.username(), userData.username());
        Assertions.assertEquals(testUserData.email(), userData.email());
        Assertions.assertTrue(userDAO.verifyUser(testUserData.username(), "goCougs27"));
    }
}
