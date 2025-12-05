package client;

import chess.ChessGame;
import model.GameData;
import request.CreateGameRequest;
import request.JoinGameRequest;
import request.ListGamesRequest;
import request.LogoutRequest;
import server.ServerFacade;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

import static client.PreloginClient.handleError;
import static chess.ChessGame.TeamColor.*;

public class PostloginClient {

    private HashMap<Integer, GameData> gameMap = new HashMap<>();
    private final ServerFacade server;

    private final String help =
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
            case "4" -> listGames(scanner, auth);
            case "5" -> playGame(scanner, auth);
            case "6" -> observeGame(scanner, auth);
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

    private void listGames(Scanner scanner, String auth) {
        System.out.println("Current Games: \n");
        try {
            HashSet<GameData> games = server.listGames(new ListGamesRequest(auth), auth).games();
            int gameNum = 1;
            gameMap.clear();
            for (GameData game : games) {
                System.out.println(gameNum + ": " + game.toString() + "\n");
                gameMap.put(gameNum, game);
                gameNum++;
            }
        } catch (Exception ex) {
            handleError(ex, null);
        }
    }

    private void playGame(Scanner scanner, String auth) {
        GameData gameData = joinGame(scanner, auth);
        if (gameData == null) {
            return;
        }
        System.out.println("What color would you like to play as? (enter \"q\" to exit)");
        System.out.println("1: White\n2: Black");
        String colorNum = scanner.nextLine();
        String color;

        while (true) {
            if (colorNum.equals("1")) {
                color = "WHITE";
                break;
            } else if (colorNum.equals("2")) {
                color = "BLACK";
                break;
            } else if (colorNum.equals("q") || colorNum.equals("Q")) {
                return;
            }
            else {
                System.out.println("Please enter 1 (White) or 2 (Black): ");
                colorNum = scanner.nextLine();
            }
        }
        try {
            server.joinGame(new JoinGameRequest(color, gameData.gameID()), auth);
            if (color.equals("WHITE")) {
                enterGameplay(auth, gameData, WHITE);
            } else if (color.equals("BLACK")) {
                enterGameplay(auth, gameData, BLACK);
            } else {
                throw new RuntimeException("Invalid color");
            }

        } catch (Exception ex) {
            handleError(ex, color + " player ");
            playGame(scanner, auth);
        }
    }

    private void observeGame(Scanner scanner, String auth) {
        GameData gameData = joinGame(scanner, auth);
        if (gameData == null) {
            return;
        }
        enterGameplay(auth, gameData, null);
    }

    static boolean isNumeric(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void enterGameplay(String authToken, GameData gameData, ChessGame.TeamColor color) {
        GameplayClient gameplayClient = new GameplayClient(server);
        gameplayClient.run(authToken, gameData, color);
    }

    private GameData joinGame(Scanner scanner, String auth) {
        listGames(scanner, auth);
        System.out.println("Enter the number for the game you would like to join (Enter \"q\" to quit)");
        String gameNum = scanner.nextLine();
        if (gameNum.isEmpty()) {
            playGame(scanner, auth);
        }
        if (gameNum.equals("q")) {
            return null;
        } else if (!isNumeric(gameNum) || Integer.parseInt(gameNum) < 0 || Integer.parseInt(gameNum) > gameMap.size()) {
            System.out.println("Please enter a number 1-" + gameMap.size());
            playGame(scanner, auth);
        }
        return gameMap.get(Integer.parseInt(gameNum));
    }
}
