package service;

import result.*;
import request.*;

public class UserService {

    public RegisterResult register(RegisterRequest registerRequest) {
        return new RegisterResult("","");
    }

    public LoginResult login(LoginRequest loginRequest) {
        return new LoginResult("", "");
    }

    public void logout(LogoutRequest logoutRequest) {

    }
}
