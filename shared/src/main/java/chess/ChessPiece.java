package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType pieceType;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.pieceType = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return pieceType;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && pieceType == that.pieceType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, pieceType);
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        if (pieceType == PieceType.KING) {
            return new KingMoves(board, this, myPosition).getMoves();
        }
        else if (pieceType == PieceType.KNIGHT) {
            return new KnightMoves(board, this, myPosition).getMoves();
        }
        else if (pieceType == PieceType.ROOK) {
            return new RookMoves(board, this, myPosition).getMoves();
        }
        else if (pieceType == PieceType.BISHOP) {
            return new BishopMoves(board, this, myPosition).getMoves();
        }
        else if (pieceType == PieceType.QUEEN) {
            return new QueenMoves(board, this, myPosition).getMoves();
        }
        else if (pieceType == PieceType.PAWN) {
            return new PawnMoves(board, this, myPosition).getMoves();
        }
        else {
            return new ArrayList<>();
        }
    }
}
