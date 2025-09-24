package chess;

import java.util.Collection;

public class QueenMoves extends Moves {

    public QueenMoves(ChessBoard board, ChessPiece piece, ChessPosition pos) {
        super(board, piece, pos);
    }

    public Collection<ChessMove> getMoves() {
        checkMoveContinuous(new ChessPosition(startRow-1, startCol), Direction.DOWN);
        checkMoveContinuous(new ChessPosition(startRow, startCol-1), Direction.LEFT);
        checkMoveContinuous(new ChessPosition(startRow, startCol+1), Direction.RIGHT);
        checkMoveContinuous(new ChessPosition(startRow+1, startCol), Direction.UP);
        checkMoveContinuous(new ChessPosition(startRow-1, startCol-1), Direction.DOWN_LEFT);
        checkMoveContinuous(new ChessPosition(startRow-1, startCol+1), Direction.DOWN_RIGHT);
        checkMoveContinuous(new ChessPosition(startRow+1, startCol-1), Direction.UP_LEFT);
        checkMoveContinuous(new ChessPosition(startRow+1, startCol+1), Direction.UP_RIGHT);
        return moves;
    }
}