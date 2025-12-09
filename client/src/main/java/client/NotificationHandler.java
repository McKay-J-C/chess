package client;

import com.google.gson.Gson;
import websocket.messages.*;
import static client.GameplayClient.*;

public interface NotificationHandler {
    default void notify(ServerMessage serverMessage, String message) {
    }

}
