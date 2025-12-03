package server.websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import chess.InvalidMoveException;
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
        System.out.println("Websocket connected");
        ctx.enableAutomaticPings();
    }

    @Override
    public void handleMessage(@NotNull WsMessageContext ctx) throws Exception {
        try {
            UserGameCommand userGameCommand = new Gson().fromJson(ctx.message(), UserGameCommand.class);
            AuthData authData = authDAO.getAuth(userGameCommand.getAuthToken());

            Session session = ctx.session;
            if (authData == null) {
                sendError(session, "Unauthorized");
                return;
            }
            String username = authData.username();

            switch (userGameCommand.getCommandType()) {
                case CONNECT -> {
                    ConnectCommand connectCommand = new Gson().fromJson(ctx.message(), ConnectCommand.class);
                    connect(connectCommand, session);
                }
                case RESIGN -> {
                    ResignCommand resignCommand = new Gson().fromJson(ctx.message(), ResignCommand.class);
                    resign(resignCommand, session);
                }
                case LEAVE -> {
                    LeaveCommand leaveCommand = new Gson().fromJson(ctx.message(), LeaveCommand.class);
                    leave(leaveCommand, session);
                }
                case MAKE_MOVE -> {
                    MakeMoveCommand makeMoveCommand = new Gson().fromJson(ctx.message(), MakeMoveCommand.class);
                    makeMove(makeMoveCommand, session);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void connect(ConnectCommand connectCommand, Session session) throws IOException, DataAccessException {
        connections.add(session);

        GameData gameData = gameDAO.getGame(connectCommand.getGameID());
        if (gameData == null) {
            sendError(session, "Game does not exist");
            return;
        }
        ChessGame game = gameData.game();

        String color;
        ChessGame.TeamColor foundColor = connectCommand.getColor();
        if (foundColor == null) {
            color = "Unknown";
        } else {
            color = foundColor.name();
        }
        String message = String.format("%s entered the game as %s", connectCommand.getUsername(), color);
        NotificationMessage notificationMessage = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(session, notificationMessage);

        LoadGameMessage loadGameMessage = new LoadGameMessage(LOAD_GAME, game);
        String loadGameMessageJson = new Gson().toJson(loadGameMessage);
        session.getRemote().sendString(loadGameMessageJson);
    }

    private void sendError(Session session, String message) throws IOException {
        ErrorMessage errorMessage = new ErrorMessage(ERROR, message);
        String errorMessageJson = new Gson().toJson(errorMessage);
        session.getRemote().sendString(errorMessageJson);
    }

    private void resign(ResignCommand resignCommand, Session session) {

    }

    private void leave(LeaveCommand leaveCommand, Session session) {

    }

    private void makeMove(MakeMoveCommand makeMoveCommand, Session session) throws DataAccessException, IOException, InvalidMoveException {
        ChessMove move = makeMoveCommand.getMove();
        GameData gameData = gameDAO.getGame(makeMoveCommand.getGameID());
        if (gameData == null) {
            sendError(session, "Game does not exist");
            return;
        }

        ChessGame game = gameData.game();
        try {
            game.makeMove(move);
        } catch (InvalidMoveException ex) {
            sendError(session, String.format("Invalid move: %s", ex.getMessage()));
        }

        GameData newGameData = new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), game);
        gameDAO.deleteThenAddGame(newGameData);

        LoadGameMessage loadGameMessage = new LoadGameMessage(LOAD_GAME, game);
        connections.broadcast(null, loadGameMessage);

        String moveMessage = String.format("%s made move: %s", makeMoveCommand.getUsername(), move);
        NotificationMessage notificationMessage = new NotificationMessage(NOTIFICATION, moveMessage);
        connections.broadcast(session, notificationMessage);

        verifyCheck(makeMoveCommand, game);

    }

    private void verifyCheck(MakeMoveCommand makeMoveCommand, ChessGame game) throws IOException {
        ChessGame.TeamColor checkColor = ChessGame.TeamColor.WHITE;
        String checkMessage = "";
        boolean needMessage = false;
        if (makeMoveCommand.getColor() == ChessGame.TeamColor.WHITE) {
            checkColor = ChessGame.TeamColor.BLACK;
        }

        if (game.isInCheck(checkColor)) {
            needMessage = true;
            checkMessage = String.format("%s player is in check", checkColor);
            if (game.isInCheckmate(checkColor)) {
                checkMessage = String.format("%s player is in checkmate", checkColor);
            }
        }

        if (game.isInStalemate(checkColor)) {
            needMessage = true;
            checkMessage = String.format("%s player is in stalemate", checkColor);
        }

        if (needMessage) {
            NotificationMessage notificationMessage = new NotificationMessage(NOTIFICATION, checkMessage);
            connections.broadcast(null, notificationMessage);
        }
    }
}
