package chess;

import java.util.Collection;
import java.util.List;
import static chess.MoveHelper.checkOpen;

public class KingMove {

    static Collection<ChessMove> getMoves(ChessBoard board, ChessPosition pos) {
        Collection<ChessMove> positions = new java.util.ArrayList<>(List.of());
        int row = pos.getRow();
        int col = pos.getColumn();

        ChessPosition bl = new ChessPosition(row-1,col-1);
        if (checkOpen(bl, board)) {
            ChessMove move = new ChessMove(pos, bl, null);
            positions.add(move);
        }

        ChessPosition bm = new ChessPosition(row-1, col);
        if (checkOpen(bm, board)) {
            ChessMove move = new ChessMove(pos, bm, null);
            positions.add(move);
        }

        ChessPosition br = new ChessPosition(row-1,col+1);
        if (checkOpen(br, board)) {
            ChessMove move = new ChessMove(pos, br, null);
            positions.add(move);
        }

        ChessPosition ml = new ChessPosition(row,col-1);
        if (checkOpen(ml, board)) {
            ChessMove move = new ChessMove(pos, ml, null);
            positions.add(move);
        }

        ChessPosition mr = new ChessPosition(row,col+1);
        if (checkOpen(mr, board)) {
            ChessMove move = new ChessMove(pos, mr, null);
            positions.add(move);
        }

        ChessPosition ul = new ChessPosition(row+1,col-1);
        if (checkOpen(ul, board)) {
            ChessMove move = new ChessMove(pos, ul, null);
            positions.add(move);
        }

        ChessPosition um = new ChessPosition(row+1,col);
        if (checkOpen(um, board)) {
            ChessMove move = new ChessMove(pos, um, null);
            positions.add(move);
        }

        ChessPosition ur = new ChessPosition(row+1,col+1);
        if (checkOpen(ur, board)) {
            ChessMove move = new ChessMove(pos, ur, null);
            positions.add(move);
        }

        return positions;
    }
}
