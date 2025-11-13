package server;

import com.google.gson.Gson;
import model.AuthData;
import response.*;
import request.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
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
        var request = createHttpRequest("DELETE", "/db", null, null);
        sendRequest(request);
    }

    public RegisterResponse register(RegisterRequest registerRequest) {
        var request = createHttpRequest("POST", "/user", registerRequest, null);
        var response = sendRequest(request);
        return handleResponse(response, RegisterResponse.class);
    }

    public LoginResponse login(LoginRequest loginRequest) {
        var request = createHttpRequest("POST", "/session", loginRequest, null);
        var response = sendRequest(request);
        return handleResponse(response, LoginResponse.class);
    }

    public LogoutResponse logout(LogoutRequest logoutRequest) {
        var request = createHttpRequest("DELETE", "/session", logoutRequest, logoutRequest.authToken());
        var response = sendRequest(request);
        return handleResponse(response, LogoutResponse.class);
    }

    public CreateGameResponse createGame(CreateGameRequest createGameRequest, String auth) {
        var request = createHttpRequest("POST", "/game", createGameRequest, auth);
        var response = sendRequest(request);
        return handleResponse(response, CreateGameResponse.class);
    }

    public ListGamesResponse listGames(ListGamesRequest listGamesRequest, String auth) {
        var request = createHttpRequest("GET", "/game", listGamesRequest, auth);
        var response = sendRequest(request);
        return handleResponse(response, ListGamesResponse.class);
    }

    public JoinGameResponse joinGame(JoinGameRequest joinGameRequest, String auth) {
        var request = createHttpRequest("PUT", "/game", joinGameRequest, auth);
        var response = sendRequest(request);
        return handleResponse(response, JoinGameResponse.class);
    }

    private HttpRequest createHttpRequest(String method, String path, Object body, String header) {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path))
                .method(method, createBody(body));
        if (header != null) {
            request.setHeader("authorization", header);
        }
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

    private boolean hasAuth(HttpRequest request) {
        HttpHeaders headers = request.headers();
        return headers.firstValue("authorization").isPresent();
    }

}
