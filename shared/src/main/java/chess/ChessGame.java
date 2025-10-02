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
        //Keeping track of King position makes check easier
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
    public ArrayList<ChessMove> validMoves(ChessPosition startPosition) {
        ArrayList<ChessMove> checkedMoves = new ArrayList<>();
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            return null;
        }

        TeamColor color = piece.getTeamColor();
        //Gets all the pieceMoves for the piece at this position
        Collection<ChessMove> moves = piece.pieceMoves(board, startPosition);
        //Make temporary board to see if moves leads to check
        ChessBoard testBoard = board.clone();

        for (ChessMove move : moves) {
            //Changes test board and checks if move led to check
            makeMoveHelper(move, testBoard);
            if (!checkHelper(color, testBoard)) {
                checkedMoves.add(move);
            }
            //Reset board back to original
            updateKingPos(piece, startPosition); //Because makeMoveHelper has side effect
            testBoard = board.clone();
        }
        return checkedMoves;
    }




    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition startPos = move.getStartPosition();
        ChessPiece piece = board.getPiece(startPos);
        if (piece == null) {
            throw new InvalidMoveException("No piece at this location");
        }
        if (piece.getTeamColor() != teamTurn) {
           throw new InvalidMoveException("Not correct team turn");
        }
        //Check if move in list of valid moves
        Collection<ChessMove> valMoves = validMoves(startPos);
        if (!valMoves.contains(move)) {
            throw new InvalidMoveException("Not valid move for selected piece");
        }
        //Change the board based on the move
        makeMoveHelper(move, board);
        //Change team turn to other team
        if (teamTurn == TeamColor.WHITE) {
            teamTurn = TeamColor.BLACK;
        } else {
            teamTurn = TeamColor.WHITE;
        }
    }

    private void makeMoveHelper(ChessMove move, ChessBoard board) {
        ChessPosition startPos = move.getStartPosition();
        ChessPosition endPos = move.getEndPosition();
        ChessPiece piece = board.getPiece(startPos);

        //Update board and king position
        board.removePiece(startPos);
        board.addPiece(endPos, piece);
        //This checks if piece is a king in the function
        updateKingPos(piece, endPos);

        //Check if pawn is promoting
        ChessPiece.PieceType promPiece = move.getPromotionPiece();
        if (move.getPromotionPiece() != null) {
            board.addPiece(endPos, new ChessPiece(piece.getTeamColor(), promPiece));
        }
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
        //Loops through every square, and then checks the possible moves for each piece of the opposite color
        for (int i=1; i<=8; i++) {
            for (int j=1; j<=8; j++) {
                ChessPosition curPos = new ChessPosition(i,j);
                ChessPiece piece = curBoard.getPiece(curPos);
                //checking if piece is the other team
                if (piece != null && piece.getTeamColor() != teamColor) {
                    //Get all the moves that piece can make
                    Collection<ChessMove> pieceMoves = piece.pieceMoves(curBoard, curPos);
                    for (ChessMove move : pieceMoves) {
                        //If a move attacks the king, the king is in check
                        if (move.getEndPosition().equals(kingPos)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private ChessPosition getKingPos(TeamColor teamColor) {
        if (teamColor == TeamColor.BLACK) {
            return blackKingPos;
        } else {
            return whiteKingPos;
        }
    }

    private void updateKingPos(ChessPiece piece, ChessPosition pos) {
        if (piece.getPieceType() != ChessPiece.PieceType.KING) {
            return;
        }
        TeamColor color = piece.getTeamColor();
        if (color == TeamColor.BLACK) {
            blackKingPos = pos;
        } else {
            whiteKingPos = pos;
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
        return noValidMoves(teamColor);
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
        return noValidMoves(teamColor);
    }

    private boolean noValidMoves(TeamColor teamColor) {
        //Checks king first for efficiency
        if (kingMoveOutOfCheck(teamColor)) {
            return false;
        }
        //Loops through every piece on the board, then checks if moving that piece can remove check
        for (int i=1; i<=8; i++) {
            for (int j=1; j<=8; j++) {
                ChessPosition curPos = new ChessPosition(i,j);
                ChessPiece piece = board.getPiece(curPos);
                //Makes sure piece is the same color and not the king (since we already checked it)
                if (piece != null && piece.getTeamColor() == teamColor && piece.getPieceType() != ChessPiece.PieceType.KING) {
                    //Valid moves will only add moves that make it so that the king is no longer in check
                    ArrayList<ChessMove> moves = validMoves(curPos);
                    //If there are any valid moves from this piece, the function can return
                    if (!moves.isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean kingMoveOutOfCheck(TeamColor teamColor) {
        //Does the same thing as noValidMoves, but just for the king since it is very likely the king can move (Improves efficiency)
        ArrayList<ChessMove> kingMoves;
        if (teamColor == TeamColor.WHITE) {
            kingMoves = validMoves(whiteKingPos);
        } else {
            kingMoves = validMoves(blackKingPos);
        }
        return !kingMoves.isEmpty();
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
        //This loops through the board and finds where the kings are starting at
        for (int i=1; i<9; i++) {
            for (int j=1; j<9; j++) {
                ChessPosition curPos = new ChessPosition(i, j);
                ChessPiece curPiece = board.getPiece(curPos);
                if (curPiece != null && curPiece.getPieceType() == ChessPiece.PieceType.KING) {
                    if (curPiece.getTeamColor() == TeamColor.WHITE) {
                        whiteKingPos = curPos;
                    } else {
                        blackKingPos = curPos;
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
