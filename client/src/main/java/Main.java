import client.PreloginClient;

public class Main {
    public static void main(String[] args) {
        PreloginClient preloginClient = new PreloginClient("8080");
        preloginClient.run();
    }
}