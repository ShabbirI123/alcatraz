package alcatraz.common;

import alcatraz.client.MainClient;

public class MainServer {
    public static void main(String[] args) {

        MainClient controller1 = new MainClient();
        controller1.init();

        MainClient controller2 = new MainClient();
        controller2.init();

    }
}
