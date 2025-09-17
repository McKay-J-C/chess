package chess;

import java.util.Collection;

public class Move {

    public static boolean checkOnBoard(ChessPosition pos) {
        int row = pos.getRow();
        int col = pos.getColumn();
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }

    public static ChessGame.TeamColor getColor(ChessPosition pos, ChessBoard board) {
        if (board.squares[pos.getRow()-1][pos.getColumn()-1] == null) {
            return null;
        }
        return board.squares[pos.getRow()-1][pos.getColumn()-1].getTeamColor();
    }

    static Collection<ChessMove> checkContinuousMove(int row, int col, ChessPosition pos, ChessBoard board, ChessGame.TeamColor color, BishopMove.Direction direction, Collection<ChessMove> moves) {
        ChessPosition newPos = new ChessPosition(row,col);

        if (checkOnBoard(newPos)) {
            ChessGame.TeamColor newColor = getColor(newPos, board);
            if (newColor != color) {
                ChessMove move = new ChessMove(pos, newPos);
                moves.add(move);
                if (newColor == null) {
                    if (direction == Direction.DOWN_LEFT) {
                        checkContinuousMove(row-1, col-1, pos, board, color, Direction.DOWN_LEFT, moves);
                    }
                    if (direction == Direction.DOWN_RIGHT) {
                        checkContinuousMove(row-1, col+1, pos, board, color, Direction.DOWN_RIGHT, moves);
                    }
                    if (direction == Direction.UP_LEFT) {
                        checkContinuousMove(row+1, col-1, pos, board, color, Direction.UP_LEFT, moves);
                    }
                    if (direction == Direction.UP_RIGHT) {
                        checkContinuousMove(row+1, col+1, pos, board, color, Direction.UP_RIGHT, moves);
                    }
                    if (direction == Direction.DOWN) {
                        checkContinuousMove(row-1, col, pos, board, color, Direction.DOWN, moves);
                    }
                    if (direction == Direction.LEFT) {
                        checkContinuousMove(row, col-1, pos, board, color, Direction.LEFT, moves);
                    }
                    if (direction == Direction.RIGHT) {
                        checkContinuousMove(row, col+1, pos, board, color, Direction.RIGHT, moves);
                    }
                    if (direction == Direction.UP) {
                        checkContinuousMove(row+1, col, pos, board, color, Direction.UP, moves);
                    }
                }
            }
        }
        return moves;
    }

    protected enum Direction {
        DOWN_LEFT,
        DOWN_RIGHT,
        UP_LEFT,
        UP_RIGHT,
        DOWN,
        LEFT,
        RIGHT,
        UP
    }
}
