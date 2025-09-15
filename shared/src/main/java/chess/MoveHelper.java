package chess;

public class MoveHelper {

    public static boolean checkOpen(ChessPosition pos, ChessBoard board) {
        int row = pos.getRow();
        int col = pos.getColumn();
        if (row < 1 || row > 8 || col < 1 || col > 8) {
            return false;
        }
        return board.squares[row-1][col-1] == null;
    }
}
