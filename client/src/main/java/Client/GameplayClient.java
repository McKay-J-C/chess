package Client;

import server.ServerFacade;

public class GameplayClient {

    private final ServerFacade server;

    public GameplayClient(ServerFacade server) {
        this.server = server;
    }

    public void run(String auth, int gameID) {
        System.out.println("Running Gameplay");
    }
}
