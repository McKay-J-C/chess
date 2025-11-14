package Client;

import model.GameData;
import request.CreateGameRequest;
import request.ListGamesRequest;
import request.LogoutRequest;
import response.CreateGameResponse;
import response.ListGamesResponse;
import server.ServerFacade;

import java.util.HashSet;
import java.util.Scanner;

import static ui.EscapeSequences.BLACK_KING;
import static ui.EscapeSequences.WHITE_KING;
import static Client.PreloginClient.handleError;

public class PostloginClient {

    private final ServerFacade server;

    private String help =
            """
            Enter a number for what you would like to do!
            
            1: Help
            2: Logout
            3: Create Game
            4: List Games
            5: Play Game
            6: Observe Game
            
            """;

    public PostloginClient(ServerFacade server) {
        this.server = server;
    }

    public void run(String auth) {
        System.out.println("Hi There!");

        Scanner scanner = new Scanner(System.in);
        String line = "";

        while (!line.equals("2")) {
            System.out.print(help);
            line = scanner.nextLine();
            eval(line, scanner, auth);
        }
    }

    private void eval(String line, Scanner scanner, String auth) {
        switch (line) {
            case "1" -> System.out.print(help);
            case "2" -> logout(auth);
            case "3" -> createGame(scanner, auth);
            case "4" -> listGames(auth);
//            case "5" -> playGame(scanner, auth);
//            case "6" -> observeGame(scanner);
            default -> System.out.print("\nInvalid input - Please Enter a number 1-6\n\n");
        }
    }

    private void logout(String auth) {
        System.out.println("Come back soon!\n");
        server.logout(new LogoutRequest(auth));
    }

    private void createGame(Scanner scanner, String auth) {
        System.out.println("Enter your game name: ");
        String gameName = scanner.nextLine();
        if (gameName.isEmpty()) {
            createGame(scanner, auth);
        }
        try {
            server.createGame(new CreateGameRequest(gameName), auth);
        } catch (Exception ex) {
            handleError(ex, null);
        }
    }

    private void listGames(String auth) {
        System.out.println("Current Games: \n");
        try {
            HashSet<GameData> games = server.listGames(new ListGamesRequest(auth), auth).games();
            int gameNum = 1;
            for (GameData game : games) {
                System.out.println(gameNum + ": " + game.toString() + "\n");
                gameNum++;
            }
        } catch (Exception ex) {
            handleError(ex, null);
        }
    }

}
