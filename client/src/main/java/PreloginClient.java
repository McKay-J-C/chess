import request.LoginRequest;
import request.RegisterRequest;
import server.ResponseException;
import server.ServerFacade;

import java.util.Scanner;

import static ui.EscapeSequences.*;

public class PreloginClient {

    private final ServerFacade server;
    private String help =
            """
            Enter a number for what you would like to do!
            
            1: Help
            2: Quit
            3: Login with existing account
            4: Register a new account
            
            """;

    public PreloginClient(String serverUrl) {
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
        quit();
    }
    
    private void eval(String line, Scanner scanner) {
        switch (line) {
            case "1" -> System.out.print(help);
            case "3" -> login(scanner);
            case "4" -> register(scanner);
            default -> System.out.print("Please Enter a number 1-4\n" + help);
        }
    }
    
    private void login(Scanner scanner) {
        String username = getUsername(scanner);
        String password = getPassword(scanner);

//        server.login(new LoginRequest(username, password));
        try {
            server.login(new LoginRequest(username, password));
        } catch (ResponseException ex) {

            if (ex.getMessage().equals("Error: incorrect password")) {
                System.out.println("Incorrect password");
            }
            login(scanner);
        }
        enterLogin();
    }
    
    private void quit() {
        System.out.println("Goodbye!");
    }
    
    private void register(Scanner scanner) {
        System.out.println("Thank you for joining Chess!");

        String username = getUsername(scanner);
        String password = getPassword(scanner);
        String email = getEmail(scanner);

        server.register(new RegisterRequest(username, password, email));
        enterLogin();
    }

    private String getUsername(Scanner scanner) {
        System.out.println("Please enter a username: ");
        String username = scanner.nextLine();
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

    private void enterLogin() {
        PostloginClient postloginClient = new PostloginClient(server);
        postloginClient.run();
    }

}
