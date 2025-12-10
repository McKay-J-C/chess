package server.websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import org.eclipse.jetty.websocket.api.Session;
import websocket.commands.*;
import websocket.messages.*;

import java.io.IOException;
import java.util.HashMap;

public class ConnectionManager {
    public final HashMap<Integer, GameConnections> connections = new HashMap<>();

    public void addGame(int gameID) {
        connections.put(gameID, new GameConnections());
    }

    public void add(Session session, int gameID, ChessGame.TeamColor color) {
        GameConnections gameConnections = connections.get(gameID);
        if (gameConnections == null) {
            addGame(gameID);
            gameConnections = connections.get(gameID);
        } else {
            checkTaken(gameConnections, color);
        }
        addPlayer(gameConnections, color, session);
        connections.put(gameID, gameConnections);
    }

    private void checkTaken(GameConnections gameConnections, ChessGame.TeamColor color) {
        if (color == ChessGame.TeamColor.BLACK && gameConnections.getBlackPlayer() != null) {
            throw new DataAccessException.AlreadyTakenException("Black player already taken");
        } else if (color == ChessGame.TeamColor.WHITE && gameConnections.getWhitePlayer() != null) {
            throw new DataAccessException.AlreadyTakenException("White player already taken");
        }
    }

    private void addPlayer(GameConnections gameConnections, ChessGame.TeamColor color, Session player) {
        if (color == ChessGame.TeamColor.WHITE) {
            gameConnections.setWhitePlayer(player);
        } else if (color == ChessGame.TeamColor.BLACK) {
            gameConnections.setBlackPlayer(player);
        } else {
            gameConnections.addObserver(player);
        }
    }

    public void remove(Session session, int gameID, ChessGame.TeamColor color) {
        GameConnections gameConnections = connections.get(gameID);
        removePlayer(gameConnections, color, session);
        connections.put(gameID, gameConnections);
    }

    private void removePlayer(GameConnections gameConnections, ChessGame.TeamColor color, Session session) {
        if (color == ChessGame.TeamColor.WHITE) {
            gameConnections.setWhitePlayer(null);
        } else if (color == ChessGame.TeamColor.BLACK) {
            gameConnections.setBlackPlayer(null);
        } else {
            gameConnections.removeObserver(session);
        }
    }

    public void broadcast(Session excludeSession, ServerMessage serverMessage, int gameID) throws IOException {
        String msg = new Gson().toJson(serverMessage);
        GameConnections gameConnections = connections.get(gameID);

        sendWhiteMessage(gameConnections, excludeSession, msg);
        sendBlackMessage(gameConnections, excludeSession, msg);
        sendObserverMessages(gameConnections, excludeSession, msg);
    }

    public void broadcastLoadGame(LoadGameMessage loadGameMessage, int gameID) throws IOException {
        GameConnections gameConnections = connections.get(gameID);

        sendObserverMessages(gameConnections, null, new Gson().toJson(loadGameMessage));
        sendLoadGame(loadGameMessage, gameConnections, ChessGame.TeamColor.WHITE);
        sendLoadGame(loadGameMessage, gameConnections, ChessGame.TeamColor.BLACK);
    }

    public void updateSessions() {
        for (GameConnections gameConnections : connections.values()) {
            gameConnections.updateSessions();
        }
    }

    private void sendLoadGame(LoadGameMessage loadGameMessage, GameConnections gameConnections, ChessGame.TeamColor color) throws IOException {
        loadGameMessage = loadGameMessage.setColor(color);
        String msg = new Gson().toJson(loadGameMessage);
        if (color == ChessGame.TeamColor.BLACK) {
            sendBlackMessage(gameConnections, null, msg);
        } else {
            sendWhiteMessage(gameConnections, null, msg);
        }
    }

    private void sendWhiteMessage(GameConnections gameConnections, Session excludeSession, String msg) throws IOException {
        Session whitePlayer = gameConnections.getWhitePlayer();
        sendSessionMessage(whitePlayer, excludeSession, msg);
    }

    private void sendBlackMessage(GameConnections gameConnections, Session excludeSession, String msg) throws IOException {
        Session blackPlayer = gameConnections.getBlackPlayer();
        sendSessionMessage(blackPlayer, excludeSession, msg);
    }

    private void sendObserverMessages(GameConnections gameConnections, Session excludeSession, String msg) throws IOException {
        for (Session c : gameConnections.getObservers()) {
            sendSessionMessage(c, excludeSession, msg);
        }
    }

    private void sendSessionMessage(Session session, Session excludeSession, String msg) throws IOException {

        if (session != null && session.isOpen()) {
            if (!session.equals(excludeSession)) {
                session.getRemote().sendString(msg);
            }
        }
    }
}
