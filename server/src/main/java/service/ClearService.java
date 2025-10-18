package service;

import dataaccess.GameDAO;

public class ClearService {
    void clear() {
        GameDAO.clear();
    }
}
