package dataaccess;

import model.AuthData;
import model.UserData;

import java.util.HashSet;

import static dataaccess.MemDatabase.userData;


public class MemUserDAO implements UserDAO {


    @Override
    public AuthData createUser(String username, String password, String email) {
        if (getUser(username) != null) {
            throw new DataAccessException.AlreadyTakenException("Error: already taken");
        }
        userData.add(new UserData(username, password, email));
        MemAuthDAO authDAO = new MemAuthDAO();
        return authDAO.createAuth(username);
    }

    @Override
    public UserData getUser(String username) {
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

    @Override
    public void clear() {
        userData = new HashSet<>();
    }

    @Override
    public HashSet<UserData> getUsers() {
        return userData;
    }

}
