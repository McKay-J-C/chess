package client;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import model.GameData;
import server.ResponseException;
import server.ServerFacade;

import javax.management.Notification;
import java.util.Scanner;

import static ui.EscapeSequences.*;
import static ui.EscapeSequences.BLACK_KING;
import static ui.EscapeSequences.BLACK_KNIGHT;
import static ui.EscapeSequences.BLACK_PAWN;

public class GameplayClient implements NotificationHandler {

    private String curBackColor = "Black";
    private String authToken;
    private final WebSocketFacade webSocket;
    private final ServerFacade server;

    public GameplayClient(ServerFacade server, String serverUrl) throws ResponseException {
        this.server = server;
        this.webSocket = new WebSocketFacade(serverUrl, this);
    }

    private final String help =
            """
            Enter a number for what you would like to do!
            
            1: Help
            2: Redraw Chess Board
            3: Leave Game
            4: Make Move
            5: Resign
            6: Highlight Legal Moves
            
            """;

    public void run(String auth, GameData gameData, ChessGame.TeamColor color) {
        printGame(gameData.game().getBoard(), color);
        Scanner scanner = new Scanner(System.in);
        String line = "";

        while (!line.equals("3")) {
            System.out.print(help);
            line = scanner.nextLine();
            eval(line, scanner, auth, gameData, color);
        }
    }

    private void eval(String input, Scanner scanner, String auth, GameData gameData, ChessGame.TeamColor color) {
        switch (input) {
            case "1" -> System.out.print(help);
            case "2" -> printGame(gameData.game().getBoard(), color);
            case "3" -> leave(auth, color, gameData);
//            case "4" -> makeMove(scanner, auth, gameData, color);
//            case "5" -> resign(auth, gameData, color);
//            case "6" -> highlightLegalMoves(scanner, gameData.game());
            default -> System.out.print("\nInvalid input - Please Enter a number 1-6\n\n");
        }
    }

    private void leave(String auth, ChessGame.TeamColor color, GameData gameData) {

    }

    private void printGame(ChessBoard board, ChessGame.TeamColor color) {
        System.out.println();
        if (color == null || color.equals(ChessGame.TeamColor.WHITE)) {
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

    public String getSymbol(ChessPiece piece) {
        return switch (piece.getPieceType()) {
            case ROOK -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_ROOK : BLACK_ROOK;
            case KNIGHT -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_KNIGHT : BLACK_KNIGHT;
            case BISHOP -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_BISHOP : BLACK_BISHOP;
            case QUEEN -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_QUEEN : BLACK_QUEEN;
            case KING -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_KING : BLACK_KING;
            case PAWN -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_PAWN : BLACK_PAWN;
        };
    }

    private void printPiece(ChessPiece piece) {
        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            System.out.print(SET_TEXT_COLOR_WHITE);
        } else {
            System.out.print(SET_TEXT_COLOR_BLACK);
        }
        System.out.print(getSymbol(piece));
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

    @Override
    public void notify(Notification notification) {
        System.out.println(notification.getMessage() + "\n");
    }
}
