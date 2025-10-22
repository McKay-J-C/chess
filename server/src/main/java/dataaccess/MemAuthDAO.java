package dataaccess;

import model.AuthData;

import java.util.HashSet;

import static model.AuthData.generateToken;

public class MemAuthDAO implements AuthDAO {

    static HashSet<AuthData> authData = new HashSet<>();

    public static AuthData createAuth(String username) {
        String token = generateToken();
        AuthData auth = new AuthData(token, username);
        authData.add(auth);
        return auth;
    }

    public static AuthData getAuth(String authToken) {
        for (AuthData auth : authData) {
            if (auth.authToken().equals(authToken)) {
                return auth;
            }
        }
        return null;
    }

    public static void deleteAuth(AuthData auth) {
        authData.remove(auth);
    }

    public static void clear() {
        authData = new HashSet<>();
    }

    public static AuthData authorize(String authToken) throws DataAccessException {
        AuthData authData = MemAuthDAO.getAuth(authToken);
        if (authData == null) {
            throw new DataAccessException.UnauthorizedException("Error: unauthorized");
        }
        return authData;
    }

    public static HashSet<AuthData> getAuths() {
        return authData;
    }
}
