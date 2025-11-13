package server;

import com.google.gson.Gson;
import model.AuthData;
import response.*;
import request.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.*;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.*;

public class ServerFacade {

    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;

    public ServerFacade(String url) {
        serverUrl = "http://localhost:" + url;
    }

    public void clear() {
        var request = createHttpRequest("DELETE", "/db", null);
        sendRequest(request);
    }

    public AuthData register(RegisterRequest registerRequest) {
        var request = createHttpRequest("POST", "/user", registerRequest);
        var response = sendRequest(request);
        return handleResponse(response, AuthData.class);
    }

    public LoginResponse login(LoginRequest loginRequest) {
        var request = createHttpRequest("POST", "/session", loginRequest);
        var response = sendRequest(request);
        return handleResponse(response, LoginResponse.class);
    }

    public LogoutResponse logout(LogoutRequest logoutRequest) {
        var request = createHttpRequest("DELETE", "/session", logoutRequest);
        var response = sendRequest(request);
        return handleResponse(response, LogoutResponse.class);
    }

    public CreateGameResponse createGame(CreateGameRequest createGameRequest) {
        var request = createHttpRequest("POST", "/game", createGameRequest);
        var response = sendRequest(request);
        return handleResponse(response, CreateGameResponse.class);
    }

    public ListGamesResponse listGames(ListGamesRequest listGamesRequest) {
        var request = createHttpRequest("GET", "/game", listGamesRequest);
        var response = sendRequest(request);
        return handleResponse(response, ListGamesResponse.class);
    }

    public JoinGameResponse joinGame(JoinGameRequest joinGameRequest) {
        var request = createHttpRequest("PUT", "/game", joinGameRequest);
        var response = sendRequest(request);
        return handleResponse(response, JoinGameResponse.class);
    }

    private HttpRequest createHttpRequest(String method, String path, Object body) {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path))
                .method(method, createBody(body));
        if (body != null) {
            request.setHeader("Content-Type", "application/json");
        }
        return request.build();
    }

    private HttpRequest.BodyPublisher createBody(Object body) {
        if (body != null) {
            return BodyPublishers.ofString(new Gson().toJson(body));
        }
        return BodyPublishers.noBody();
    }

    private HttpResponse<String> sendRequest(HttpRequest request) throws ResponseException {
        try {
            return client.send(request, BodyHandlers.ofString());
        } catch (IOException | InterruptedException ex) {
            throw new ResponseException(ex.getMessage());
        }
    }

    private <T> T handleResponse(HttpResponse<String> response, Class<T> responseClass) throws ResponseException {
        var status = response.statusCode();
        if (!successful(status)) {
            var body = response.body();
            if (body != null) {
                throw new ResponseException(body);
            }
            throw new ResponseException(Integer.toString(status));
        }
        if (responseClass != null) {
            return new Gson().fromJson(response.body(), responseClass);
        }
        return null;
    }

    private boolean successful(int status) {
        return status / 100 == 2;
    }

}
