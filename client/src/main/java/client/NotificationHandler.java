package client;

import com.google.gson.Gson;
import websocket.messages.*;
import static client.GameplayClient.*;
import javax.management.Notification;

public interface NotificationHandler {
    default void notify(Notification notification) {
        String message = notification.getMessage();
        ServerMessage serverMessage = new Gson().fromJson(message, ServerMessage.class);

        switch (serverMessage.getServerMessageType()) {
            case ERROR -> handleErrorMessage(new Gson().fromJson(message, ErrorMessage.class));
            case LOAD_GAME -> handleLoadGameMessage(new Gson().fromJson(message, LoadGameMessage.class));
            case NOTIFICATION -> handleNotificationMessage(new Gson().fromJson(message, NotificationMessage.class));
            default -> throw new RuntimeException("Unknown Server Message");
        }
    }

    private void handleErrorMessage(ErrorMessage errorMessage) {
        System.out.println(errorMessage.getErrorMessage());
    }

    private void handleNotificationMessage(NotificationMessage notificationMessage) {
        System.out.println(notificationMessage.getMessage());
    }

    private void handleLoadGameMessage(LoadGameMessage loadGameMessage) {
        printGame(loadGameMessage.getGame().getBoard(), loadGameMessage.getColor());
    }
}
