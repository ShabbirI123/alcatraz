package alcatraz.client;

import alcatraz.common.Lobby;
import alcatraz.common.Move;
import alcatraz.common.User;
import alcatraz.server.IServer;

import java.net.UnknownHostException;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientImpl implements IClient {

    static Registry reg;

    static IServer stub;

    static int numberOfClients = 0;

    private ArrayList<IClient> clientStubs = new ArrayList<>();
    private List<String> serverIPs = Arrays.asList("127.0.0.1", "10.0.0.16","10.0.0.45", "10.0.0.42");
    //User of this client
    private User thisUser = new User();

    //Lobby of this client
    private Lobby lobby;

    //Game
    private AlcatrazImpl alcatrazImpl = new AlcatrazImpl(this);

    private UserInterfaceLobbies userInterfaceLobbies;

    private boolean rmiStarted = false;

    private Logger logger =Logger.getLogger(ClientImpl.class.getName());



    public boolean isRmiStarted() {
        return rmiStarted;
    }

    public UserInterfaceLobbies getUserInterfaceLobbies() {
        return userInterfaceLobbies;
    }

    public void setUserInterfaceLobbies(UserInterfaceLobbies userInterfaceLobbies) {
        this.userInterfaceLobbies = userInterfaceLobbies;

    }

    public User getThisUser() {
        return thisUser;
    }

    public void setThisUser(User thisUser) {
        this.thisUser = thisUser;
    }

    public void init(String username) {
        thisUser.setUserId(UUID.randomUUID());
        thisUser.setUsername(username);
    }


    public Lobby getLobby() {
        return lobby;
    }

    public void setLobby(Lobby lobby) {
        this.lobby = lobby;
    }

    public ClientImpl() {
        this.thisUser.setRmiPort(1100 + numberOfClients);

        numberOfClients++;

        try {
            thisUser.setIpAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
      //  System.setProperty ("sun.rmi.transport.tcp.responseTimeout", "10000");
    }

    /**
     connect to the primary Lobby Server
     */
    public void connectToServer(Integer counter) {
        try {
            Registry reg = LocateRegistry.getRegistry(serverIPs.get(counter));
            stub = (IServer) reg.lookup("Server");
        } catch (Exception e) {
            connectToServer(counter+1);
            System.out.println(e);
        }
    }

    //!!! Client to server RMI function begin

    /**
     join a  lobby
     send join lobby to the primary Lobby Server
     @param lobbyID the id of the joined lobby
     */
    public void serverJoinLobby(UUID lobbyID) throws RemoteException {
        try {
            stub.joinLobby(thisUser, lobbyID);
        } catch (ConnectException ex) {
            connectToServer(0);
            stub.joinLobby(thisUser, lobbyID);
        }
    }

    /**
     create a  lobby
     send create lobby to the primary Lobby Server
     */
    public Lobby serverCreateLobby() throws RemoteException {
        try {
            Lobby lob = stub.createLobby(thisUser);
            this.lobby = lob;
            return lob;
        } catch (ConnectException ex) {
            connectToServer(0);
            Lobby lob = stub.createLobby(thisUser);
            this.lobby = lob;
            return lob;
        }
    }

    /**
     leave the joined lobby
     send leave lobby to the primary Lobby Server
     */
    public void serverLeaveLobby(UUID lobbyID) throws RemoteException {
        try {
            stub.leaveLobby(thisUser, lobbyID);
        } catch (ConnectException ex) {
            connectToServer(0);
            stub.leaveLobby(thisUser, lobbyID);
        }
    }

    /**
     get all Lobbys from the primary Lobby Server
     */
    public List<Lobby> serverGetLobbies() throws RemoteException {
        try {
            List<Lobby> result = stub.availableLobbies();
            System.out.println(result);
            return result;

        } catch (ConnectException ex) {
            connectToServer(0);
            List<Lobby> result = stub.availableLobbies();
            System.out.println(result);
            return result;

        }

    }

    /**
     send start Game to the primary Lobby Server
     */
    public Lobby serverStartGame() throws RemoteException {
        try {
            return stub.startGame(lobby.getLobbyId());
        } catch (ConnectException ex) {
            connectToServer(0);
            return stub.startGame(lobby.getLobbyId());
        }
    }


    //!!! Client to server RMI function end

    /**
     send the Lobby of this client to the connected clients, to ensure that all clients have the same information
     */
    public void sendUsersToOtherClients() throws RemoteException {
        for (IClient stub : this.clientStubs) {
            try {
                stub.presentPlayers(this.lobby);
            } catch (Exception e) {
                e.printStackTrace();

            }

        }
    }

    /**
     send the start game to the connected clients
     */
    public void sendStartToOtherClients() throws RemoteException {
        for (IClient stub : this.clientStubs) {
            try {
                stub.startGame();
            } catch (Exception e) {
                e.printStackTrace();

            }
        }
    }


    /**
     send a  game move to the connected clients
     @param move The move operation from the game
     @param count
     */
    public void sendMoveToOtherClients(Move move, int count) throws RemoteException {
        System.out.println();
        System.out.println("send moveRMU" + move.toString());
        System.out.println("anz Clints" + clientStubs.size());
        for (IClient stub : this.clientStubs) {
            try {
                System.out.println("send to:");
                System.out.println(stub.toString());
                stub.Move(thisUser, move);
            } catch (Exception e) {
                try{
                    Thread.sleep(10000);
                    sendMoveToOtherClients(move, count+1);
                }catch(InterruptedException ex){
                    ex.printStackTrace();
                }
                e.printStackTrace();

              //  handelTimeOut(stub);
            }

        }
    }


    /**
     send the end game to the connected clients
     */
    private void sendEndGameToOtherClients(){
        for (IClient stub:clientStubs){
            try {
                stub.endGame();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     Starts the RMI of this client
     */
    public void startClientRMI() throws RemoteException {
        IClient clientStub = (IClient) UnicastRemoteObject.exportObject(this, 0);
        reg = LocateRegistry.createRegistry(thisUser.getRmiPort());
        reg = LocateRegistry.getRegistry(thisUser.getRmiPort());
       // System.out.println("user/+ " + this.thisUser.getUsername() + "con= " + "client/" + this.thisUser.getUsername());
        String connect = "client/" + this.thisUser.getUsername();
        reg.rebind(connect, clientStub);
        System.out.println();

        genInfLog(" client:" +thisUser.getUsername()+ "has started RMI with name= "+connect);
        rmiStarted = true;
    }


    /**
     connect this client to the other clients
     get the other client(and there ip&port) from the lobby
     */
    public void connectToTheClients() throws RemoteException, NotBoundException {
        for (User user : this.lobby.getUsers()) {
            if (!user.getUsername().equals(thisUser.getUsername())) {
                Registry reg;
                String ipAddress = user.getIpAddress().toString();
                if (ipAddress != null) {

                    reg = LocateRegistry.getRegistry(ipAddress, user.getRmiPort());
                } else {
                    System.out.println();
                    genInfLog("No Ip Address from Target");
                    reg = LocateRegistry.getRegistry(user.getRmiPort());
                }

                IClient clientStub = (IClient) reg.lookup("client/" + user.getUsername());
                clientStubs.add(clientStub);

                genInfLog(" Verbinde:" +thisUser.getUsername() +" zu:"+user.getUsername()+"\n"+" Ip address: "+ipAddress+" Port:"+user.getRmiPort());
            }
        }
    }



    private void genInfLog(String message){
        logger.log(Level.OFF,"\n");
        logger.log(Level.INFO, message );

    }

    /**
    Starts the Game
    */
    public void startAlcatrazGame() {
        alcatrazImpl.init();
    }


    //??? Client to Client RMI function begin ???

    @Override
    public void presentPlayers(Lobby lobby) throws RemoteException {
        System.out.println("reciver =" + thisUser.getUsername() + " " + lobby);
        if (!this.lobby.equals(lobby)) {
            System.out.println("reciver =" + thisUser.getUsername() + " changed lobby");
            this.lobby = lobby;
        }
        try {
            if (!rmiStarted) {
                startClientRMI();
            }
            connectToTheClients();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void Move(User user, Move move) throws RemoteException {
        genInfLog("recived Move =\" + move.toString() ");
        this.alcatrazImpl.makeRMIMove(move);
    }

    @Override
    public void startGame() throws RemoteException {
        genInfLog("receiver =" + thisUser.getUsername() + "Start game!!! ");
        userInterfaceLobbies.closeWindow();
        startAlcatrazGame();
    }

    @Override
    public void endGame() throws RemoteException {
        alcatrazImpl.end();
        userInterfaceLobbies.showWindow();

    }


    //??? Client to Client RMI function end ???

}
