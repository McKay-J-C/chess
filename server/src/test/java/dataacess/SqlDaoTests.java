package dataacess;


import dataaccess.*;

import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;
import org.mindrot.jbcrypt.BCrypt;
import service.ClearService;

import java.sql.SQLException;
import java.util.HashSet;


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
        assertEquivalentUsers(userData, testUserData, "goCougs27");
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
        assertEquivalentUsers(userData, testUserData, "goCougs27");
    }

    @Test
    @Order(4)
    @DisplayName("Taken Create User")
    public void takenCreateUser() throws DataAccessException, SQLException {
        createBob();
        Assertions.assertThrows(DataAccessException.AlreadyTakenException.class, this::createBob);
    }

    @Test
    @Order(5)
    @DisplayName("Successful Get Users")
    public void successfulGetUsers() throws DataAccessException, SQLException {
        createBob();
        createDave();
        HashSet<UserData> userData = userDAO.getUsers();

        HashSet<UserData> testUserData = new HashSet<>();
        testUserData.add(new UserData("Bob", "goCougs27", "cs240@gmail.com"));
        testUserData.add(new UserData("Dave", "yourmom", "hi@gmail.com"));

        assertEquivalentUserSets(userData, testUserData);
    }

    @Test
    @Order(6)
    @DisplayName("Unsuccessful Get Users")
    public void noUsersGetUsers() throws DataAccessException, SQLException {
        Assertions.assertNull(userDAO.getUsers());
    }

    @Test
    @Order(7)
    @DisplayName("User Clear")
    public void testUserClear() throws DataAccessException, SQLException {
        createBob();
        createDave();
        userDAO.clear();
        Assertions.assertNull(userDAO.getUsers());
        Assertions.assertNull(userDAO.getUser("Bob"));
        Assertions.assertNull(userDAO.getUser("Dave"));
    }

    @Test
    @Order(8)
    @DisplayName("Successful Get Auth")
    public void successfulGetAuth() throws DataAccessException, SQLException {
        makeUserBob();
        makeBobAuth();
        AuthData testAuth = new AuthData("BobsAuth", "Bob");
        AuthData auth = authDAO.getAuth("BobsAuth");
        Assertions.assertEquals(testAuth, auth);
    }

    @Test
    @Order(9)
    @DisplayName("Unsuccessful Get Auth")
    public void unsuccessfulGetAuth() throws DataAccessException, SQLException {
        makeUserBob();
        makeBobAuth();
        Assertions.assertNull(authDAO.getAuth("hi"));
    }

    @Test
    @Order(10)
    @DisplayName("Successful Create Auth")
    public void successfulCreateAuth() throws DataAccessException, SQLException {
        AuthData authData = createBobAndAuth();
        AuthData foundAuthData = authDAO.getAuth(authData.authToken());
        Assertions.assertEquals(authData, foundAuthData);
    }

    @Test
    @Order(11)
    @DisplayName("No User Create Auth")
    public void noUserCreateAuth() throws DataAccessException, SQLException {
        Assertions.assertThrows(DataAccessException.class, () -> authDAO.createAuth("Bob"));
    }

    @Test
    @Order(12)
    @DisplayName("Successful Get Auths")
    public void successfulGetAuths() throws DataAccessException, SQLException {
        AuthData bobAuth = createBobAndAuth();
        AuthData daveAuth = createDaveAndAuth();
        HashSet<AuthData> authData = authDAO.getAuths();

        Assertions.assertTrue(authData.contains(bobAuth));
        Assertions.assertTrue(authData.contains(daveAuth));
        Assertions.assertNotNull(bobAuth.authToken());
        Assertions.assertNotNull(daveAuth.authToken());
    }

    @Test
    @Order(13)
    @DisplayName("Unsuccessful Get Auths")
    public void noAuthGetAuths() throws DataAccessException, SQLException {
        Assertions.assertNull(authDAO.getAuths());
    }

    @Test
    @Order(14)
    @DisplayName("Successful Delete Auth")
    public void successfulDeleteAuth() throws DataAccessException, SQLException {
        AuthData bobAuth = createBobAndAuth();
        authDAO.deleteAuth(bobAuth);
        Assertions.assertNull(authDAO.getAuth(bobAuth.authToken()));
    }

    @Test
    @Order(15)
    @DisplayName("Wrong AuthToken Delete Auth")
    public void wrongAuthDeleteAuth() throws DataAccessException, SQLException {
        AuthData bobAuth = createBobAndAuth();
        AuthData authData = new AuthData("Bob", "hi");
        authDAO.deleteAuth(authData);
        Assertions.assertEquals(bobAuth, authDAO.getAuth(bobAuth.authToken()));
    }

    @Test
    @Order(16)
    @DisplayName("Successful Authorize")
    public void successfulAuthorize() throws DataAccessException, SQLException {
        AuthData bobAuth = createBobAndAuth();
        AuthData daveAuth = createDaveAndAuth();

        Assertions.assertEquals(bobAuth, authDAO.authorize(bobAuth.authToken()));
        Assertions.assertEquals(daveAuth, authDAO.authorize(daveAuth.authToken()));
    }

    @Test
    @Order(16)
    @DisplayName("Unsuccessful Authorize")
    public void unsuccessfulAuthorize() throws DataAccessException, SQLException {
        AuthData bobAuth = createBobAndAuth();
        AuthData daveAuth = createDaveAndAuth();

        Assertions.assertThrows(DataAccessException.UnauthorizedException.class, () -> authDAO.authorize("hi"));
        Assertions.assertThrows(DataAccessException.UnauthorizedException.class, () -> authDAO.authorize("your mom"));
    }

    public AuthData createBobAndAuth() throws SQLException, DataAccessException {
        createBob();
        return authDAO.createAuth("Bob");
    }

    public AuthData createDaveAndAuth() throws SQLException, DataAccessException {
        createDave();
        return authDAO.createAuth("Dave");
    }

    public void createBob() throws DataAccessException, SQLException {
        userDAO.createUser("Bob", "goCougs27", "cs240@gmail.com");
    }

    public void createDave() throws DataAccessException, SQLException {
        userDAO.createUser("Dave", "yourmom", "hi@gmail.com");
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

    public void makeBobAuth() throws DataAccessException {
        String statement = "INSERT INTO auth (username, authToken) VALUES (?, ?)";
        try (var conn = DatabaseManager.getConnection()) {
            var preparedStatement = conn.prepareStatement(statement);
            preparedStatement.setString(1, "Bob");
            preparedStatement.setString(2, "BobsAuth");
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

        public void assertEquivalentUsers(UserData userData, UserData testUserData, String clearPassword) throws DataAccessException {
        Assertions.assertEquals(testUserData.username(), userData.username());
        Assertions.assertEquals(testUserData.email(), userData.email());
        Assertions.assertTrue(userDAO.verifyUser(testUserData.username(), clearPassword));
    }

    public void assertEquivalentUserSets(HashSet<UserData> userData, HashSet<UserData> testUserData) throws DataAccessException {
        for (UserData user : userData) {
            String username = user.username();
            boolean found = false;
            for (UserData testUser : testUserData) {
                if (testUser.username().equals(username)) {
                    assertEquivalentUsers(user, testUser, testUser.password());
                    found = true;
                }
            }
            Assertions.assertTrue(found);
        }
    }
}
