package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.*;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;
import request.RegisterRequest;
import response.ErrorResponse;
import response.RegisterResponse;
import service.BadRequestException;
import service.UserService;

import static io.javalin.apibuilder.ApiBuilder.post;

public class Server {

    private final Javalin javalin;


    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"))
                .post("/user", this::register)
                .exception(Exception.class, this::exceptionHandler);
        // Register your endpoints and exception handlers here.


    }

    private void exceptionHandler(@NotNull Exception ex, @NotNull Context context) {
        if (ex instanceof BadRequestException) {
            context.status(400);
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

    private void register(@NotNull Context context) throws DataAccessException, BadRequestException {
        RegisterRequest registerRequest = new Gson().fromJson(context.body(), RegisterRequest.class);
        if (registerRequest.username() == null || registerRequest.password() == null || registerRequest.email() == null) {
            throw new BadRequestException("Error: bad request");
        }
        RegisterResponse registerResponse = service.UserService.register(registerRequest);
        context.json(new Gson().toJson(registerResponse));
    }


    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }


}
