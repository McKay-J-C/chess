package service;


import chess.ChessGame;
import dataaccess.*;
import model.*;
import org.eclipse.jetty.server.Authentication;
import org.junit.jupiter.api.*;
import request.*;
import response.*;

import java.util.HashSet;

import static dataaccess.MemAuthDAO.*;
import static dataaccess.MemGameDAO.*;
import static dataaccess.MemUserDAO.*;
import static service.UserService.*;
import static service.GameService.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServiceTests {

    @AfterEach
    public void clear() {
        ClearService.clear();
    }

    @Test
    @Order(1)
    @DisplayName("Successful Registration")
    public void successfulRegistration() throws Exception {
        RegisterResponse foundResponse =  registerBob();
        UserData foundUser = MemUserDAO.getUser("Bob");
        UserData testUser = new UserData("Bob", "goCougs27", "mjc2021@byu.edu");
        Assertions.assertEquals(testUser, foundUser);

        AuthData testResponse = new AuthData("", "Bob");
        Assertions.assertEquals(testResponse.username(), foundResponse.username());
    }

    @Test
    @Order(2)
    @DisplayName("Taken Username Error Registration")
    public void usernameTakenRegistration() throws Exception {
        registerBob();
        RegisterRequest newRegisterRequest = new RegisterRequest(
                "Bob", "hello", "goBYU@byu.edu");
        Assertions.assertThrows(DataAccessException.AlreadyTakenException.class,
                () -> register(newRegisterRequest));
    }

    @Test
    @Order(3)
    @DisplayName("Successful Login")
    public void successfulLogin() throws Exception {
        registerBob();

        LoginRequest loginRequest = new LoginRequest("Bob", "goCougs27");
        LoginResponse foundResponse = login(loginRequest);
        LoginResponse testResponse = new LoginResponse("Bob", "4", null);

        Assertions.assertEquals(testResponse.username(), foundResponse.username());
        Assertions.assertEquals(testResponse.message(), foundResponse.message());
        Assertions.assertNotNull(foundResponse.authToken());
    }

    @Test
    @Order(4)
    @DisplayName("Username Not Found Login")
    public void usernameNotFoundLogin() throws Exception {
        LoginRequest loginRequest = new LoginRequest("Bob", "hi");
        Assertions.assertThrows(DataAccessException.UnauthorizedException.class,
                () -> login(loginRequest));
    }

    @Test
    @Order(5)
    @DisplayName("Incorrect Password Login")
    public void incorrectPasswordLogin() throws Exception {
        registerBob();
        LoginRequest loginRequest = new LoginRequest("Bob", "hi");

        Assertions.assertThrows(DataAccessException.UnauthorizedException.class,
                () -> login(loginRequest));
    }

    @Test
    @Order(6)
    @DisplayName("Successful Logout")
    public void successfulLogout() throws Exception {
        String authToken = registerBob().authToken();
        LogoutRequest logoutRequest = new LogoutRequest(authToken);

        LogoutResponse logoutResponse = logout(logoutRequest);

        Assertions.assertNull(logoutResponse.message());
        //Make sure authorization is deleted
        Assertions.assertThrows(DataAccessException.UnauthorizedException.class,
                () -> authorize(authToken));
    }

    @Test
    @Order(7)
    @DisplayName("Unauthorized Logout")
    public void unauthorizedLogout() throws Exception {
        registerBob();
        LogoutRequest logoutRequest = new LogoutRequest("hi");
        Assertions.assertThrows(DataAccessException.UnauthorizedException.class,
                () -> logout(logoutRequest));
    }

    @Test
    @Order(8)
    @DisplayName("Successful Game Creation")
    public void successfulCreateGame() throws Exception {
        String authToken = registerBob().authToken();
        CreateGameResponse createGameResponse1 = makeBobsGame(authToken);
        CreateGameResponse createGameResponse2 = makeBobsGame(authToken);

        //Confirm games have unique IDs
        Assertions.assertNotEquals(createGameResponse2.gameID(), createGameResponse1.gameID());
        GameData gameData = getGame(1);
        GameData expectedGameData1 = new GameData(
                1, null, null, "Bob's game", new ChessGame());

        //Confirm correct creation of game
        Assertions.assertEquals(expectedGameData1, gameData);
    }

    @Test
    @Order(9)
    @DisplayName("Unauthorized Game Creation")
    public void unauthorizedCreateGame() throws Exception {
        registerBob();
        CreateGameRequest createGameRequest = new CreateGameRequest("Bob's game");
        Assertions.assertThrows(DataAccessException.UnauthorizedException.class,
                () -> createGame("hi", createGameRequest));
    }

    @Test
    @Order(10)
    @DisplayName("Successful List Games")
    public void successfulListGames() throws Exception {
        String authToken = registerBob().authToken();
        ListGamesRequest listGamesRequest = new ListGamesRequest(authToken);
        ListGamesResponse listGamesResponse = listGames(listGamesRequest);
        Assertions.assertEquals(new HashSet<>(), listGamesResponse.games());

        makeBobsGame(authToken);
        HashSet<GameData> testGameData = new HashSet<>();
        GameData testGame = new GameData(
                1, null, null, "Bob's game", new ChessGame());
        testGameData.add(testGame);
        Assertions.assertEquals(new ListGamesResponse(testGameData, null), listGamesResponse);
    }

    @Test
    @Order(11)
    @DisplayName("Unauthorized List Games")
    public void unauthorizedListGames() throws Exception {
        registerBob();
        ListGamesRequest listGamesRequest = new ListGamesRequest("hi");
        Assertions.assertThrows(DataAccessException.UnauthorizedException.class,
                () -> listGames(listGamesRequest));
    }

    @Test
    @Order(12)
    @DisplayName("Successful Join Game")
    public void successfulJoinGame() throws Exception {
        JoinGameResponse joinGameResponse = registerMakeAndJoinGame();
        GameData gameData = getGame(1);
        Assertions.assertNotNull(gameData);
        Assertions.assertEquals("Bob", gameData.whiteUsername());
        Assertions.assertNull(joinGameResponse.message());
    }

    @Test
    @Order(13)
    @DisplayName("Unauthorized Join Game")
    public void unauthorizedJoinGame() throws Exception {
        registerMakeAndJoinGame();
        JoinGameRequest joinGameRequest = new JoinGameRequest("BLACK", 1);
        Assertions.assertThrows(DataAccessException.UnauthorizedException.class,
                () -> joinGame("hi", joinGameRequest));
    }

    @Test
    @Order(14)
    @DisplayName("No Game Join Game")
    public void noGameJoinGame() throws Exception {
        String authToken = registerBob().authToken();
        makeBobsGame(authToken);
        JoinGameRequest joinGameRequest = new JoinGameRequest("WHITE", 2);
        Assertions.assertThrows(BadRequestException.class,
                () -> joinGame(authToken, joinGameRequest));
    }

    @Test
    @Order(15)
    @DisplayName("Already Taken Join Game")
    public void alreadyTakenJoinGame() throws Exception {
        String authToken = registerBob().authToken();
        makeBobsGame(authToken);
        joinBobsGame(authToken);

        Assertions.assertThrows(DataAccessException.AlreadyTakenException.class,
                () -> joinBobsGame(authToken));
    }

    @Test
    @Order(16)
    @DisplayName("Successful Clear")
    public void successfulClear() throws Exception {
        registerMakeAndJoinGame();
        ClearService.clear();
        assert(getGames().isEmpty());
        assert(getUsers().isEmpty());
        assert(getAuths().isEmpty());
    }

    static JoinGameResponse registerMakeAndJoinGame() throws DataAccessException {
        String authToken = registerBob().authToken();
        makeBobsGame(authToken);
        return joinBobsGame(authToken);
    }

    static JoinGameResponse joinBobsGame(String authToken) throws DataAccessException, BadRequestException {
        JoinGameRequest joinGameRequest = new JoinGameRequest("WHITE", 1);
        return joinGame(authToken, joinGameRequest);
    }

    static RegisterResponse registerBob() throws DataAccessException {
        return register(new RegisterRequest(
                "Bob", "goCougs27", "mjc2021@byu.edu"));
    }

    static CreateGameResponse makeBobsGame(String authToken) throws DataAccessException {
        CreateGameRequest createGameRequest = new CreateGameRequest("Bob's game");
        return createGame(authToken, createGameRequest);
    }

}
