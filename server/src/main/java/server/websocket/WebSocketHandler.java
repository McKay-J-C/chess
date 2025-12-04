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
        System.out.println("Websocket connected");
        ctx.enableAutomaticPings();
    }

    @Override
    public void handleMessage(@NotNull WsMessageContext ctx) throws Exception {
        try {
            UserGameCommand userGameCommand = new Gson().fromJson(ctx.message(), UserGameCommand.class);


            Session session = ctx.session;

            String username = authorizeUser(userGameCommand.getAuthToken(), session);
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
                    leave(leaveCommand, session);
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
        if (gameData.whiteUsername().equals(username)) {
            color = ChessGame.TeamColor.WHITE;
        } else if (gameData.blackUsername().equals(username)){
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

        GameData gameData = gameDAO.getGame(gameID);
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

        connections.add(session, gameID, foundColor);

        String message = String.format("%s entered the game as %s", connectCommand.getUsername(), color);
        NotificationMessage notificationMessage = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(session, notificationMessage, gameID);

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
        ChessGame.TeamColor moveColor = makeMoveCommand.getColor();

        ChessMove move = makeMoveCommand.getMove();
        int gameID = makeMoveCommand.getGameID();
        GameData gameData = gameDAO.getGame(gameID);
        if (gameData == null) {
            sendError(session, "Game does not exist");
            return;
        }

        ChessGame game = gameData.game();

        try {
            executeMove(gameData, move, session, makeMoveCommand.getColor());
        } catch (InvalidMoveException ex) {
            sendError(session, String.format("Invalid move: %s", ex.getMessage()));
            return;
        }

        GameData newGameData = new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), game);
        gameDAO.deleteThenAddGame(newGameData);

        LoadGameMessage loadGameMessage = new LoadGameMessage(LOAD_GAME, game);
        connections.broadcast(null, loadGameMessage, gameID);

        String moveMessage = String.format("%s made move: %s", makeMoveCommand.getUsername(), move);
        NotificationMessage notificationMessage = new NotificationMessage(NOTIFICATION, moveMessage);
        connections.broadcast(session, notificationMessage, gameID);

        verifyCheck(makeMoveCommand, gameData);
    }

    private void executeMove(GameData gameData, ChessMove move, Session session, ChessGame.TeamColor color) throws InvalidMoveException, IOException, DataAccessException {
        ChessGame game = gameData.game();
        ChessGame.TeamColor colorTurn = game.getTeamTurn();
        if (colorTurn == null) {
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
        GameData newGameData = new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), game);
        gameDAO.deleteThenAddGame(newGameData);
    }
}
