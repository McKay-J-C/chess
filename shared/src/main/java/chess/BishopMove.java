package chess;

import java.util.Collection;
import java.util.List;

//import static chess.MoveHelper.checkOnBoard;
//import static chess.MoveHelper.*;

import java.util.Collection;

public class BishopMove extends Move {
    static Collection<ChessMove> moves = new java.util.ArrayList<>(List.of());


    static Collection<ChessMove> getMoves(ChessBoard board, ChessPosition pos, ChessGame.TeamColor color) {

        moves = new java.util.ArrayList<>(List.of());
        int row = pos.getRow();
        int col = pos.getColumn();

        moves.addAll(checkContinuousMove(row-1, col-1, pos, board, color, Direction.DOWN_LEFT, new java.util.ArrayList<>(List.of())));
        moves.addAll(checkContinuousMove(row-1, col+1, pos, board, color, Direction.DOWN_RIGHT, new java.util.ArrayList<>(List.of())));
        moves.addAll(checkContinuousMove(row+1, col-1, pos, board, color, Direction.UP_LEFT, new java.util.ArrayList<>(List.of())));
        moves.addAll(checkContinuousMove(row+1, col+1, pos, board, color, Direction.UP_RIGHT, new java.util.ArrayList<>(List.of())));

        return moves;
    }



}
