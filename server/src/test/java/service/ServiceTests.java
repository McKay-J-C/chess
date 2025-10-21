package service;


import chess.ChessGame;
import dataaccess.MemUserDAO;
import dataaccess.*;
import model.*;
import org.eclipse.jetty.server.Authentication;
import org.junit.jupiter.api.*;
import request.*;
import response.*;

import static dataaccess.MemAuthDAO.authorize;
import static dataaccess.MemGameDAO.getGame;
import static service.UserService.*;
import static service.GameService.*;
import static service.ClearService.clear;

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
        RegisterResponse registerResponse = registerBob();
        CreateGameRequest createGameRequest = new CreateGameRequest(
                registerResponse.authToken(), "Bob's game");
        CreateGameResponse createGameResponse1 = createGame(createGameRequest);
        CreateGameResponse createGameResponse2 = createGame(createGameRequest);

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
        CreateGameRequest createGameRequest = new CreateGameRequest("hi", "funny game");
        Assertions.assertThrows(DataAccessException.UnauthorizedException.class,
                () -> createGame(createGameRequest));
    }



    static RegisterResponse registerBob() throws DataAccessException {
        return register(new RegisterRequest(
                "Bob", "goCougs27", "mjc2021@byu.edu"));
    }

}
