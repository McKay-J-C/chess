package dataaccess;


import chess.ChessGame;
import com.google.gson.Gson;

import model.AuthData;
import model.GameData;
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
    public void successfulCreateUser() throws DataAccessException {
        createBob();
        UserData userData = userDAO.getUser("Bob");
        UserData testUserData = new UserData("Bob", "goCougs27", "cs240@gmail.com");
        assertEquivalentUsers(userData, testUserData, "goCougs27");
    }

    @Test
    @Order(4)
    @DisplayName("Taken Create User")
    public void takenCreateUser() throws DataAccessException {
        createBob();
        Assertions.assertThrows(DataAccessException.AlreadyTakenException.class, this::createBob);
    }

    @Test
    @Order(5)
    @DisplayName("Successful Get Users")
    public void successfulGetUsers() throws DataAccessException {
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
    public void noUsersGetUsers() throws DataAccessException {
        Assertions.assertNull(userDAO.getUsers());
    }

    @Test
    @Order(7)
    @DisplayName("User Clear")
    public void testUserClear() throws DataAccessException {
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
    public void successfulGetAuth() throws DataAccessException {
        makeUserBob();
        makeBobAuth();
        AuthData testAuth = new AuthData("BobsAuth", "Bob");
        AuthData auth = authDAO.getAuth("BobsAuth");
        Assertions.assertEquals(testAuth, auth);
    }

    @Test
    @Order(9)
    @DisplayName("Unsuccessful Get Auth")
    public void unsuccessfulGetAuth() throws DataAccessException {
        makeUserBob();
        makeBobAuth();
        Assertions.assertNull(authDAO.getAuth("hi"));
    }

    @Test
    @Order(10)
    @DisplayName("Successful Create Auth")
    public void successfulCreateAuth() throws DataAccessException {
        AuthData authData = createBobAndAuth();
        AuthData foundAuthData = authDAO.getAuth(authData.authToken());
        Assertions.assertEquals(authData, foundAuthData);
    }

    @Test
    @Order(11)
    @DisplayName("No User Create Auth")
    public void noUserCreateAuth() {
        Assertions.assertThrows(DataAccessException.class, () -> authDAO.createAuth("Bob"));
    }

    @Test
    @Order(12)
    @DisplayName("Successful Get Auths")
    public void successfulGetAuths() throws DataAccessException {
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
    public void noAuthGetAuths() throws DataAccessException {
        Assertions.assertNull(authDAO.getAuths());
    }

    @Test
    @Order(14)
    @DisplayName("Successful Delete Auth")
    public void successfulDeleteAuth() throws DataAccessException {
        AuthData bobAuth = createBobAndAuth();
        authDAO.deleteAuth(bobAuth);
        Assertions.assertNull(authDAO.getAuth(bobAuth.authToken()));
    }

    @Test
    @Order(15)
    @DisplayName("Wrong AuthToken Delete Auth")
    public void wrongAuthDeleteAuth() throws DataAccessException {
        AuthData bobAuth = createBobAndAuth();
        AuthData authData = new AuthData("Bob", "hi");
        authDAO.deleteAuth(authData);
        Assertions.assertEquals(bobAuth, authDAO.getAuth(bobAuth.authToken()));
    }

    @Test
    @Order(16)
    @DisplayName("Successful Authorize")
    public void successfulAuthorize() throws DataAccessException {
        AuthData bobAuth = createBobAndAuth();
        AuthData daveAuth = createDaveAndAuth();

        Assertions.assertEquals(bobAuth, authDAO.authorize(bobAuth.authToken()));
        Assertions.assertEquals(daveAuth, authDAO.authorize(daveAuth.authToken()));
    }

    @Test
    @Order(16)
    @DisplayName("Unsuccessful Authorize")
    public void unsuccessfulAuthorize() throws DataAccessException {
        createBobAndAuth();
        createDaveAndAuth();

        Assertions.assertThrows(DataAccessException.UnauthorizedException.class, () -> authDAO.authorize("hi"));
        Assertions.assertThrows(DataAccessException.UnauthorizedException.class, () -> authDAO.authorize("your mom"));
    }

    @Test
    @Order(17)
    @DisplayName("Auth Clear")
    public void testAuthClear() throws DataAccessException {
        createBobAndAuth();
        createDaveAndAuth();
        authDAO.clear();
        Assertions.assertNull(authDAO.getAuths());
        Assertions.assertNull(authDAO.getAuth("Bob"));
        Assertions.assertNull(authDAO.getAuth("Dave"));
    }

    @Test
    @Order(18)
    @DisplayName("Successful Get Game")
    public void successfulGetGame() throws DataAccessException {
        createBobAndAuth();
        makeBobGame();
        GameData testGameData = new GameData(1, "Bob", null, "Bobs Game", new ChessGame());
        GameData foundGameData = gameDAO.getGame(1);
        Assertions.assertEquals(testGameData, foundGameData);
    }

    @Test
    @Order(18)
    @DisplayName("Wrong GameID Get Game")
    public void wrongIdGetGame() throws DataAccessException {
        createBobAndAuth();
        makeBobGame();
        Assertions.assertNull(gameDAO.getGame(9));
    }

    @Test
    @Order(19)
    @DisplayName("Successful Create Game")
    public void successfulCreateGame() throws DataAccessException {
        createBobAndAuth();
        int gameID = gameDAO.addGame("Bobs game");
        GameData gameData = gameDAO.getGame(gameID);

        GameData testGameData = new GameData(1, null, null, "Bobs game", new ChessGame());
        Assertions.assertEquals(testGameData, gameData);
    }

    @Test
    @Order(20)
    @DisplayName("Successful Get Games")
    public void successfulGetGames() throws DataAccessException {
        createBobAndAuth();
        createDaveAndAuth();
        gameDAO.addGame("Bobs game");
        gameDAO.addGame("Daves game");

        GameData testBobGame = new GameData(1, null, null, "Bobs game", new ChessGame());
        GameData testDaveGame = new GameData(2, null, null, "Daves game", new ChessGame());
        HashSet<GameData> gameData = gameDAO.getGames();

        Assertions.assertTrue(gameData.contains(testBobGame));
        Assertions.assertTrue(gameData.contains(testDaveGame));
        Assertions.assertEquals(2, gameData.size());
    }

    @Test
    @Order(20)
    @DisplayName("No Games Get Games")
    public void noGamesGetGames() throws DataAccessException {
        createBobAndAuth();
        createDaveAndAuth();
        HashSet<GameData> gameData = gameDAO.getGames();
        Assertions.assertNull(gameData);
    }

    @Test
    @Order(21)
    @DisplayName("Successful Update Game")
    public void successfulUpdateGame() throws DataAccessException {
        createBobAndAuth();
        createDaveAndAuth();
        int gameID = gameDAO.addGame("Bobs game");
        gameDAO.updateGame(gameID, ChessGame.TeamColor.WHITE, "Bob");

        GameData gameData1 = gameDAO.getGame(gameID);
        GameData testGameData1 = new GameData(1, "Bob", null, "Bobs game", new ChessGame());
        Assertions.assertEquals(testGameData1, gameData1);

        gameDAO.updateGame(gameID, ChessGame.TeamColor.BLACK, "Dave");
        GameData gameData2 = gameDAO.getGame(gameID);
        GameData testGameData2 = new GameData(1, "Bob", "Dave", "Bobs game", new ChessGame());
        Assertions.assertEquals(testGameData2, gameData2);
    }

    @Test
    @Order(22)
    @DisplayName("Player Taken Update Game")
    public void playerTakenUpdateGame() throws DataAccessException {
        createBobAndAuth();
        createDaveAndAuth();
        int gameID = gameDAO.addGame("Bobs game");
        gameDAO.updateGame(gameID, ChessGame.TeamColor.WHITE, "Bob");

        Assertions.assertThrows(DataAccessException.AlreadyTakenException.class,
                () -> gameDAO.updateGame(gameID, ChessGame.TeamColor.WHITE, "Dave"));
    }

    @Test
    @Order(23)
    @DisplayName("Game Does Not Exist Update Game")
    public void gameNoExistUpdateGame() throws DataAccessException {
        createBobAndAuth();
        createDaveAndAuth();
        gameDAO.addGame("Bobs game");

        Assertions.assertThrows(DataAccessException.class,
                () -> gameDAO.updateGame(7, ChessGame.TeamColor.WHITE, "Bob"));
    }

    @Test
    @Order(24)
    @DisplayName("User Does Not Exist Update Game")
    public void userNoExistUpdateGame() throws DataAccessException {
        createBobAndAuth();
        gameDAO.addGame("Bobs game");

        Assertions.assertThrows(DataAccessException.class,
                () -> gameDAO.updateGame(7, ChessGame.TeamColor.WHITE, "Dave"));
    }

    @Test
    @Order(25)
    @DisplayName("Game Clear")
    public void testGameClear() throws DataAccessException {
        createBobAndAuth();
        createDaveAndAuth();
        gameDAO.addGame("Bobs game");
        gameDAO.addGame("Daves game");

        gameDAO.updateGame(1, ChessGame.TeamColor.WHITE, "Bob");
        gameDAO.updateGame(1, ChessGame.TeamColor.BLACK, "Dave");
        gameDAO.updateGame(2, ChessGame.TeamColor.WHITE, "Dave");
        gameDAO.updateGame(2, ChessGame.TeamColor.BLACK, "Bob");
        gameDAO.clear();

        Assertions.assertNull(gameDAO.getGames());
        Assertions.assertNull(gameDAO.getGame(1));
        Assertions.assertNull(gameDAO.getGame(2));
    }

    public AuthData createBobAndAuth() throws DataAccessException {
        createBob();
        return authDAO.createAuth("Bob");
    }

    public AuthData createDaveAndAuth() throws DataAccessException {
        createDave();
        return authDAO.createAuth("Dave");
    }

    public void createBob() throws DataAccessException {
        userDAO.createUser("Bob", "goCougs27", "cs240@gmail.com");
    }

    public void createDave() throws DataAccessException {
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

    public void makeBobGame() throws DataAccessException {
        String statement = "INSERT INTO game (gameID, whiteUsername, gameName, game) VALUES (?,?,?,?)";
        try (var conn = DatabaseManager.getConnection()) {
            var preparedStatement = conn.prepareStatement(statement);
            preparedStatement.setInt(1, 1);
            preparedStatement.setString(2, "Bob");
            preparedStatement.setString(3, "Bobs Game");

            ChessGame game = new ChessGame();
            var serializer = new Gson();
            var gameJson = serializer.toJson(game);
            preparedStatement.setString(4, gameJson);
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
