package client;

import chess.ChessGame;
import model.GameData;
import org.junit.jupiter.api.*;
import request.RegisterRequest;
import response.RegisterResponse;
import server.ResponseException;
import server.Server;
import server.ServerFacade;
import response.*;
import request.*;

import java.util.HashSet;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(Integer.toString(port));
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void clearDatabase() {
        facade.clear();
    }

    @Test
    @Order(1)
    public void successfulRegister() {
        RegisterResponse registerResponse = registerBob();
        Assertions.assertEquals("Bob", registerResponse.username());
        Assertions.assertTrue(registerResponse.authToken().length() > 10);
    }

    @Test
    @Order(2)
    public void unsuccessfulRegister() {
        registerBob();
        Assertions.assertThrows(ResponseException.class, this::registerBob);
    }

    @Test
    @Order(3)
    public void successfulLogin() {
        registerBob();
        LoginResponse loginResponse = facade.login(new LoginRequest("Bob", "goCougs27"));
        Assertions.assertEquals("Bob", loginResponse.username());
        Assertions.assertTrue(loginResponse.authToken().length() > 10);
    }

    @Test
    @Order(4)
    public void unsuccessfulLogin() {
        registerBob();
        Assertions.assertThrows(ResponseException.class, () ->
                facade.login(new LoginRequest("Bob", "hi")));
    }

    @Test
    @Order(5)
    public void successfulLogout() {
        RegisterResponse registerResponse = registerBob();
        LogoutResponse logoutResponse = facade.logout(new LogoutRequest(registerResponse.authToken()));
        Assertions.assertTrue(logoutResponse.message() == null);
    }

    @Test
    @Order(6)
    public void unsuccessfulLogout() {
        RegisterResponse registerResponse = registerBob();
        Assertions.assertThrows(ResponseException.class,
                () -> facade.logout(new LogoutRequest("hi")));
    }

    @Test
    @Order(7)
    public void successfulCreateGame() {
        CreateGameResponse createGameResponse = registerCreateBobsGame();
        Assertions.assertEquals(1, createGameResponse.gameID());
    }

    @Test
    @Order(8)
    public void unsuccessfulCreateGame() {
        registerBob();
        Assertions.assertThrows(ResponseException.class, () -> createBobsGame("hi"));
    }

    @Test
    @Order(9)
    public void successfulListGames() {
        String auth = registerBob().authToken();
        createBobsGame(auth);
        registerCreateDavesGame();
        ListGamesResponse listGamesResponse = facade.listGames(new ListGamesRequest(auth), auth);

        HashSet<GameData> testGames = new HashSet<>();
        testGames.add(new GameData(1, null, null, "Bobs Game", new ChessGame()));
        testGames.add(new GameData(2, null, null, "Daves Game", new ChessGame()));

        Assertions.assertEquals(testGames, listGamesResponse.games());
    }

    @Test
    @Order(10)
    public void unsuccessfulListGames() {
        registerCreateBobsGame();
        registerCreateDavesGame();

        Assertions.assertThrows(ResponseException.class, () -> facade.listGames(new ListGamesRequest("hi"), "hi"));
    }

    @Test
    @Order(11)
    public void successfulJoinGame() {
        String auth = registerBob().authToken();
        JoinGameResponse joinBobGameResponse = createJoinBobsGame(auth);
        JoinGameResponse joinDaveGameResponse = registerJoinDavesGame();
        Assertions.assertNull(joinBobGameResponse.message());
        Assertions.assertNull(joinDaveGameResponse.message());

        HashSet<GameData> testGames = new HashSet<>();
        testGames.add(new GameData(1, "Bob", null, "Bobs Game", new ChessGame()));
        testGames.add(new GameData(2, null, "Dave", "Daves Game", new ChessGame()));

        Assertions.assertEquals(testGames, facade.listGames(new ListGamesRequest(auth), auth).games());
    }

    @Test
    @Order(12)
    public void unsuccessfulJoinGame() {
        String bobAuth = registerBob().authToken();
        String daveAuth = registerDave().authToken();
        createJoinBobsGame(bobAuth);
        createJoinDavesGame(daveAuth);

        Assertions.assertThrows(ResponseException.class,
                () -> facade.joinGame(new JoinGameRequest("WHITE", 1), bobAuth));
        Assertions.assertThrows(ResponseException.class,
                () -> facade.joinGame(new JoinGameRequest("BLACK", 2), daveAuth));
    }

    @Test
    @Order(13)
    public void successfulClear() {
        String auth = registerBob().authToken();
        createJoinBobsGame(auth);
        registerJoinDavesGame();

        HashSet<GameData> testGames = new HashSet<>();
        testGames.add(new GameData(1, "Bob", null, "Bobs Game", new ChessGame()));
        testGames.add(new GameData(2, null, "Dave", "Daves Game", new ChessGame()));
        Assertions.assertEquals(testGames, facade.listGames(new ListGamesRequest(auth), auth).games());

        facade.clear();
        Assertions.assertThrows(ResponseException.class,
                () -> facade.listGames(new ListGamesRequest(auth), auth));

        String newAuth = registerBob().authToken();
        Assertions.assertEquals(new HashSet<>(), facade.listGames(new ListGamesRequest(newAuth), newAuth).games());
        createJoinBobsGame(newAuth);

        HashSet<GameData> testGames2 = new HashSet<>();
        testGames2.add(new GameData(1, null, null, "Bobs Game", new ChessGame()));
        Assertions.assertEquals(facade.listGames(new ListGamesRequest(auth), auth).games(), testGames2);
    }

    private RegisterResponse registerBob() {
        return facade.register(new RegisterRequest("Bob", "goCougs27", "gamil@gmail.com"));
    }

    private CreateGameResponse createBobsGame(String auth) {
        return facade.createGame(new CreateGameRequest("Bobs Game"), auth);
    }

    private RegisterResponse registerDave() {
        return facade.register(new RegisterRequest("Dave", "hello", "hi@hi.com"));
    }

    private CreateGameResponse createDavesGame(String auth) {
        return facade.createGame(new CreateGameRequest("Daves Game"), auth);
    }

    private JoinGameResponse joinBobsGames(String auth) {
        return facade.joinGame(new JoinGameRequest("WHITE", 1), auth);
    }

    private JoinGameResponse joinDavesGames(String auth) {
        return facade.joinGame(new JoinGameRequest("BLACK", 2), auth);
    }

    private CreateGameResponse registerCreateBobsGame() {
        RegisterResponse registerResponse = registerBob();
        return createBobsGame(registerResponse.authToken());
    }

    private CreateGameResponse registerCreateDavesGame() {
        RegisterResponse registerResponse = registerDave();
        return createDavesGame(registerResponse.authToken());
    }

    private JoinGameResponse registerJoinBobsGame() {
        RegisterResponse registerResponse = registerBob();
        return createJoinBobsGame(registerResponse.authToken());
    }

    private JoinGameResponse createJoinBobsGame(String auth) {
        createBobsGame(auth);
        return joinBobsGames(auth);
    }

    private JoinGameResponse createJoinDavesGame(String auth) {
        createDavesGame(auth);
        return joinDavesGames(auth);
    }

    private JoinGameResponse registerJoinDavesGame() {
        RegisterResponse registerResponse = registerDave();
        return createJoinDavesGame(registerResponse.authToken());
    }
//    private AuthData registerBob() {
//        return facade.register(new RegisterRequest("Bob", "goCougs27", "gamil@gmail.com"));
//    }
}
