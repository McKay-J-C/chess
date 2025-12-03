package websocket.commands;

import chess.ChessGame;

public class LeaveCommand extends UserGameCommand {

    private final String username;

    public LeaveCommand(CommandType commandType, String authToken, Integer gameID, String username, ChessGame.TeamColor color) {
        super(commandType, authToken, gameID, color);
        this.username = username;
        assert commandType.equals(CommandType.LEAVE);
    }

    public String getUsername() {
        return username;
    }
}
