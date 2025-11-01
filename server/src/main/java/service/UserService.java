package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;
import response.*;
import request.*;

import java.sql.SQLException;


public class UserService {

    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public UserDAO getUserDAO() {
        return userDAO;
    }

    public AuthDAO getAuthDAO() {
        return authDAO;
    }

    public RegisterResponse register(RegisterRequest registerRequest) throws DataAccessException, DataAccessException.AlreadyTakenException {
        String username = registerRequest.username();
        String password = registerRequest.password();
        String email = registerRequest.email();

        AuthData userAuth = userDAO.createUser(username, password, email);
        return new RegisterResponse(userAuth.username(), userAuth.authToken(), null);
    }

    public LoginResponse login(LoginRequest loginRequest) throws DataAccessException {
        String username = loginRequest.username();
        String password = loginRequest.password();

        UserData user = userDAO.getUser(username);
        if (user == null) {
            throw new DataAccessException.UnauthorizedException("Error: username not found");
        }
        if (!userDAO.verifyUser(username, password)) {
            throw new DataAccessException.UnauthorizedException("Error: incorrect password");
        }

        AuthData authData = authDAO.createAuth(username);
        return new LoginResponse(username, authData.authToken(), null);
    }

    public LogoutResponse logout(LogoutRequest logoutRequest) throws DataAccessException {
        AuthData auth = authDAO.authorize(logoutRequest.authToken());
        authDAO.deleteAuth(auth);
        return new LogoutResponse(null);
    }
}
