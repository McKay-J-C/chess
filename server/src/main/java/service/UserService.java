package service;

import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import response.*;
import request.*;

import java.util.Objects;

import static dataaccess.MemAuthDAO.*;
import static dataaccess.MemUserDAO.*;

public class UserService {

    public static RegisterResponse register(RegisterRequest registerRequest) throws DataAccessException, DataAccessException.AlreadyTakenException {
        String username = registerRequest.username();
        String password = registerRequest.password();
        String email = registerRequest.email();

//        if (username == null || password == null || email == null) {
//            throw new BadRequestException("Error: bad request");
//        }

        AuthData userAuth = createUser(username, password, email);
        return new RegisterResponse(userAuth.username(), userAuth.authToken(), null);
    }

    public static LoginResponse login(LoginRequest loginRequest) {
        String username = loginRequest.username();
        String password = loginRequest.password();

        UserData user = getUser(username);
        if (user == null) {
            throw new DataAccessException.UnauthorizedException("Error: username not found");
        }
        if (!Objects.equals(user.password(), password)) {
            throw new DataAccessException.UnauthorizedException("Error: incorrect password");
        }

        AuthData authData = createAuth(username);
        return new LoginResponse(username, authData.authToken(), null);
    }

    public static LogoutResponse logout(LogoutRequest logoutRequest) throws DataAccessException {
        AuthData auth = authorize(logoutRequest.authToken());
        deleteAuth(auth);
        return new LogoutResponse(null);
    }
}
