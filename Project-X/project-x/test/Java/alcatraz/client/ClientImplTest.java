package alcatraz.client;

import alcatraz.common.Lobby;
import alcatraz.server.ServerImpl;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;
class ClientImplTest {



         @Test
         public void testRMIClientToClientPresentPlayers(){
             try {
                 Lobby lobby=new Lobby();

                 ClientImpl client1=new ClientImpl();
                 ClientImpl client2=new ClientImpl();

                 client1.init("client1");
                 client2.init("client2");

                 lobby.addPlayer(client1.getThisUser());
                 lobby.addPlayer(client2.getThisUser());

                 client1.setLobby(lobby);
                 client2.setLobby(lobby);

                 client1.startClientRMI();
                 client2.startClientRMI();

                 client1.connectToTheClients();
                 client2.connectToTheClients();

                 client1.sendUsersToOtherClients();
                 client2.sendUsersToOtherClients();

             }catch (Exception e){
                 e.printStackTrace();
                 fail();
             }
         }


}