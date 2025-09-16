package chess;

import java.util.Collection;
import java.util.List;

//import static chess.MoveHelper.checkOnBoard;
//import static chess.MoveHelper.*;

public class KingMove extends Move {

    static Collection<ChessMove> moves = new java.util.ArrayList<>(List.of());

    static Collection<ChessMove> getMoves(ChessBoard board, ChessPosition pos, ChessGame.TeamColor color) {
//        Collection<ChessMove> moves = new java.util.ArrayList<>(List.of());
        moves = new java.util.ArrayList<>(List.of());
        int row = pos.getRow();
        int col = pos.getColumn();

        checkMove(row-1, col-1, pos, board, color);
        checkMove(row-1, col, pos, board, color);
        checkMove(row-1, col+1, pos, board, color);
        checkMove(row, col-1, pos, board, color);
        checkMove(row, col+1, pos, board, color);
        checkMove(row+1, col-1, pos, board, color);
        checkMove(row+1, col, pos, board, color);
        checkMove(row+1, col+1, pos, board, color);

        return moves;
    }

    static void checkMove(int row, int col, ChessPosition pos, ChessBoard board, ChessGame.TeamColor color) {

        ChessPosition newPos = new ChessPosition(row,col);
        if (checkOnBoard(newPos)) {
            if (getColor(newPos, board) != color) {
                ChessMove move = new ChessMove(pos, newPos, null);
                moves.add(move);
            }
        }
    }


}
