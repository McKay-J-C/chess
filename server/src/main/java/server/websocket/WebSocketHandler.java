package server.websocket;

import chess.ChessGame;
import chess.ChessMove;
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
            String username = authDAO.getAuth(userGameCommand.getAuthToken()).username();
            switch (userGameCommand.getCommandType()) {
                case CONNECT -> connect(new ConnectCommand(UserGameCommand.CommandType.CONNECT, userGameCommand.getAuthToken(), userGameCommand.getGameID(), username, ChessGame.TeamColor.WHITE), ctx.session);
                case RESIGN -> resign(new ResignCommand(UserGameCommand.CommandType.RESIGN, userGameCommand.getAuthToken(), userGameCommand.getGameID(), username, ChessGame.TeamColor.WHITE), ctx.session);
                case LEAVE -> leave(new LeaveCommand(UserGameCommand.CommandType.LEAVE, userGameCommand.getAuthToken(), userGameCommand.getGameID(), username, ChessGame.TeamColor.WHITE), ctx.session);
                case MAKE_MOVE -> makeMove(new MakeMoveCommand(UserGameCommand.CommandType.MAKE_MOVE, userGameCommand.getAuthToken(), userGameCommand.getGameID(), new ChessMove(null, null, null), username, ChessGame.TeamColor.WHITE), ctx.session);
            }
            new Gson().fromJson(ctx.message(), UserGameCommand.class);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void connect(ConnectCommand connectCommand, Session session) throws IOException, DataAccessException {
        connections.add(session);

        GameData gameData = gameDAO.getGame(connectCommand.getGameID());
        if (gameData == null) {
            ErrorMessage errorMessage = new ErrorMessage(ERROR, "Game does not exist");
            String errorMessageJson = new Gson().toJson(errorMessage);
            session.getRemote().sendString(errorMessageJson);
            return;
        }

        String message = String.format("%s entered the game as %s", connectCommand.getUsername(), connectCommand.getColor().name());
        NotificationMessage notificationMessage = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(session, notificationMessage);


        ChessGame game = gameData.game();
        LoadGameMessage loadGameMessage = new LoadGameMessage(LOAD_GAME, game);
        String loadGameMessageJson = new Gson().toJson(loadGameMessage);
        session.getRemote().sendString(loadGameMessageJson);
    }

    private void resign(ResignCommand resignCommand, Session session) {

    }

    private void leave(LeaveCommand leaveCommand, Session session) {

    }

    private void makeMove(MakeMoveCommand makeMoveCommand, Session session) {

    }
}
