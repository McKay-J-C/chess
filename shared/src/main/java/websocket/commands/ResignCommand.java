package websocket.commands;

import chess.ChessGame;

public class ResignCommand extends UserGameCommand {

    private final String username;

    public ResignCommand(CommandType commandType, String authToken, Integer gameID, String username, ChessGame.TeamColor color) {
        super(commandType, authToken, gameID, color);
        this.username = username;
        assert commandType.equals(CommandType.RESIGN);
    }

    public String getUsername() {
        return username;
    }
}
