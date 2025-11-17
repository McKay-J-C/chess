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

    public void run(String auth, GameData gameData, ChessGame.TeamColor color) {
        System.out.println("Running Gameplay");
        printGame(gameData.game().getBoard(), color);

        System.out.println("Enter \"q\" to exit");
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        if (input.equals("q") || input.equals("Q")) {
            exitGame();
        } else {
            printGame(gameData.game().getBoard(), color);
        }
    }
//
//    private void printGame(ChessBoard board) {
//        System.out.println();
//        for (int i=1; i < 9; i++) {
//            for (int j=8; j > 0; j--) {
//                switchBackColor();
//                ChessPosition pos = new ChessPosition(i, j);
//                ChessPiece piece = board.getPiece(pos);
//                if (piece == null) {
//                    System.out.print(EMPTY);
//                } else {
//                    printPiece(piece);
//                }
////                System.out.print(Integer.toString(i) + Integer.toString(j));
//                if (j == 1) {
//                    switchBackColor();
//                    System.out.print(SET_BG_COLOR_BLACK);
//                    System.out.print("\n");
//                }
//            }
//        }
//        System.out.println();
//    }

    private void printGame(ChessBoard board, ChessGame.TeamColor color) {
        System.out.println();
        if (color.equals(ChessGame.TeamColor.WHITE)) {
            printGameWhite(board);
        } else if (color.equals(ChessGame.TeamColor.BLACK)) {
            printGameBlack(board);
        } else {
            throw new RuntimeException("Invalid color");
        }
        System.out.print(RESET_TEXT_COLOR);
        System.out.print(RESET_BG_COLOR);
        System.out.println();
    }

    private void printGameWhite(ChessBoard board) {
        for (int i=8; i > 0; i--) {
            for (int j=1; j < 9; j++) {
                printNextPiece(i, j, board);
                checkNewRow(j, 8);
            }
        }
    }

    private void printGameBlack(ChessBoard board) {
        for (int i=1; i < 9; i++) {
            for (int j=8; j > 0; j--) {
                printNextPiece(i, j, board);
                checkNewRow(j, 1);
            }
        }
    }

    private void checkNewRow(int j, int switchNum) {
        if (j == switchNum) {
            switchBackColor();
            System.out.print(SET_BG_COLOR_BLACK);
            System.out.print("\n");
        }
    }

    private void printNextPiece(int i, int j, ChessBoard board) {
        switchBackColor();
        ChessPosition pos = new ChessPosition(i, j);
        ChessPiece piece = board.getPiece(pos);
        if (piece == null) {
            System.out.print(EMPTY);
        } else {
            printPiece(piece);
        }
    }

    private void printPiece(ChessPiece piece) {
        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            printWhitePiece(piece.getPieceType());
        } else {
            printBlackPiece(piece.getPieceType());
        }
    }

    private void printWhitePiece(ChessPiece.PieceType type) {
        System.out.print(SET_TEXT_COLOR_WHITE);
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
        System.out.print(SET_TEXT_COLOR_BLACK);
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
            System.out.print(SET_BG_COLOR_MEDIUM_GREY);
            curBackColor = "Black";
        } else if (curBackColor.equals("Black")) {
            System.out.print(SET_BG_COLOR_EXTRA_LIGHT_GREY);
            curBackColor = "White";
        } else {
            throw new RuntimeException("Bad Background Color");
        }
    }

    private void exitGame() {
        System.out.println("Hope you had fun!");
    }

}
