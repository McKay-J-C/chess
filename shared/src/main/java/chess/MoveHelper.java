package chess;

public class MoveHelper {

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
}
