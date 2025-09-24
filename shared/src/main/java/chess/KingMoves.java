package chess;

import java.util.Collection;

public class KingMoves extends Moves {

    public KingMoves(ChessBoard board, ChessPiece piece, ChessPosition pos) {
        super(board, piece, pos);
    }

    public Collection<ChessMove> getMoves() {
        checkMove(new ChessPosition(startRow-1, startCol-1));
        checkMove(new ChessPosition(startRow-1, startCol));
        checkMove(new ChessPosition(startRow-1, startCol+1));
        checkMove(new ChessPosition(startRow, startCol-1));
        checkMove(new ChessPosition(startRow, startCol+1));
        checkMove(new ChessPosition(startRow+1, startCol-1));
        checkMove(new ChessPosition(startRow+1, startCol));
        checkMove(new ChessPosition(startRow+1, startCol+1));
        return moves;
    }

    private void checkMove(ChessPosition newPos) {
        if (checkInBounds(newPos)) {
            if (getColor(newPos) != color) {
                moves.add(new ChessMove(startPos, newPos));
            }
        }
    }
}