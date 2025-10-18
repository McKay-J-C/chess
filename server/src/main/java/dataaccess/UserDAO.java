package dataaccess;

import model.UserData;

public class UserDAO {

    public static UserData createUser() {
        return new UserData("", "", "");
    }

    public static UserData getUser(String username) {
        return new UserData("", "", "");
    }

    public static void clear() {

    }
}
