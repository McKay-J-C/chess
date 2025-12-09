package client;

import chess.*;
import com.google.gson.Gson;
import model.GameData;
import server.ResponseException;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.util.ArrayList;
import java.util.Scanner;

import static ui.EscapeSequences.*;
import static ui.EscapeSequences.BLACK_KING;
import static ui.EscapeSequences.BLACK_KNIGHT;
import static ui.EscapeSequences.BLACK_PAWN;

public class GameplayClient implements NotificationHandler {

    static String curBackColor = "Black";
    private final WebSocketFacade webSocket;
    private GameData gameData;

    public GameplayClient(String serverUrl) throws ResponseException {
        this.webSocket = new WebSocketFacade(serverUrl, this);
    }

    private final String help =
            """
            
            Options:
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

        Scanner scanner = new Scanner(System.in);
        String line = "";
        System.out.println(help);

        while (!line.equals("3")) {
            line = scanner.nextLine();
            eval(line, scanner, auth, this.gameData, color);
        }
    }

    private void eval(String input, Scanner scanner, String auth, GameData gameData, ChessGame.TeamColor color) {
        switch (input) {
            case "1" -> System.out.print(help);
            case "2" -> printGame(gameData.game().getBoard(), color, new ArrayList<>());
            case "3" -> leave(auth, color, gameData.gameID());
            case "4" -> makeMove(scanner, auth, color, gameData.gameID());
            case "5" -> resign(scanner, auth, gameData.gameID(), color);
            case "6" -> highlightLegalMoves(scanner, gameData.game(), color);
            default -> System.out.println("\nInvalid input - Please Enter a number 1-6\n");
        }
    }

    private void highlightLegalMoves(Scanner scanner, ChessGame game, ChessGame.TeamColor color) {
        ChessPosition pos = getPos(scanner, "\nWhat piece would you like to highlight moves for?");
        if (pos == null) {
            return;
        }
        if (game.getBoard().getPiece(pos) == null) {
            System.out.println("No piece at this position\n");
            return;
        }
        ArrayList<ChessMove> validMoves = game.validMoves(pos);
        ArrayList<ChessPosition> highlightPositions = new ArrayList<>();
        highlightPositions.add(pos);
        for (ChessMove move : validMoves) {
            highlightPositions.add(move.getEndPosition());
        }
        printGame(game.getBoard(), color, highlightPositions);
    }

    private void resign(Scanner scanner, String auth, int gameID, ChessGame.TeamColor color) {
        System.out.println("Are you sure you want to resign?\nEnter 1 to resign, Enter anything else to cancel");
        String input = scanner.nextLine();
        if (input.equals("1")) {
            webSocket.resign(auth, gameID, color);
        }
    }

    private void makeMove(Scanner scanner, String auth, ChessGame.TeamColor color, int gameID) {
        if (!checkTeamTurn(color)) {
            return;
        }
        ChessPosition startPos = getPos(scanner, "What piece would you like to move? (enter 'q' to quit)");
        if (startPos == null) {
            return;
        }
        //Bad piece at start location
        if (!checkStartPiece(startPos, color)) {
            return;
        }

        ChessPosition endPos = getPos(scanner, "Where would you like to move the piece?");
        if (endPos == null) {
            return;
        }

        webSocket.makeMove(auth, gameID, color, new ChessMove(startPos, endPos));
    }

    private boolean checkTeamTurn(ChessGame.TeamColor color) {
        if (!color.equals(gameData.game().getTeamTurn())) {
            System.out.println("It's not your turn!");
            return false;
        }
        return true;
    }

    private boolean checkStartPiece(ChessPosition pos, ChessGame.TeamColor color) {
        ChessPiece startPiece = gameData.game().getBoard().getPiece(pos);
        if (startPiece == null) {
            System.out.println("No piece at location\n");
            return false;
        } else if (startPiece.getTeamColor() != color) {
            System.out.println("Wrong piece color at location\n");
            return false;
        }
        return true;
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

    private void printGame(ChessBoard board, ChessGame.TeamColor color, ArrayList<ChessPosition> highlightPositions) {
        System.out.println();
        if (color == null || color.equals(ChessGame.TeamColor.WHITE)) {
            printGameWhite(board, highlightPositions);
        } else if (color.equals(ChessGame.TeamColor.BLACK)) {
            printGameBlack(board, highlightPositions);
        } else {
            throw new RuntimeException("Invalid color");
        }

        System.out.print(RESET_TEXT_COLOR);
        System.out.print(RESET_BG_COLOR);
        System.out.println(gameData.game().getTeamTurn().toString() + " turn!");
        System.out.println("Enter a number for what you would like to do (Enter 1 for help)!");
    }

    static void printGameWhite(ChessBoard board, ArrayList<ChessPosition> highlightPositions) {
        System.out.print(SET_BG_COLOR_BLACK);
        System.out.println();
        for (int i=8; i > 0; i--) {
            System.out.print(SET_TEXT_COLOR_LIGHT_GREY);
            System.out.print("  " + i + "  ");
            for (int j=1; j < 9; j++) {
                ChessPosition curPos = new ChessPosition(i, j);
                boolean highlighted = (highlightPositions.contains(curPos));
                printNextPiece(i, j, board, highlighted);
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

    static void printGameBlack(ChessBoard board, ArrayList<ChessPosition> highlightPositions) {
        System.out.print(SET_BG_COLOR_BLACK);
        System.out.println();
        for (int i=1; i < 9; i++) {
            System.out.print(SET_TEXT_COLOR_LIGHT_GREY);
            System.out.print("  " + i + "  ");
            for (int j=8; j > 0; j--) {
                ChessPosition curPos = new ChessPosition(i, j);
                boolean highlighted = (highlightPositions.contains(curPos));
                printNextPiece(i, j, board, highlighted);
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
            switchBackColor(false);
            System.out.print(SET_BG_COLOR_BLACK);
            System.out.print("\n");
        }
    }

    static void printNextPiece(int i, int j, ChessBoard board, boolean highlighted) {
        switchBackColor(highlighted);
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

    static void switchBackColor(boolean highlighted) {
        if (curBackColor.equals("White")) {
            System.out.print(SET_BG_COLOR_MEDIUM_GREY);
            if (highlighted) {
                System.out.print(SET_BG_COLOR_DARK_GREEN);
            }
            curBackColor = "Black";
        } else if (curBackColor.equals("Black")) {
            System.out.print(SET_BG_COLOR_EXTRA_LIGHT_GREY);
            if (highlighted) {
                System.out.print(SET_BG_COLOR_GREEN);
            }
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
        switch (serverMessage.getServerMessageType()) {
            case ERROR -> handleErrorMessage(new Gson().fromJson(message, ErrorMessage.class));
            case LOAD_GAME -> handleLoadGameMessage(new Gson().fromJson(message, LoadGameMessage.class));
            case NOTIFICATION -> handleNotificationMessage(new Gson().fromJson(message, NotificationMessage.class));
            default -> throw new RuntimeException("Unknown Server Message");
        }
    }

    private void handleErrorMessage(ErrorMessage errorMessage) {
        System.out.println(errorMessage.getErrorMessage());
        System.out.println("Enter a number for what you would like to do! (Enter 1 for help)");
    }

    private void handleNotificationMessage(NotificationMessage notificationMessage) {
        String message = notificationMessage.getMessage();
        System.out.println(message);
        if (message.contains("wins") || message.contains("tie")) {
            System.out.println("Enter 3 to exit");
            return;
        }
        if (!message.contains("move:")) {
            System.out.println("Enter a number for what you would like to do! (Enter 1 for help)");
        }
    }

    private void handleLoadGameMessage(LoadGameMessage loadGameMessage) {
        this.gameData = new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), loadGameMessage.getGame());
        printGame(loadGameMessage.getGame().getBoard(), loadGameMessage.getColor(), new ArrayList<>());
    }
}
