package service;

import chess.ChessGame;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;
import response.*;
import request.*;
import java.util.HashSet;

public class GameService {

    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public GameDAO getGameDAO() {
        return gameDAO;
    }

    public AuthDAO getAuthDAO() {
        return authDAO;
    }

    public ListGamesResponse listGames(ListGamesRequest listGamesRequest) throws DataAccessException {
        authDAO.authorize(listGamesRequest.authToken());
        HashSet<GameData> games = gameDAO.getGames();
        return new ListGamesResponse(games, null);
    }

    public CreateGameResponse createGame(String authToken, CreateGameRequest createGameRequest) throws DataAccessException {
        authDAO.authorize(authToken);
        int gameID = gameDAO.addGame(createGameRequest.gameName());
        return new CreateGameResponse(gameID, null);
    }

    public JoinGameResponse joinGame(String authToken, JoinGameRequest joinGameRequest) throws DataAccessException {
        AuthData auth = authDAO.authorize(authToken);
        int gameID = joinGameRequest.gameID();
        GameData game = gameDAO.getGame(gameID);
        if (game == null) {
            throw new BadRequestException("Error: No game " + gameID);
        }

        String color = joinGameRequest.playerColor();
        ChessGame.TeamColor teamColor = ChessGame.TeamColor.WHITE;

        if (color.equals("WHITE")) {
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

        gameDAO.updateGame(gameID, teamColor, auth.username());
        return new JoinGameResponse(null);
    }
}
