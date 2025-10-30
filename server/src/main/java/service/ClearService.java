package service;

import dataaccess.*;
import response.ClearResponse;

public class ClearService {

    private final AuthDAO authDAO;
    private final GameDAO gameDAO;
    private final UserDAO userDAO;

    public ClearService(AuthDAO authDAO, GameDAO gameDAO, UserDAO userDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
        this.userDAO = userDAO;
    }

    public AuthDAO getAuthDAO() {
        return authDAO;
    }

    public GameDAO getGameDAO() {
        return gameDAO;
    }

    public UserDAO getUserDAO() {
        return userDAO;
    }

    public ClearResponse clear() {
        authDAO.clear();
        gameDAO.clear();
        userDAO.clear();
        return new ClearResponse(null);
    }
}
