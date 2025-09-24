package chess;

import java.util.Collection;

public class KnightMoves extends Moves {

    public KnightMoves(ChessBoard board, ChessPiece piece, ChessPosition pos) {
        super(board, piece, pos);
    }

    public Collection<ChessMove> getMoves() {
        checkMove(new ChessPosition(startRow-2, startCol-1));
        checkMove(new ChessPosition(startRow-2, startCol+1));
        checkMove(new ChessPosition(startRow-1, startCol-2));
        checkMove(new ChessPosition(startRow-1, startCol+2));
        checkMove(new ChessPosition(startRow+1, startCol-2));
        checkMove(new ChessPosition(startRow+1, startCol+2));
        checkMove(new ChessPosition(startRow+2, startCol-1));
        checkMove(new ChessPosition(startRow+2, startCol+1));
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
