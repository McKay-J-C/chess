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

//import static dataaccess.MemDatabase.authData;


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

    @BeforeAll
    static void clearDatabase() {
        facade.clear();
    }

//    @AfterAll
//    static void clearADatabase() {
//        facade.clear();
//    }

//    @Test
//    public void successfulRegister() {
//        AuthData authData = registerBob();
//        Assertions.assertEquals("Bob", authData.username());
//        Assertions.assertTrue(authData.authToken().length() > 10);
//    }
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
        JoinGameResponse joinGameResponse = registerJoinBobsGame();

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
        createBobsGame(registerResponse.authToken());
        return joinBobsGames(registerResponse.authToken());
    }
//    private AuthData registerBob() {
//        return facade.register(new RegisterRequest("Bob", "goCougs27", "gamil@gmail.com"));
//    }
}
