package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.*;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;

import request.CreateGameRequest;
import request.LoginRequest;
import request.LogoutRequest;
import request.RegisterRequest;
import response.*;
import service.BadRequestException;

import static dataaccess.MemAuthDAO.authorize;


public class Server {

    private final Javalin javalin;


    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"))
                .post("/user", this::registerHandler)
                .post("/session", this::loginHandler)
                .delete("/session", this::logoutHandler)
                .post("/game", this::createGameHandler)
                .exception(Exception.class, this::exceptionHandler);
        // Register your endpoints and exception handlers here.

    }

    private void exceptionHandler(@NotNull Exception ex, @NotNull Context context) {
        if (ex instanceof BadRequestException) {
            context.status(400);
        }
        else if (ex instanceof DataAccessException.UnauthorizedException) {
            context.status(401);
        }
        else if (ex instanceof DataAccessException.AlreadyTakenException) {
            context.status(403);
        }
        else {
            context.status(500);
        }
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
        if (errorResponse.message() == null) {
            errorResponse = new ErrorResponse("Error: unidentified error");
        }
        context.json(new Gson().toJson(errorResponse));
    }

    private void registerHandler(@NotNull Context context) throws DataAccessException, BadRequestException {
        RegisterRequest registerRequest = new Gson().fromJson(context.body(), RegisterRequest.class);
        if (registerRequest.username() == null || registerRequest.password() == null || registerRequest.email() == null) {
            throw new BadRequestException("Error: bad request");
        }
        RegisterResponse registerResponse = service.UserService.register(registerRequest);
        context.json(new Gson().toJson(registerResponse));
    }

    private void loginHandler(@NotNull Context context) throws DataAccessException, BadRequestException {
        LoginRequest loginRequest = new Gson().fromJson(context.body(), LoginRequest.class);
        if (loginRequest.username() == null || loginRequest.password() == null) {
            throw new BadRequestException("Error: bad request");
        }
        LoginResponse loginResponse = service.UserService.login(loginRequest);
        context.json(new Gson().toJson(loginResponse));
    }

    private void logoutHandler(@NotNull Context context) throws DataAccessException, BadRequestException {
        LogoutRequest logoutRequest = new LogoutRequest(context.header("authorization"));
        if (logoutRequest.authToken() == null) {
            throw new BadRequestException("Error: bad request");
        }
        LogoutResponse logoutResponse = service.UserService.logout(logoutRequest);
        context.json(new Gson().toJson(logoutResponse));
    }

    private void createGameHandler(@NotNull Context context) throws DataAccessException, BadRequestException {
        String authToken = context.header("authorization");
        CreateGameRequest createGameRequest = new Gson().fromJson(context.body(), CreateGameRequest.class);
        checkAuth(authToken);
        if (createGameRequest.gameName() == null) {
            throw new BadRequestException("Error: bad request");
        }
        CreateGameResponse createGameResponse = service.GameService.createGame(authToken, createGameRequest);
        context.json(new Gson().toJson(createGameResponse));
    }



    private void checkAuth(String authToken) {
        if (authToken == null || authToken.isEmpty()) {
            throw new DataAccessException.UnauthorizedException("Error: unauthorized");
        }
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }

}
