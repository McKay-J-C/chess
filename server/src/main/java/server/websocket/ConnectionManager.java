package server.websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import org.eclipse.jetty.websocket.api.Session;
import websocket.commands.*;
import websocket.messages.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    public final HashMap<Integer, GameConnections> connections = new HashMap<>();

    public void add(Session session, int gameID, ChessGame.TeamColor color) {
        GameConnections gameConnections = connections.get(gameID);
        checkTaken(gameConnections, color);
        addPlayer(gameConnections, color, session);
        connections.put(gameID, gameConnections);
    }

    private void checkTaken(GameConnections gameConnections, ChessGame.TeamColor color) {
        if (color == ChessGame.TeamColor.BLACK && !gameConnections.getBlackPlayer().isOpen()) {
            throw new DataAccessException.AlreadyTakenException("Black player already taken");
        } else if (color == ChessGame.TeamColor.WHITE && !gameConnections.getWhitePlayer().isOpen()) {
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

        Session whitePlayer = gameConnections.getWhitePlayer();
        sendSessionMessage(whitePlayer, excludeSession, msg);

        Session blackPlayer = gameConnections.getWhitePlayer();
        sendSessionMessage(blackPlayer, excludeSession, msg);

        for (Session c : gameConnections.getObservers()) {
            sendSessionMessage(c, excludeSession, msg);
        }
    }

    private void sendSessionMessage(Session session, Session excludeSession, String msg) throws IOException {
        if (session.isOpen()) {
            if (!session.equals(excludeSession)) {
                session.getRemote().sendString(msg);
            }
        }
    }
}
