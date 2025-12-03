package websocket.commands;

import chess.ChessMove;

public class MakeMoveCommand extends UserGameCommand {

    private final ChessMove move;

    public MakeMoveCommand(CommandType commandType, String authToken, Integer gameID, ChessMove move) {
        super(commandType, authToken, gameID);
        assert commandType.equals(CommandType.MAKE_MOVE);
        this.move = move;
    }

    public ChessMove getMove() {
        return move;
    }

}
