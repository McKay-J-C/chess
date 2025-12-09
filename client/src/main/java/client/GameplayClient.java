package client;

import chess.*;
import com.google.gson.Gson;
import model.GameData;
import server.ResponseException;
import server.ServerFacade;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;
import java.util.Scanner;

import static ui.EscapeSequences.*;
import static ui.EscapeSequences.BLACK_KING;
import static ui.EscapeSequences.BLACK_KNIGHT;
import static ui.EscapeSequences.BLACK_PAWN;

public class GameplayClient implements NotificationHandler {

    static String curBackColor = "Black";
    private final WebSocketFacade webSocket;
    private final ServerFacade server;
    private GameData gameData;

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

    public void run(String auth, GameData gameData, ChessGame.TeamColor color, boolean needConnect) {
        if (needConnect) {
            webSocket.connect(auth, gameData.gameID(), color);
        }
        this.gameData = gameData;

//        printGame(gameData.game().getBoard(), color);
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
            case "3" -> leave(auth, color, gameData.gameID());
            case "4" -> makeMove(scanner, auth, color, gameData.gameID());
            case "5" -> resign(scanner, auth, gameData.gameID(), color);
//            case "6" -> highlightLegalMoves(scanner, gameData.game());
            default -> System.out.print("\nInvalid input - Please Enter a number 1-6\n\n");
        }
    }

    private void resign(Scanner scanner, String auth, int gameID, ChessGame.TeamColor color) {
        System.out.println("Are you sure you want to resign?\nEnter 1 to resign, Enter anything else to cancel");
        String input = scanner.nextLine();
        if (input.equals("1")) {
            webSocket.resign(auth, gameID, color);
        }
    }

    private void makeMove(Scanner scanner, String auth, ChessGame.TeamColor color, int gameID) {
        ChessPosition startPos = getPos(scanner, "\nWhat piece would you like to move?");
        if (startPos == null) {
            return;
        }

        ChessPosition endPos = getPos(scanner, "\nWhere would you like to move the piece?");
        if (endPos == null) {
            return;
        }

        webSocket.makeMove(auth, gameID, color, new ChessMove(startPos, endPos));
    }

    private void leave(String auth, ChessGame.TeamColor color, int gameID) {
        exitGame();
        webSocket.leave(auth, gameID, color);
    }

    private ChessPosition getPos(Scanner scanner, String question) {
        System.out.println(question + "\nEnter row number: ");
        String row = scanner.nextLine();
        if (checkQuit(row)) {
            return null;
        }
        if (!isValid(row)) {
            System.out.println("\nPlease enter a number 1-8\n");
            return getPos(scanner, question);
        }
        int intRow = Integer.parseInt(row);

        System.out.println("Enter column number: ");
        String col = scanner.nextLine();
        if (checkQuit(col)) {
            return null;
        }
        if (!isValid(col)) {
            System.out.println("\nPlease enter a number 1-8\n");
            return getPos(scanner, question);
        }
        int intCol = Integer.parseInt(col);

        return new ChessPosition(intRow, intCol);
    }

    private boolean checkQuit(String num) {
        return num.equals("q") || num.equals("Q");
    }

    private boolean isValid(String num) {
        return num.equals("1") || num.equals("2") || num.equals("3") || num.equals("4") || num.equals("5") || num.equals("6") || num.equals("7") || num.equals("8");
    }

    static void printGame(ChessBoard board, ChessGame.TeamColor color) {
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

    static void printGameWhite(ChessBoard board) {
        System.out.print(SET_BG_COLOR_BLACK);
        System.out.println();
        for (int i=8; i > 0; i--) {
            System.out.print(SET_TEXT_COLOR_LIGHT_GREY);
            System.out.print("  " + i + "  ");
            for (int j=1; j < 9; j++) {
                printNextPiece(i, j, board);
                checkNewRow(j, 8);
            }
        }
        System.out.print(SET_TEXT_COLOR_LIGHT_GREY);
        System.out.print("     ");
        for (int k = 1; k < 9; k++) {
            System.out.print(" " + k + " ");
        }
        System.out.println();
    }

    static void printGameBlack(ChessBoard board) {
        System.out.print(SET_BG_COLOR_BLACK);
        System.out.println();
        for (int i=1; i < 9; i++) {
            System.out.print(SET_TEXT_COLOR_LIGHT_GREY);
            System.out.print("  " + i + "  ");
            for (int j=8; j > 0; j--) {
                printNextPiece(i, j, board);
                checkNewRow(j, 1);
            }
        }
        System.out.print(SET_TEXT_COLOR_LIGHT_GREY);
        System.out.print("     ");
        for (int k = 8; k > 0; k--) {
            System.out.print(" " + k + " ");
        }
        System.out.println();
    }

    static void checkNewRow(int j, int switchNum) {
        if (j == switchNum) {
            switchBackColor();
            System.out.print(SET_BG_COLOR_BLACK);
            System.out.print("\n");
        }
    }

    static void printNextPiece(int i, int j, ChessBoard board) {
        switchBackColor();
        ChessPosition pos = new ChessPosition(i, j);
        ChessPiece piece = board.getPiece(pos);
        if (piece == null) {
            System.out.print(EMPTY);
        } else {
            printPiece(piece);
        }
    }

    static String getSymbol(ChessPiece piece) {
        return switch (piece.getPieceType()) {
            case ROOK -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_ROOK : BLACK_ROOK;
            case KNIGHT -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_KNIGHT : BLACK_KNIGHT;
            case BISHOP -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_BISHOP : BLACK_BISHOP;
            case QUEEN -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_QUEEN : BLACK_QUEEN;
            case KING -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_KING : BLACK_KING;
            case PAWN -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_PAWN : BLACK_PAWN;
        };
    }

    static void printPiece(ChessPiece piece) {
        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            System.out.print(SET_TEXT_COLOR_WHITE);
        } else {
            System.out.print(SET_TEXT_COLOR_BLACK);
        }
        System.out.print(getSymbol(piece));
    }

    static void switchBackColor() {
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
    public void notify(ServerMessage serverMessage, String message) {
//        System.out.println("Attempting to handle message");
        boolean gameOver = false;
        switch (serverMessage.getServerMessageType()) {
            case ERROR -> handleErrorMessage(new Gson().fromJson(message, ErrorMessage.class));
            case LOAD_GAME -> handleLoadGameMessage(new Gson().fromJson(message, LoadGameMessage.class));
            case NOTIFICATION -> gameOver = handleNotificationMessage(new Gson().fromJson(message, NotificationMessage.class));
            default -> throw new RuntimeException("Unknown Server Message");
        }
        if (!gameOver) {
            System.out.println("\nEnter a number for what you would like to do! (Enter 1 for help)");
        }
    }

    private void handleErrorMessage(ErrorMessage errorMessage) {
        System.out.println(errorMessage.getErrorMessage());
    }

    private boolean handleNotificationMessage(NotificationMessage notificationMessage) {
        String message = notificationMessage.getMessage();
        System.out.println(message);
        if (message.contains("wins") || message.contains("tie")) {
            System.out.println("Enter 3 to exit");
            return true;
        }
        return false;
    }

    private void handleLoadGameMessage(LoadGameMessage loadGameMessage) {
        printGame(loadGameMessage.getGame().getBoard(), loadGameMessage.getColor());
    }
}
