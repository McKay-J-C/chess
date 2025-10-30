package dataaccess;

import model.AuthData;

import java.util.HashSet;

import static dataaccess.MemDatabase.authData;
import static model.AuthData.generateToken;

public class MemAuthDAO implements AuthDAO {

    @Override
    public AuthData createAuth(String username) {
        String token = generateToken();
        AuthData auth = new AuthData(token, username);
        authData.add(auth);
        return auth;
    }

    @Override
    public AuthData getAuth(String authToken) {
        for (AuthData auth : authData) {
            if (auth.authToken().equals(authToken)) {
                return auth;
            }
        }
        return null;
    }

    @Override
    public void deleteAuth(AuthData auth) {
        authData.remove(auth);
    }

    @Override
    public void clear() {
        authData = new HashSet<>();
    }

    @Override
    public HashSet<AuthData> getAuths() {
        return authData;
    }
}
