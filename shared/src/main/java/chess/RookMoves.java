package chess;

import java.util.Collection;

public class RookMoves extends Moves {

    public RookMoves(ChessBoard board, ChessPiece piece, ChessPosition pos) {
        super(board, piece, pos);
    }

    public Collection<ChessMove> getMoves() {
        checkMoveContinuous(new ChessPosition(startRow-1, startCol), Direction.DOWN);
        checkMoveContinuous(new ChessPosition(startRow, startCol-1), Direction.LEFT);
        checkMoveContinuous(new ChessPosition(startRow, startCol+1), Direction.RIGHT);
        checkMoveContinuous(new ChessPosition(startRow+1, startCol), Direction.UP);
        return moves;
    }

}
