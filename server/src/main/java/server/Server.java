package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.*;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;

import request.*;
import response.*;
import service.BadRequestException;


public class Server {

    private final Javalin javalin;

//    public Server() {
//
//    }


    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"))
                .post("/user", this::registerHandler)
                .post("/session", this::loginHandler)
                .delete("/session", this::logoutHandler)
                .post("/game", this::createGameHandler)
                .get("/game", this::listGamesHandler)
                .put("/game", this::joinGameHandler)
                .delete("/db", this::clearHandler)
                .exception(Exception.class, this::exceptionHandler);
    }

    private void exceptionHandler(@NotNull Exception ex, @NotNull Context context) {
        switch (ex) {
            case BadRequestException badRequestException -> context.status(400);
            case DataAccessException.UnauthorizedException unauthorizedException -> context.status(401);
            case DataAccessException.AlreadyTakenException alreadyTakenException -> context.status(403);
            default -> context.status(500);
        }
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
        if (errorResponse.message() == null) {
            errorResponse = new ErrorResponse("Error: unidentified error");
        }
        context.json(new Gson().toJson(errorResponse));
    }

    private void registerHandler(@NotNull Context context) throws DataAccessException, BadRequestException {
        RegisterRequest registerRequest = new Gson().fromJson(context.body(), RegisterRequest.class);
        checkArg(registerRequest.username());
        checkArg(registerRequest.password());
        checkArg(registerRequest.email());

        RegisterResponse registerResponse = service.UserService.register(registerRequest);
        context.json(new Gson().toJson(registerResponse));
    }

    private void loginHandler(@NotNull Context context) throws BadRequestException {
        LoginRequest loginRequest = new Gson().fromJson(context.body(), LoginRequest.class);
        checkArg(loginRequest.username());
        checkArg(loginRequest.password());

        LoginResponse loginResponse = service.UserService.login(loginRequest);
        context.json(new Gson().toJson(loginResponse));
    }

    private void logoutHandler(@NotNull Context context) throws DataAccessException, BadRequestException {
        LogoutRequest logoutRequest = new LogoutRequest(context.header("authorization"));
        checkAuth(logoutRequest.authToken());

        LogoutResponse logoutResponse = service.UserService.logout(logoutRequest);
        context.json(new Gson().toJson(logoutResponse));
    }

    private void createGameHandler(@NotNull Context context) throws DataAccessException, BadRequestException {
        String authToken = context.header("authorization");
        CreateGameRequest createGameRequest = new Gson().fromJson(context.body(), CreateGameRequest.class);
        checkAuth(authToken);
        checkArg(createGameRequest.gameName());

        CreateGameResponse createGameResponse = service.GameService.createGame(authToken, createGameRequest);
        context.json(new Gson().toJson(createGameResponse));
    }

    private void listGamesHandler(@NotNull Context context) throws DataAccessException, BadRequestException {
        ListGamesRequest listGamesRequest = new ListGamesRequest(context.header("authorization"));
        checkAuth(listGamesRequest.authToken());

        ListGamesResponse listGamesResponse = service.GameService.listGames(listGamesRequest);
        context.json(new Gson().toJson(listGamesResponse));
    }

    private void joinGameHandler(@NotNull Context context) throws DataAccessException, BadRequestException {
        String authToken = context.header("authorization");
        JoinGameRequest joinGameRequest = new Gson().fromJson(context.body(), JoinGameRequest.class);
        checkAuth(authToken);
        checkArg(joinGameRequest.playerColor());
        checkArg(String.valueOf(joinGameRequest.gameID()));

        JoinGameResponse joinGameResponse = service.GameService.joinGame(authToken, joinGameRequest);
        context.json(new Gson().toJson(joinGameResponse));
    }

    private void clearHandler(@NotNull Context context) {
        ClearResponse clearResponse = service.ClearService.clear();
        context.json(new Gson().toJson(clearResponse));
    }

    private void checkArg(String arg) {
        if (arg == null || arg.isEmpty() || arg.equals("null")) {
            throw new BadRequestException("Error: bad request");
        }
    }

    private void checkAuth(String auth) {
        if (auth == null || auth.isEmpty()) {
            throw new BadRequestException("Error: unauthorized");
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
