package chess;

import java.util.ArrayList;
import java.util.Collection;

public class Moves {

    Collection<ChessMove> moves;
    ChessBoard board;
    ChessPiece piece;
    ChessPosition startPos;
    ChessGame.TeamColor color;
    int startRow;
    int startCol;

    public Moves(ChessBoard board, ChessPiece piece, ChessPosition pos) {
        this.moves = new ArrayList<>();
        this.board = board;
        this.piece = piece;
        this.startPos = pos;
        this.color = piece.getTeamColor();
        this.startRow = pos.getRow();
        this.startCol = pos.getColumn();
    }

    protected boolean checkInBounds(ChessPosition newPos) {
        int newRow = newPos.getRow();
        int newCol = newPos.getColumn();
        return (newRow>0 && newRow<9 && newCol>0 && newCol<9);
    }

    protected ChessGame.TeamColor getColor(ChessPosition newPos) {
        ChessPiece newPiece = board.getPiece(newPos);
        if (newPiece == null) {
            return null;
        } else {
            return newPiece.getTeamColor();
        }
    }

    protected void checkMoveContinuous(ChessPosition newPos, Direction direction) {
        if (checkInBounds(newPos)) {
            ChessGame.TeamColor newColor = getColor(newPos);
            if (newColor != color) {
                moves.add(new ChessMove(startPos, newPos));

                if (newColor == null) {
                    int newRow = newPos.getRow();
                    int newCol = newPos.getColumn();
                    if (direction == Direction.DOWN) {
                        checkMoveContinuous(new ChessPosition(newRow-1, newCol), Direction.DOWN);
                    }
                    else if (direction == Direction.LEFT) {
                        checkMoveContinuous(new ChessPosition(newRow, newCol-1), Direction.LEFT);
                    }
                    else if (direction == Direction.RIGHT) {
                        checkMoveContinuous(new ChessPosition(newRow, newCol+1), Direction.RIGHT);
                    }
                    else if (direction == Direction.UP) {
                        checkMoveContinuous(new ChessPosition(newRow+1, newCol), Direction.UP);
                    }
                    else if (direction == Direction.DOWN_LEFT) {
                        checkMoveContinuous(new ChessPosition(newRow-1, newCol-1), Direction.DOWN_LEFT);
                    }
                    else if (direction == Direction.DOWN_RIGHT) {
                        checkMoveContinuous(new ChessPosition(newRow-1, newCol+1), Direction.DOWN_RIGHT);
                    }
                    else if (direction == Direction.UP_LEFT) {
                        checkMoveContinuous(new ChessPosition(newRow+1, newCol-1), Direction.UP_LEFT);
                    }
                    else if (direction == Direction.UP_RIGHT) {
                        checkMoveContinuous(new ChessPosition(newRow+1, newCol+1), Direction.UP_RIGHT);
                    }
                }
            }
        }
    }



    protected enum Direction {
        DOWN,
        LEFT,
        RIGHT,
        UP,
        DOWN_LEFT,
        DOWN_RIGHT,
        UP_LEFT,
        UP_RIGHT
    }

}
