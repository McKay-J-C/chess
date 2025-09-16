package chess;

import java.util.Collection;
import java.util.List;

public class RookMove extends Move {

    static Collection<ChessMove> moves = new java.util.ArrayList<>(List.of());


    static Collection<ChessMove> getMoves(ChessBoard board, ChessPosition pos, ChessGame.TeamColor color) {

        moves = new java.util.ArrayList<>(List.of());
        int row = pos.getRow();
        int col = pos.getColumn();

        moves.addAll(checkContinuousMove(row-1, col, pos, board, color, Direction.DOWN, new java.util.ArrayList<>(List.of())));
        moves.addAll(checkContinuousMove(row, col-1, pos, board, color, Direction.LEFT, new java.util.ArrayList<>(List.of())));
        moves.addAll(checkContinuousMove(row, col+1, pos, board, color, Direction.RIGHT, new java.util.ArrayList<>(List.of())));
        moves.addAll(checkContinuousMove(row+1, col, pos, board, color, Direction.UP, new java.util.ArrayList<>(List.of())));

        return moves;
    }
}
