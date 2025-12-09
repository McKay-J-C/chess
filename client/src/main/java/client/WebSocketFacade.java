package client;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import jakarta.websocket.*;
import server.ResponseException;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.commands.UserGameCommand.CommandType;
import websocket.messages.*;
import java.io.IOException;
import java.net.URI;

public class WebSocketFacade extends Endpoint {

    Session session;
    NotificationHandler notificationHandler;

    public WebSocketFacade(String url, NotificationHandler notificationHandler) {
        try {
//            System.out.println("Original url: " + url);
            url = "ws://localhost:" + url;
//            System.out.println("New url: " + url);
            URI socketURI = new URI(url + "/ws");
            this.notificationHandler = notificationHandler;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);
            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    ServerMessage serverMessage = new Gson().fromJson(message, ServerMessage.class);
                    notificationHandler.notify(serverMessage, message);
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
            UserGameCommand userGameCommand = new MakeMoveCommand(CommandType.MAKE_MOVE, authToken, gameID, move, color);
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
//            System.out.println("session = " + session);
//            System.out.println("session.isOpen() = " + session.isOpen());
            String commandJson = new Gson().toJson(userGameCommand);
//            System.out.println("JSON OUT: " + commandJson);
            this.session.getBasicRemote().sendText(commandJson);
        } catch (IOException ex) {
            throw new ResponseException(ex.getMessage());
        }
    }
}
