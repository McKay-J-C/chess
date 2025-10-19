package dataaccess;

import model.AuthData;
import model.UserData;

public interface UserDAO {

    static AuthData createUser(String username, String password, String email) {
        return new AuthData("", "");
    }

    static UserData getUser(String username) {
        return new UserData("", "", "");
    }

    static void clear() {

    }
}
