package server.websocket;

import java.util.HashSet;
import org.eclipse.jetty.websocket.api.Session;

public class GameConnections {
    private Session whitePlayer;
    private Session blackPlayer;
    private HashSet<Session> observers;

    public GameConnections() {
        whitePlayer = null;
        blackPlayer = null;
        observers = new HashSet<>();
    }

    public void addObserver(Session session) {
        observers.add(session);
    }

    public void removeObserver(Session session) {
        observers.remove(session);
    }

    public Session getWhitePlayer() {
        return whitePlayer;
    }

    public void setWhitePlayer(Session whitePlayer) {
        this.whitePlayer = whitePlayer;
    }

    public Session getBlackPlayer() {
        return blackPlayer;
    }

    public void setBlackPlayer(Session blackPlayer) {
        this.blackPlayer = blackPlayer;
    }

    public HashSet<Session> getObservers() {
        return observers;
    }

    public void setObservers(HashSet<Session> observers) {
        this.observers = observers;
    }
}
