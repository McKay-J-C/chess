package server.websocket;

import chess.*;
import com.google.gson.Gson;
import dataaccess.*;
import model.*;
import org.jetbrains.annotations.NotNull;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsCloseHandler;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsConnectHandler;
import io.javalin.websocket.WsMessageContext;
import io.javalin.websocket.WsMessageHandler;
import org.eclipse.jetty.websocket.api.Session;
import websocket.commands.*;
import websocket.messages.*;

import java.io.IOException;
import static websocket.messages.ServerMessage.ServerMessageType.*;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {

    private final ConnectionManager connections = new ConnectionManager();
    private final AuthDAO authDAO = new SqlAuthDAO();
    private final GameDAO gameDAO = new SqlGameDAO();

    @Override
    public void handleClose(@NotNull WsCloseContext ctx) throws Exception {
        System.out.println("Websocket closed");
    }

    @Override
    public void handleConnect(@NotNull WsConnectContext ctx) throws Exception {
        System.out.println("Websocket connected! session = " + ctx.session);
        ctx.enableAutomaticPings();
    }

    @Override
    public void handleMessage(@NotNull WsMessageContext ctx) throws Exception {
        System.out.println("Entering handle message");
        try {
            UserGameCommand userGameCommand = new Gson().fromJson(ctx.message(), UserGameCommand.class);
            Session session = ctx.session;
            System.out.println("SERVER RECEIVED: " + ctx.message());

            String username = authorizeUser(userGameCommand.getAuthToken(), session);
            System.out.println("Parsed username = " + username);
            if (username == null) {
                return;
            }
            GameData gameData = getGameData(userGameCommand.getGameID(), session);
            ChessGame.TeamColor color = getTeamColor(gameData, username);

            switch (userGameCommand.getCommandType()) {
                case CONNECT -> {
                    ConnectCommand connectCommand = new ConnectCommand(UserGameCommand.CommandType.CONNECT,
                            userGameCommand.getAuthToken(), userGameCommand.getGameID(), username, color);
                    connect(connectCommand, session);
                }
                case RESIGN -> {
                    ResignCommand resignCommand = new ResignCommand(UserGameCommand.CommandType.RESIGN,
                            userGameCommand.getAuthToken(), userGameCommand.getGameID(), username, color);
                    resign(resignCommand, session);
                }
                case LEAVE -> {
                    LeaveCommand leaveCommand = new LeaveCommand(UserGameCommand.CommandType.LEAVE,
                            userGameCommand.getAuthToken(), userGameCommand.getGameID(), username, color);
                    System.out.println("RECEIVED LEAVE COMMAND: " + leaveCommand);
                    leave(leaveCommand, session, color);
                }
                case MAKE_MOVE -> {
                    MakeMoveCommand makeMoveCommand = new Gson().fromJson(ctx.message(), MakeMoveCommand.class);
                    makeMoveCommand.setUsername(username);
                    makeMoveCommand.setColor(color);
                    makeMove(makeMoveCommand, session);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String authorizeUser(String authToken, Session session) throws DataAccessException, IOException {
        AuthData authData = authDAO.getAuth(authToken);
        if (authData == null) {
            sendError(session, "Unauthorized");
            return null;
        }
        return authData.username();
    }

    private ChessGame.TeamColor getTeamColor(GameData gameData, String username) {
        ChessGame.TeamColor color = null;
        String whiteUsername = gameData.whiteUsername();
        String blackUsername = gameData.blackUsername();
        if (whiteUsername != null && whiteUsername.equals(username)) {
            color = ChessGame.TeamColor.WHITE;
        } else if (blackUsername != null && blackUsername.equals(username)){
            color = ChessGame.TeamColor.BLACK;
        }
        return color;
    }

    private GameData getGameData(int gameID, Session session) throws DataAccessException, IOException {
        GameData gameData = gameDAO.getGame(gameID);
        if (gameData == null) {
            sendError(session, "No game with given gameID");
        }
        return gameData;
    }

    private void connect(ConnectCommand connectCommand, Session session) throws IOException, DataAccessException {
        int gameID = connectCommand.getGameID();

        GameData gameData = getGameData(gameID, session);
        if (gameData == null) return;
        ChessGame game = gameData.game();

        ChessGame.TeamColor foundColor = connectCommand.getColor();
        String color = makeColorString(foundColor);
        connections.add(session, gameID, foundColor);

        String message = String.format("%s entered the game as %s", connectCommand.getUsername(), color);
        NotificationMessage notificationMessage = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(session, notificationMessage, gameID);

        LoadGameMessage loadGameMessage = new LoadGameMessage(LOAD_GAME, game);
        String loadGameMessageJson = new Gson().toJson(loadGameMessage);
        session.getRemote().sendString(loadGameMessageJson);
    }

    private String makeColorString(ChessGame.TeamColor foundColor) {
        String color;
        if (foundColor == null) {
            color = "";
        } else {
            color = "as " + foundColor.name();
        }
        return color;
    }

    private void sendError(Session session, String message) throws IOException {
        ErrorMessage errorMessage = new ErrorMessage(ERROR, message);
        String errorMessageJson = new Gson().toJson(errorMessage);
        session.getRemote().sendString(errorMessageJson);
    }

    private void resign(ResignCommand resignCommand, Session session) throws IOException, DataAccessException {
        GameData gameData = getGameData(resignCommand.getGameID(), session);
        if (gameData.game().isGameOver()) {
            sendError(session, "Game is already resigned");
            return;
        }
        endGame(gameData);

        ChessGame.TeamColor winColor;
        if (resignCommand.getColor() == null) {
            sendError(session, "Observer cannot resign match");
            return;
        }
        if (resignCommand.getColor() == ChessGame.TeamColor.WHITE) {
            winColor = ChessGame.TeamColor.BLACK;
        } else {
            winColor = ChessGame.TeamColor.WHITE;
        }

        String resignMessage = String.format("%s resigned - %s player wins!", resignCommand.getUsername(), winColor);
        NotificationMessage notificationMessage = new NotificationMessage(NOTIFICATION, resignMessage);
        connections.broadcast(null, notificationMessage, resignCommand.getGameID());
    }

    private void leave(LeaveCommand leaveCommand, Session session, ChessGame.TeamColor color) throws IOException, DataAccessException {
        connections.remove(session, leaveCommand.getGameID(), color);
        if (color != null) {
            gameDAO.updateGame(leaveCommand.getGameID(), color, null);
        }

        String leaveMessage = String.format("%s left the game", leaveCommand.getUsername());
        NotificationMessage notificationMessage = new NotificationMessage(NOTIFICATION, leaveMessage);
        connections.broadcast(session, notificationMessage, leaveCommand.getGameID());
    }

    private void makeMove(MakeMoveCommand makeMoveCommand, Session session) throws DataAccessException, IOException, InvalidMoveException {
        ChessGame.TeamColor moveColor = makeMoveCommand.getColor();

        ChessMove move = makeMoveCommand.getMove();
        int gameID = makeMoveCommand.getGameID();

        GameData gameData = getGameData(gameID, session);
        ChessGame game;

        try {
            game = executeMove(gameData, move, session, moveColor);
        } catch (InvalidMoveException ex) {
            sendError(session, String.format("Invalid move: %s", ex.getMessage()));
            return;
        }

        GameData newGameData = new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), game);
        String gameJson = new Gson().toJson(newGameData.game());
        gameDAO.updateMove(gameJson, gameID);

        LoadGameMessage loadGameMessage = new LoadGameMessage(LOAD_GAME, game);
        connections.broadcast(null, loadGameMessage, gameID);

        String moveMessage = String.format("%s made move: %s", makeMoveCommand.getUsername(), move);
        NotificationMessage notificationMessage = new NotificationMessage(NOTIFICATION, moveMessage);
        connections.broadcast(session, notificationMessage, gameID);

        verifyCheck(makeMoveCommand, newGameData);
    }

    private ChessGame executeMove(GameData gameData, ChessMove move, Session session, ChessGame.TeamColor color) throws InvalidMoveException, IOException, DataAccessException {
        ChessGame game = gameData.game();
        ChessGame.TeamColor colorTurn = game.getTeamTurn();
        if (colorTurn == null || game.isGameOver()) {
            throw new InvalidMoveException("Game is over");
        }

        ChessPiece movingPiece = game.getBoard().getPiece(move.getStartPosition());

        if (movingPiece.getTeamColor() == null) {
            throw new InvalidMoveException("No piece at location");
        }
        if (movingPiece.getTeamColor() != color) {
            throw new InvalidMoveException("Wrong color piece at location");
        }
        game.makeMove(move);
        return game;
    }

    private void verifyCheck(MakeMoveCommand makeMoveCommand, GameData gameData) throws IOException, DataAccessException {
        ChessGame.TeamColor checkColor = ChessGame.TeamColor.WHITE;
        String checkMessage = "";
        boolean needMessage = false;
        boolean gameOver = false;
        ChessGame.TeamColor moveColor = makeMoveCommand.getColor();
        int gameID = makeMoveCommand.getGameID();

        if (moveColor == ChessGame.TeamColor.WHITE) {
            checkColor = ChessGame.TeamColor.BLACK;
        }

        ChessGame game = gameData.game();
        if (game.isInCheck(checkColor)) {
            needMessage = true;
            checkMessage = String.format("%s player is in check!", checkColor);
            if (game.isInCheckmate(checkColor)) {
                checkMessage = String.format("%s player is in checkmate! %s player wins!", checkColor, moveColor);
                gameOver = true;
            }
        }

        if (game.isInStalemate(checkColor)) {
            needMessage = true;
            gameOver = true;
            checkMessage = String.format("%s player is in stalemate! You tie!", checkColor);
        }

        if (needMessage) {
            NotificationMessage notificationMessage = new NotificationMessage(NOTIFICATION, checkMessage);
            connections.broadcast(null, notificationMessage, gameID);
            if (gameOver) {
                endGame(gameData);
            }
        }
    }

    private void endGame(GameData gameData) throws DataAccessException {
        ChessGame game = gameData.game();
        game.setTeamTurn(null);
        game.gameOver();
        String gameJson = new Gson().toJson(game);
        gameDAO.updateMove(gameJson, gameData.gameID());
    }

//    private void clear() {
//        connections.clear();
//    }
}
