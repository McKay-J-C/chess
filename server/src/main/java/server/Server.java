package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.*;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;

import request.LoginRequest;
import request.LogoutRequest;
import request.RegisterRequest;
import response.ErrorResponse;
import response.LoginResponse;
import response.LogoutResponse;
import response.RegisterResponse;
import service.BadRequestException;


public class Server {

    private final Javalin javalin;


    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"))
                .post("/user", this::registerHandler)
                .post("/session", this::loginHandler)
                .delete("/session", this::logoutHandler)
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



    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }

}
