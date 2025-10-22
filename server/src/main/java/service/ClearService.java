package service;

import dataaccess.*;
import response.ClearResponse;

public class ClearService {
    public static ClearResponse clear() {
        MemAuthDAO.clear();
        MemGameDAO.clear();
        MemUserDAO.clear();
        return new ClearResponse(null);
    }
}
