//package alcatraz.server;
//
//import alcatraz.client.ClientImpl;
//import alcatraz.common.Lobby;
//import alcatraz.common.User;
//import org.junit.jupiter.api.Test;
//
//
//import static org.junit.jupiter.api.Assertions.*;
//class ServerImplTest {
//    @Test
//    public void testRMIServerConnection(){
//        try {
//            ServerImpl server=new ServerImpl();
//            ClientImpl client= new ClientImpl();
//            User user=new User("test1");
//            client.setThisUser(user);
//
//            server.getLobbyManager().genLobby(user);
//            server.setRMIforPrimary();
//
//            client.connectToServer(0);
//            client.serverGetLobbies();
//
//        }catch (Exception e){
//            e.printStackTrace();
//            fail();
//        }
//    }
//
//
//
//
//    @Test
//    public void testRMIServerCreateLobby(){
//        try {
//            int expectedResult=1;
//            int actualResult;
//
//            ServerImpl server=new ServerImpl();
//            ClientImpl client= new ClientImpl();
//            User user=new User("test1");
//            client.setThisUser(user);
//
//
//            server.setRMIforPrimary();
//
//            client.connectToServer(0);
//            client.serverCreateLobby();
//
//            actualResult=server.lobbyManager.getLobbies().size();
//
//
//            assertEquals(expectedResult,actualResult);
//
//        }catch (Exception e){
//            e.printStackTrace();
//            fail();
//        }
//    }
//
//
//
//
//    @Test
//    public void testRMIServerJoinLobby(){
//        try {
//            int expectedResult=1;
//            int actualResult;
//
//            ServerImpl server=new ServerImpl();
//            ClientImpl client= new ClientImpl();
//
//            client.init("test1");
//            User user=client.getThisUser();
//            client.setThisUser(user);
//            Lobby lobby =new Lobby();
//            server.lobbyManager.getLobbies().add(lobby);
//
//
//            server.setRMIforPrimary();
//
//            client.connectToServer(0);
//          //  client.serverCreateLobby();
//
//            client.serverJoinLobby(lobby.getLobbyId());
//
//            actualResult=lobby.getUsers().size();
//
//
//            assertEquals(expectedResult,actualResult);
//
//        }catch (Exception e){
//            e.printStackTrace();
//            fail();
//        }
//    }
//
//    @Test
//    public void testRMIServerLeaveLobby(){
//        try {
//            int expectedResult=0;
//            int actualResult;
//
//            ServerImpl server=new ServerImpl();
//            ClientImpl client= new ClientImpl();
//
//            client.init("test1");
//            User user=client.getThisUser();
//            Lobby lobby =new Lobby();
//            lobby.addPlayer(user);
//            server.lobbyManager.getLobbies().add(lobby);
//
//
//            server.setRMIforPrimary();
//
//            client.connectToServer(0);
//           // client.serverCreateLobby();
//
//            client.serverLeaveLobby(lobby.getLobbyId());
//
//
//
//            actualResult=lobby.getUsers().size();
//
//
//            assertEquals(expectedResult,actualResult);
//
//        }catch (Exception e){
//            e.printStackTrace();
//            fail();
//        }
//    }
//
//
//
//}