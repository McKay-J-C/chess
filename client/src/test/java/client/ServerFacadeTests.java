package client;

import org.junit.jupiter.api.*;
import request.RegisterRequest;
import response.RegisterResponse;
import server.ResponseException;
import server.Server;
import server.ServerFacade;
import response.*;
import request.*;

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

//    @Test
//    public void successfulLogin() {
//        registerBob();
//    }
    private RegisterResponse registerBob() {
        return facade.register(new RegisterRequest("Bob", "goCougs27", "gamil@gmail.com"));
    }
//    private AuthData registerBob() {
//        return facade.register(new RegisterRequest("Bob", "goCougs27", "gamil@gmail.com"));
//    }
}
