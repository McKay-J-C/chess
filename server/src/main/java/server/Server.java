package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.*;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;
import request.RegisterRequest;
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
        if (ex.getClass() ==  BadRequestException.class) {
            context.status(400);
        }
        else if (ex.getClass() == DataAccessException.AlreadyTakenException.class) {
            context.status(403);
        }
        context.json(new Gson.toJson());

    }

    private void register(@NotNull Context context) throws DataAccessException {
        try {
            RegisterRequest registerRequest = new Gson().fromJson(context.body(), RegisterRequest.class);
            RegisterResponse registerResponse = service.UserService.register(registerRequest);
            context.json(new Gson().toJson(registerResponse));
        } catch {
            throw new BadRequestException("Error: bad request");
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
