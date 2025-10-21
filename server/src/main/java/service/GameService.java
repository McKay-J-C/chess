package service;

import dataaccess.DataAccessException;
import dataaccess.MemGameDAO;
import model.AuthData;
import model.GameData;
import response.*;
import request.*;
import dataaccess.MemGameDAO.*;

import java.util.HashSet;

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

    public static JoinGameResponse joinGame(String authToken, JoinGameRequest logoutRequest) throws DataAccessException {
        return new JoinGameResponse("default");
    }
}
