package websocket.commands;

import chess.ChessGame;
import chess.ChessMove;

public class MakeMoveCommand extends UserGameCommand {

    private final ChessMove move;
    private final String username;

    public MakeMoveCommand(CommandType commandType, String authToken, Integer gameID, ChessMove move, String username, ChessGame.TeamColor color) {
        super(commandType, authToken, gameID, color);
        this.username = username;
        assert commandType.equals(CommandType.MAKE_MOVE);
        this.move = move;
    }

    public ChessMove getMove() {
        return move;
    }

    public String getUsername() {
        return username;
    }
}
