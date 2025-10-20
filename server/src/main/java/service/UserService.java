package service;

import dataaccess.DataAccessException;
import model.AuthData;
import response.*;
import request.*;
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

    public LoginResponse login(LoginRequest loginRequest) {
        return new LoginResponse("", "", "default");
    }

    public void logout(LogoutRequest logoutRequest) {

    }
}
