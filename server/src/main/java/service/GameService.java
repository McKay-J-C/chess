package service;

import response.*;
import request.*;

public class GameService {

    public ListGamesResponse listGames(ListGamesRequest listGamesRequest) {
        return new ListGamesResponse("default");
    }

    public CreateGameResponse createGame(CreateGameRequest createGameRequest) {
        return new CreateGameResponse(1, "default");
    }

    public JoinGameResponse joinGame(JoinGameRequest logoutRequest) {
        return new JoinGameResponse("default");
    }
}
