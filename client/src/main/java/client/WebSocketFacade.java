package client;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import jakarta.websocket.*;
import server.ResponseException;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.commands.UserGameCommand.CommandType;

import javax.imageio.IIOException;
import javax.management.Notification;
import java.io.IOException;
import java.net.URI;

public class WebSocketFacade extends Endpoint {

    Session session;
    NotificationHandler notificationHandler;

    public WebSocketFacade(String url, NotificationHandler notificationHandler) {
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            this.notificationHandler = notificationHandler;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);
            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    Notification notification = new Gson().fromJson(message, Notification.class);
                    notificationHandler.notify(notification);
                }
            });
        } catch (Exception ex){
            throw new ResponseException(ex.getMessage());
        }
    }

    //Don't need to do anything with this
    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }

    public void connect(String authToken, int gameID, ChessGame.TeamColor color) {
        sendCommand(CommandType.CONNECT, authToken, gameID, color);
    }

    public void makeMove(String authToken, int gameID, ChessGame.TeamColor color, ChessMove move) {
        try {
            UserGameCommand userGameCommand = new MakeMoveCommand(CommandType.CONNECT, authToken, gameID, move, color);
            this.session.getBasicRemote().sendText(new Gson().toJson(userGameCommand));
        } catch (IOException ex) {
            throw new ResponseException(ex.getMessage());
        }
    }

    public void leave(String authToken, int gameID, ChessGame.TeamColor color) {
        sendCommand(CommandType.LEAVE, authToken, gameID, color);
    }

    public void resign(String authToken, int gameID, ChessGame.TeamColor color) {
        sendCommand(CommandType.RESIGN, authToken, gameID, color);
    }

    private void sendCommand(CommandType type, String authToken, int gameID, ChessGame.TeamColor color) {
        try {
            UserGameCommand userGameCommand = new UserGameCommand(type, authToken, gameID, color);
            this.session.getBasicRemote().sendText(new Gson().toJson(userGameCommand));
        } catch (IOException ex) {
            throw new ResponseException(ex.getMessage());
        }
    }
}
