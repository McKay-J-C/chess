package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private TeamColor teamTurn;
    private ChessBoard board;
    private ChessPosition blackKingPos;
    private ChessPosition whiteKingPos;

    public ChessGame() {
        teamTurn = TeamColor.WHITE;
        board = new ChessBoard();
        board.resetBoard();
        whiteKingPos = new ChessPosition(1, 5);
        blackKingPos = new ChessPosition(8, 5);
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ArrayList<ChessMove> checkedMoves = new ArrayList<>();
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            return null;
        }
        TeamColor color = piece.getTeamColor();
//        If it is not the team's turn, return empty list
        if (color != teamTurn) {
            return checkedMoves;
        }
        Collection<ChessMove> moves = piece.pieceMoves(board, startPosition);
        ChessBoard testBoard = board.clone();
        for (ChessMove move : moves) {
            testBoard.removePiece(move.getStartPosition());
            testBoard.addPiece(move.getEndPosition(), piece);
            if (!checkHelper(color, testBoard)) {
                checkedMoves.add(move);
            }
            testBoard = board.clone();
        }
        return checkedMoves;
    }

//    public void removePiece(ChessPosition pos) {
//        board.removePiece(pos);
//    }
//
//    public void addPiece(ChessPosition pos, ChessPiece piece) {
//        board.addPiece(pos, piece);
//    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        return checkHelper(teamColor, board);
    }

    private boolean checkHelper(TeamColor teamColor, ChessBoard curBoard) {
        ChessPosition kingPos = getKingPos(teamColor);
        for (int i=1; i<=8; i++) {
            for (int j=1; j<=8; j++) {
                ChessPosition curPos = new ChessPosition(i,j);
                ChessPiece piece = curBoard.getPiece(curPos);
                if (piece != null && piece.getTeamColor() != teamColor) {
                    Collection<ChessMove> pieceMoves = piece.pieceMoves(curBoard, curPos);
                    for (ChessMove move : pieceMoves) {
                        if (move.getEndPosition().equals(kingPos)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public ChessPosition getKingPos(TeamColor teamColor) {
        if (teamColor == TeamColor.BLACK) {
            return blackKingPos;
        } else {
            return whiteKingPos;
        }
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }
//        for (int i=1; i<=8; i++) {
//            for (int j=1; j<=8; j++) {
//                ChessPosition curPos = new ChessPosition(i,j);
//                ChessPiece piece = board.getPiece(curPos);
//                if (piece != null && piece.getTeamColor() == teamColor) {
//                    Collection<ChessMove> pieceMoves = piece.pieceMoves(board, curPos);
//                }
//            }
//        }
        return true;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }
        ArrayList<ChessMove> moves;
        return true;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
        for (int i=1; i<9; i++) {
            for (int j=1; j<9; j++) {
                ChessPosition curPos = new ChessPosition(i, j);
                ChessPiece curPiece = board.getPiece(curPos);
                if (curPiece != null) {
                    if (curPiece.getPieceType() == ChessPiece.PieceType.KING) {
                        if (curPiece.getTeamColor() == TeamColor.WHITE) {
                            whiteKingPos = curPos;
                        } else {
                            blackKingPos = curPos;
                        }
                    }
                }
            }
        }
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return teamTurn == chessGame.teamTurn && Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamTurn, board);
    }
}
