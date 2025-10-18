package dataaccess;

import model.UserData;

public class UserDAO {

    static UserData createUser() {
        return new UserData();
    }

    static UserData getUser(String username) {
        return new UserData();
    }

    static void clear() {

    }
}
