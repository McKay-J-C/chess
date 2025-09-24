package chess;

import java.awt.*;
import java.util.Collection;

public class PawnMoves extends Moves {

    private final int moveDirection;
    private final int homeRow;
    private final int dubRow;
    private final int promRow;

    public PawnMoves(ChessBoard board, ChessPiece piece, ChessPosition pos) {
        super(board, piece, pos);
        if (color == ChessGame.TeamColor.WHITE) {
            moveDirection = 1;
            homeRow = 2;
            dubRow = 4;
            promRow = 8;
        } else {
            moveDirection = -1;
            homeRow = 7;
            dubRow = 5;
            promRow = 1;
        }
    }

    public Collection<ChessMove> getMoves() {
        ChessPosition onePos = new ChessPosition(startRow+moveDirection, startCol);
        ChessGame.TeamColor oneColor = getColor(onePos);
        if (oneColor == null) {
            addWithPromo(onePos);
            if (startRow == homeRow) {
                ChessPosition dubPos = new ChessPosition(dubRow, startCol);
                if (getColor(dubPos) == null) {
                    moves.add(new ChessMove(startPos, dubPos));
                }
            }
        }
        checkCapture(new ChessPosition(startRow+moveDirection, startCol-1));
        checkCapture(new ChessPosition(startRow+moveDirection, startCol+1));
        return moves;
    }

    private void checkCapture(ChessPosition newPos) {
        if (checkInBounds(newPos)) {
            ChessGame.TeamColor lColor = getColor(newPos);
            if (lColor != null && lColor != color) {
                addWithPromo(newPos);
            }
        }
    }

    private void addWithPromo(ChessPosition newPos) {
        if (newPos.getRow() == promRow) {
            moves.add(new ChessMove(startPos, newPos, ChessPiece.PieceType.QUEEN));
            moves.add(new ChessMove(startPos, newPos, ChessPiece.PieceType.ROOK));
            moves.add(new ChessMove(startPos, newPos, ChessPiece.PieceType.BISHOP));
            moves.add(new ChessMove(startPos, newPos, ChessPiece.PieceType.KNIGHT));
        } else {
            moves.add(new ChessMove(startPos, newPos));
        }
    }

}