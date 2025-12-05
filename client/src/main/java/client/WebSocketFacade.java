package client;

import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.Session;

public class WebSocketFacade extends Endpoint {

    Session session;

    public WebSocketFacade(String url) {
        try {
            url = url.replace("http", "ws");
        } catch (Exception ex){

        }
    }


    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }
}
