package dataaccess;

import model.AuthData;

import static dataaccess.MemAuthDAO.getAuth;

public interface AuthDAO {

    static AuthData createAuth() {
        return null;
    }

    static AuthData getAuth() {
        return null;
    }

    static void deleteAuth() {

    }

    static void clear() {

    }

    static AuthData authorize() {
        return null;
    }
}
