package service;

import result.*;
import request.*;

public class GameService {

    public ListGamesResult listGames(ListGamesRequest listGamesRequest) {
        return new ListGamesResult();
    }

    public CreateGameResult createGame(CreateGameRequest createGameRequest) {
        return new CreateGameResult(1);
    }

    public JoinGameResult joinGame(JoinGameRequest logoutRequest) {
        return new JoinGameResult();
    }
}
