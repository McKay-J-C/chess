package service;

import dataaccess.*;

public class ClearService {
    public static void clear() {
        MemAuthDAO.clear();
        MemGameDAO.clear();
        MemUserDAO.clear();
    }
}
