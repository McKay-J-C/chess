package websocket.commands;

import chess.ChessGame;

public class ConnectCommand extends UserGameCommand {

    private final String username;

    public ConnectCommand(CommandType commandType, String authToken, Integer gameID, String username, ChessGame.TeamColor color) {
        super(commandType, authToken, gameID, color);
        this.username = username;
        assert commandType.equals(CommandType.CONNECT);
    }

    public String getUsername() {
        return username;
    }
}
