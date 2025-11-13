package client;

import dataaccess.DataAccessException;
import model.AuthData;
import org.junit.jupiter.api.*;
import request.RegisterRequest;
import server.ResponseException;
import server.Server;
import server.ServerFacade;


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

    @Test
    public void successfulRegister() {
        AuthData authData = registerBob();
        Assertions.assertEquals("Bob", authData.username());
        Assertions.assertTrue(authData.authToken().length() > 10);
    }

    @Test
    public void unsuccessfulRegiester() {
        AuthData authData = registerBob();
        Assertions.assertThrows(ResponseException.class, this::registerBob);
    }

    private AuthData registerBob() {
        return facade.register(new RegisterRequest("Bob", "goCougs27", "gamil@gmail.com"));
    }
}
