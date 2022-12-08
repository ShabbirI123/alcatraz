package alcatraz.client;

public class MainClient {

    private ClientImpl client = new ClientImpl();
    private UserInterfaceLobbies userInterfaceLobbies;

    public void init() {
        userInterfaceLobbies = new UserInterfaceLobbies(client);

        client.connectToServer(0);

        userInterfaceLobbies.generateWindow();
        client.setUserInterfaceLobbies(userInterfaceLobbies);
    }

    public static void main(String[] args) {
        MainClient controller = new MainClient();
        controller.init();

    }

}
