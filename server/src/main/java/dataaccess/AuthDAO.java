package dataaccess;

import model.AuthData;

import java.util.HashSet;


public interface AuthDAO {

    default AuthData createAuth(String username) throws DataAccessException {
        return null;
    }

    default AuthData getAuth(String authToken) throws DataAccessException {
        return null;
    }

    default void deleteAuth(AuthData auth) throws DataAccessException {
    }

    default AuthData authorize(String authToken) throws DataAccessException {
        AuthData authData = this.getAuth(authToken);
        if (authData == null) {
            throw new DataAccessException.UnauthorizedException("Error: unauthorized");
        }
        return authData;
    }

    default void clear() throws DataAccessException {
    }

    default HashSet<AuthData> getAuths() throws DataAccessException {
        return null;
    }
}
