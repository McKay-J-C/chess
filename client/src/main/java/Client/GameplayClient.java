package Client;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import model.GameData;
import server.ServerFacade;

import java.util.Scanner;

import static ui.EscapeSequences.*;
import static ui.EscapeSequences.BLACK_KING;
import static ui.EscapeSequences.BLACK_PAWN;

public class GameplayClient {

    private String curBackColor = "Black";
    private final ServerFacade server;

    public GameplayClient(ServerFacade server) {
        this.server = server;
    }

    public void run(String auth, GameData gameData) {
        System.out.println("Running Gameplay");
        printGame(gameData.game().getBoard());

        System.out.println("Enter \"q\" to exit");
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        if (input.equals("q") || input.equals("Q")) {
            exitGame();
        } else {
            printGame(gameData.game().getBoard());
        }
    }

    private void printGame(ChessBoard board) {
        System.out.println();
        for (int i=1; i < 9; i++) {
            for (int j=1; j < 9; j++) {
                switchBackColor();
                ChessPosition pos = new ChessPosition(i, j);
                ChessPiece piece = board.getPiece(pos);
                if (piece == null) {
                    System.out.print(EMPTY);
                } else {
                    printPiece(piece);
                }
                if (j == 8) {
                    switchBackColor();
                    System.out.print(SET_BG_COLOR_BLACK);
                    System.out.print("\n");
                }
            }
        }
        System.out.println();
    }

    private void printPiece(ChessPiece piece) {
        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            printWhitePiece(piece.getPieceType());
        } else {
            printBlackPiece(piece.getPieceType());
        }
    }

    private void printWhitePiece(ChessPiece.PieceType type) {
        if (type == ChessPiece.PieceType.ROOK) {
            System.out.print(WHITE_ROOK);
        } else if (type == ChessPiece.PieceType.KNIGHT){
            System.out.print(WHITE_KNIGHT);
        } else if (type == ChessPiece.PieceType.BISHOP){
            System.out.print(WHITE_BISHOP);
        } else if (type == ChessPiece.PieceType.QUEEN){
            System.out.print(WHITE_QUEEN);
        } else if (type == ChessPiece.PieceType.KING){
            System.out.print(WHITE_KING);
        } else {
            System.out.print(WHITE_PAWN);
        }
    }

    private void printBlackPiece(ChessPiece.PieceType type) {
        if (type == ChessPiece.PieceType.ROOK) {
            System.out.print(BLACK_ROOK);
        } else if (type == ChessPiece.PieceType.KNIGHT){
            System.out.print(BLACK_KNIGHT);
        } else if (type == ChessPiece.PieceType.BISHOP){
            System.out.print(BLACK_BISHOP);
        } else if (type == ChessPiece.PieceType.QUEEN){
            System.out.print(BLACK_QUEEN);
        } else if (type == ChessPiece.PieceType.KING){
            System.out.print(BLACK_KING);
        } else {
            System.out.print(BLACK_PAWN);
        }
    }

    private void switchBackColor() {
        if (curBackColor.equals("White")) {
            System.out.print(SET_BG_COLOR_DARK_GREY);
            curBackColor = "Black";
        } else if (curBackColor.equals("Black")) {
            System.out.print(SET_BG_COLOR_LIGHT_GREY);
            curBackColor = "White";
        } else {
            throw new RuntimeException("Bad Background Color");
        }
    }

    private void exitGame() {
        System.out.println("Hope you had fun!");
    }

}
