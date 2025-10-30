package dataaccess;

import model.AuthData;
import model.UserData;

import java.util.HashSet;

public interface UserDAO {


    default AuthData createUser(String username, String password, String email) {
        return null;
    }

    default UserData getUser(String username) {
        return new UserData("", "", "");
    }

    default void clear() {

    }

    default HashSet<UserData> getUsers() {
        return null;
    }
}
