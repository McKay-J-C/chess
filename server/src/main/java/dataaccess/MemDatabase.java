package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.HashSet;

public class MemDatabase {
    static HashSet<UserData> userData = new HashSet<>();
    static HashSet<GameData> gameData = new HashSet<>();
    static int curID = 1;
    static HashSet<AuthData> authData = new HashSet<>();
}
