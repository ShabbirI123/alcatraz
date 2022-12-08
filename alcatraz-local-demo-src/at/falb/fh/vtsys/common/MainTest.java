package at.falb.fh.vtsys.common;

import at.falb.fh.vtsys.client.MainClient;
import at.falb.fh.vtsys.server.ServerImpl;

public class MainTest {
    public static void main(String[] args) {
        ServerImpl server = new ServerImpl();
        server.setRMIforPrimary();

        MainClient controller1 = new MainClient();
        controller1.init();

        MainClient controller2 = new MainClient();
        controller2.init();

    }


}
