package websocket.commands;

import chess.ChessGame;
import chess.ChessMove;

public class MakeMoveCommand extends UserGameCommand {

    private final ChessMove move;
    private String username;

    public MakeMoveCommand(CommandType commandType, String authToken, Integer gameID, ChessMove move, ChessGame.TeamColor color) {
        super(commandType, authToken, gameID, color);
        assert commandType.equals(CommandType.MAKE_MOVE);
        this.move = move;
    }

    public ChessMove getMove() {
        return move;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
