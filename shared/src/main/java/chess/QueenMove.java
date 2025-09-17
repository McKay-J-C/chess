package chess;

import java.util.Collection;
import java.util.List;

public class QueenMove extends Move {
    static Collection<ChessMove> moves = new java.util.ArrayList<>(List.of());

    static Collection<ChessMove> getMoves(ChessBoard board, ChessPosition pos, ChessGame.TeamColor color) {
        moves = new java.util.ArrayList<>(List.of());
        moves.addAll(BishopMove.getMoves(board, pos, color));
        moves.addAll(RookMove.getMoves(board, pos, color));
        return moves;
    }
}
