package service;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.MemGameDAO;
import model.AuthData;
import model.GameData;
import response.*;
import request.*;
import dataaccess.MemGameDAO.*;

import java.util.HashSet;
import java.util.Objects;

import static dataaccess.MemAuthDAO.authorize;
import static dataaccess.MemGameDAO.*;

public class GameService {

    public static ListGamesResponse listGames(ListGamesRequest listGamesRequest) throws DataAccessException {
        authorize(listGamesRequest.authToken());
        HashSet<GameData> games = getGames();
        return new ListGamesResponse(games, null);
    }

    public static CreateGameResponse createGame(String authToken, CreateGameRequest createGameRequest) throws DataAccessException {
        authorize(authToken);
        int gameID = addGame(createGameRequest.gameName());
        return new CreateGameResponse(gameID, null);
    }

    public static JoinGameResponse joinGame(String authToken, JoinGameRequest joinGameRequest) throws DataAccessException {
        AuthData auth = authorize(authToken);
        int gameID = joinGameRequest.gameID();
        GameData game = getGame(gameID);
        if (game == null) {
            throw new BadRequestException("Error: No game " + gameID);
        }

        String color = joinGameRequest.playerColor();
        ChessGame.TeamColor teamColor;

        if (color.equals("WHITE")) {
            teamColor = ChessGame.TeamColor.WHITE;
            if (game.whiteUsername() != null) {
                throw new DataAccessException.AlreadyTakenException("Error: already taken");
            }

        } else if (color.equals("BLACK")) {
            teamColor = ChessGame.TeamColor.BLACK;
            if (game.blackUsername() != null) {
                throw new DataAccessException.AlreadyTakenException("Error: already taken");
            }

        } else {
            throw new BadRequestException("Error: Not a valid color");
        }

        updateGame(gameID, teamColor, auth.username());
        return new JoinGameResponse(null);
    }
}
