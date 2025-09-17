package chess;

import java.util.Collection;
import java.util.List;

public class PawnMove extends Move {
    static Collection<ChessMove> moves = new java.util.ArrayList<>(List.of());
    static final ChessPiece.PieceType[] promoPieces = {ChessPiece.PieceType.QUEEN, ChessPiece.PieceType.BISHOP, ChessPiece.PieceType.ROOK, ChessPiece.PieceType.KNIGHT};

    static Collection<ChessMove> getMoves(ChessBoard board, ChessPosition pos, ChessGame.TeamColor color) {

        moves = new java.util.ArrayList<>(List.of());
        int row = pos.getRow();

        if (color == ChessGame.TeamColor.WHITE) {
            checkMoves(board, pos, row+1, 2, 4, 8, color);
        } else {
            checkMoves(board, pos, row-1, 7, 5, 1, color);
        }

        return moves;
    }

    static void checkMoves(ChessBoard board, ChessPosition pos, int newRow, int startRow, int doubleRow, int promRow, ChessGame.TeamColor color) {
        int row = pos.getRow();
        int col = pos.getColumn();
        ChessPosition newPos = new ChessPosition(newRow, col);
        if (checkOnBoard(newPos)) {
            if (getColor(newPos, board) == null) {
                addWithPromo(pos, newPos, promRow);
                if (row == startRow) {
                    newPos = new ChessPosition(doubleRow, col);
                    if (getColor(newPos, board) == null) {
                        moves.add(new ChessMove(pos, newPos));
                    }
                }
            }
        }

        checkCaptureMove(board, pos, new ChessPosition(newRow, col-1), color, promRow);
        checkCaptureMove(board, pos, new ChessPosition(newRow, col+1), color, promRow);
    }

    static void checkCaptureMove(ChessBoard board, ChessPosition pos, ChessPosition newPos, ChessGame.TeamColor color, int promRow) {
        if (checkOnBoard(newPos)) {
            ChessGame.TeamColor newColor = getColor(newPos, board);
            if (newColor != color && newColor != null) {
                addWithPromo(pos, newPos, promRow);
            }
        }
    }

    static void addWithPromo(ChessPosition pos, ChessPosition newPos, int promRow) {

        if (newPos.getRow() == promRow) {
            for (ChessPiece.PieceType type : promoPieces) {
                moves.add(new ChessMove(pos, newPos, type));
            }
        } else {
            moves.add(new ChessMove(pos, newPos));
        }
    }

}
