package server;

import response.*;
import request.*;

import java.net.http.HttpClient;

public class ServerFacade {

    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;

    public ServerFacade(String url) {
        serverUrl = url;
    }

    public void clear() {

    }

//    public RegisterResponse;
}
