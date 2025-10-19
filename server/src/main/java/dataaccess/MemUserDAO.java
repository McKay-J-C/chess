package dataaccess;

import model.AuthData;
import model.UserData;
import service.AlreadyTakenException;

import java.util.HashSet;

import static dataaccess.MemAuthDAO.createAuth;
import static model.AuthData.generateToken;

public class MemUserDAO implements UserDAO {

    static HashSet<UserData> userData = new HashSet<>();

    public static AuthData createUser(String username, String password, String email) {
        if (getUser(username) != null) {
            throw new AlreadyTakenException("Error: already taken");
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

    static void clear() {

    }
}
