package dataaccess;

import model.AuthData;
import java.util.HashSet;

import static model.AuthData.generateToken;

public class MemAuthDAO implements AuthDAO {

    static HashSet<AuthData> authData = new HashSet<>();

    static AuthData createAuth(String username) {
        String token = generateToken();
        AuthData auth = new AuthData(token, username);
        authData.add(auth);
        return auth;
    }

    static AuthData getAuth() {
        return null;
    }

    static void deleteAuth() {

    }

    static void clear() {

    }
}
