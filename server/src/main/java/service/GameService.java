package service;

import dataaccess.DataAccessException;
import dataaccess.MemGameDAO;
import model.AuthData;
import response.*;
import request.*;
import dataaccess.MemGameDAO.*;
import static dataaccess.MemAuthDAO.authorize;
import static dataaccess.MemGameDAO.addGame;

public class GameService {

    public static ListGamesResponse listGames(ListGamesRequest listGamesRequest) {
        return new ListGamesResponse("default");
    }

    public static CreateGameResponse createGame(CreateGameRequest createGameRequest) throws DataAccessException {
        authorize(createGameRequest.authToken());
        int gameID = addGame(createGameRequest.gameName());
        return new CreateGameResponse(gameID, null);
    }

    public static JoinGameResponse joinGame(JoinGameRequest logoutRequest) {
        return new JoinGameResponse("default");
    }
}
