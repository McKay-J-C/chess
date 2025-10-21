package dataaccess;

import model.AuthData;
import model.UserData;

import java.util.HashSet;

import static dataaccess.MemAuthDAO.createAuth;

public class MemUserDAO implements UserDAO {

    static HashSet<UserData> userData = new HashSet<>();

    public static AuthData createUser(String username, String password, String email) {
        if (getUser(username) != null) {
            throw new DataAccessException.AlreadyTakenException("Error: already taken");
        }
        userData.add(new UserData(username, password, email));
        return createAuth(username);
    }

    public static UserData getUser(String username) {
        if (userData == null) {
            return null;
        }
        for (UserData user : userData) {
            if (user.username().equals(username)) {
                return user;
            }
        }
        return null;
    }

    public static void clear() {
        userData = new HashSet<>();
    }
}
