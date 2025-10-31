package dataaccess;

import model.AuthData;
import model.UserData;

import java.sql.SQLException;
import java.util.HashSet;

public interface UserDAO {


    default AuthData createUser(String username, String password, String email) throws SQLException, DataAccessException {
        return null;
    }

    default UserData getUser(String username) throws DataAccessException {
        return new UserData("", "", "");
    }

    default void clear() throws DataAccessException {

    }

    default HashSet<UserData> getUsers() {
        return null;
    }

    default boolean verifyUser(String username, String providedClearTextPassword) throws DataAccessException {
        return false;
    }
}
