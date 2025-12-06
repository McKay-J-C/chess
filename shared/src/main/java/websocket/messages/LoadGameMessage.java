package websocket.messages;

import chess.ChessGame;

public class LoadGameMessage extends ServerMessage {

    private final ChessGame game;
    private final ChessGame.TeamColor color;

    public LoadGameMessage(ServerMessageType type, ChessGame game, ChessGame.TeamColor color) {
        super(type);
        this.game = game;
        this.color = color;
        assert type.equals(ServerMessageType.LOAD_GAME);
    }

    public ChessGame getGame() {
        return game;
    }

    public ChessGame.TeamColor getColor() {
        return color;
    }
}
