package chess;

import java.util.Collection;
import java.util.List;

import static chess.MoveHelper.checkOnBoard;
import static chess.MoveHelper.*;

public class KingMove {

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

//        ChessPosition bl = new ChessPosition(row-1,col-1);
//        if (checkOnBoard(bl)) {
//            if (getColor(bl, board) != color) {
//                ChessMove move = new ChessMove(pos, bl, null);
//                moves.add(move);
//            }
//        }

//        ChessPosition bm = new ChessPosition(row-1, col);
//        if (checkOpen(bm, board)) {
//            ChessMove move = new ChessMove(pos, bm, null);
//            positions.add(move);
//        }
//
//        ChessPosition br = new ChessPosition(row-1,col+1);
//        if (checkOpen(br, board)) {
//            ChessMove move = new ChessMove(pos, br, null);
//            positions.add(move);
//        }
//
//        ChessPosition ml = new ChessPosition(row,col-1);
//        if (checkOpen(ml, board)) {
//            ChessMove move = new ChessMove(pos, ml, null);
//            positions.add(move);
//        }
//
//        ChessPosition mr = new ChessPosition(row,col+1);
//        if (checkOpen(mr, board)) {
//            ChessMove move = new ChessMove(pos, mr, null);
//            positions.add(move);
//        }
//
//        ChessPosition ul = new ChessPosition(row+1,col-1);
//        if (checkOpen(ul, board)) {
//            ChessMove move = new ChessMove(pos, ul, null);
//            positions.add(move);
//        }
//
//        ChessPosition um = new ChessPosition(row+1,col);
//        if (checkOpen(um, board)) {
//            ChessMove move = new ChessMove(pos, um, null);
//            positions.add(move);
//        }
//
//        ChessPosition ur = new ChessPosition(row+1,col+1);
//        if (checkOpen(ur, board)) {
//            ChessMove move = new ChessMove(pos, ur, null);
//            positions.add(move);
//        }
//
//        return positions;
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
