package client;

import request.LoginRequest;
import request.RegisterRequest;
import response.LoginResponse;
import response.RegisterResponse;
import server.ResponseException;
import server.ServerFacade;

import java.util.Scanner;

import static ui.EscapeSequences.*;

public class PreloginClient {

    private final ServerFacade server;
    private final String serverUrl;
    private final String help =
            """
            Enter a number for what you would like to do!
            
            1: Help
            2: Quit
            3: Login with existing account
            4: Register a new account
            
            """;

    public PreloginClient(String serverUrl) {
        this.serverUrl = serverUrl;
        server = new ServerFacade(serverUrl);
    }

    public void run() {
        System.out.println(BLACK_KING + "Welcome to Chess!!!" + WHITE_KING);

        Scanner scanner = new Scanner(System.in);
        String line = "";

        while (!line.equals("2")) {
            System.out.print(help);
            line = scanner.nextLine();
            eval(line, scanner);
        }
    }
    
    private void eval(String line, Scanner scanner) {
        switch (line) {
            case "1" -> System.out.print(help);
            case "2" -> quit();
            case "3" -> login(scanner);
            case "4" -> register(scanner);
            default -> System.out.print("\nInvalid input - Please Enter a number 1-4\n\n");
        }
    }
    
    private void login(Scanner scanner) {
        String username = getUsername(scanner);
        String password = getPassword(scanner);

        try {
            LoginResponse loginResponse = server.login(new LoginRequest(username, password));
            enterLogin(loginResponse.authToken());
        } catch (ResponseException ex) {
            handleError(ex, "Username");
            login(scanner);
        }
    }
    
    private void quit() {
        System.out.println("Goodbye!");
    }
    
    private void register(Scanner scanner) {
        System.out.println("Thank you for joining Chess!");

        String username = getUsername(scanner);
        String password = getPassword(scanner);
        String email = getEmail(scanner);

        try {
            RegisterResponse registerResponse = server.register(new RegisterRequest(username, password, email));
            enterLogin(registerResponse.authToken());
        } catch (Exception ex) {
            handleError(ex, "Username");
        }
    }

    private String getUsername(Scanner scanner) {
        System.out.println("Please enter a username (or enter q to go back) : ");
        String username = scanner.nextLine();
        if (username.equals("q")) {
            run();
        }
        if (username.isEmpty()) {
            return getUsername(scanner);
        }
        return username;
    }

    private String getPassword(Scanner scanner) {
        System.out.println("Please enter a password: ");
        String password = scanner.nextLine();
        if (password.length() < 5) {
            System.out.println("Password must be at least 5 characters");
            return getPassword(scanner);
        }
        return password;
    }

    private String getEmail(Scanner scanner) {
        System.out.println("Please enter your email: ");
        String email = scanner.nextLine();
        if (email.isEmpty()) {
            return getEmail(scanner);
        }
        return email;
    }

    private void enterLogin(String authToken, String serverUrl) {
        PostloginClient postloginClient = new PostloginClient(server, serverUrl);
        postloginClient.run(authToken);
    }

    public static void handleError(Exception ex, String takenVariable) {
        String message = ex.getMessage();
        if (message == null) {
            System.out.println("MESSAGE NULL");
        }
        switch (message) {
            case "400" -> System.out.println("\nInvalid input\n");
            case "401" -> System.out.println("\nIncorrect username or password\n");
            case "403" -> System.out.println("\nSorry! " + takenVariable + "already taken\n");
            default -> System.out.println("\nUnknown error\n" + message);
        }
    }
}
