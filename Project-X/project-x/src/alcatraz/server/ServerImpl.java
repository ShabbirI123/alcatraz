package alcatraz.server;

import alcatraz.common.Lobby;
import alcatraz.common.User;
import spread.*;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerImpl implements IServer, AdvancedMessageListener {
    static Registry reg;
    private boolean isRunning = true;
    private String serverId;
    private SpreadGroup myGroup;
    private SpreadGroup serverGroup;
    private SpreadGroup currentPrimaryGroup;
    private boolean isPrimary = false;
    SpreadConnection newConnection;
    private final short lobbyMessage = 2;
    private final short primaryMessage = 1;
    private final String spreadGroupName = "spreadGroupName";
    LobbyManager lobbyManager = new LobbyManager();

    /**
     * start method - calls the constructor and checks whether a connection could be established
     * if no connection could be established the server ends after 15 seconds
     * @param args
     */
    public static void main(String[] args) {
        ServerImpl remoteObject = new ServerImpl();
        while (remoteObject.isRunning) {
            try {
                Thread.sleep(15000);
            } catch (InterruptedException ex) {
                Logger.getLogger(ServerImpl.class.getName()).log(Level.SEVERE, "No connection was established", ex);
            }
        }
        Logger.getLogger(ServerImpl.class.getName()).log(Level.SEVERE, "Programm ended");

    }

    /**
     * is called by the main method
     */
    public ServerImpl() {
        this.serverId = UUID.randomUUID().toString();
        newConnection = new SpreadConnection();

        if (newConnection == null){
            isRunning = false;
        }
        try {
            newConnection.connect(InetAddress.getByName("127.0.0.1"), 4803, this.serverId, false, true);
            //add advanced Message listener
            newConnection.add(this);
            this.serverGroup = initSpreadGroup(newConnection, spreadGroupName);
            this.myGroup = newConnection.getPrivateGroup();
        } catch (SpreadException e) {
            Logger.getLogger(ServerImpl.class.getName()).log(Level.SEVERE, "No spread connection could be established", e);
        } catch (UnknownHostException e) {
            Logger.getLogger(ServerImpl.class.getName()).log(Level.SEVERE, "Host was not recognized", e);
        }


    }

    /**
     * multicasts a spread message to the other group members
     * @param connection - the defined spread connection
     * @param groupname - the defined groupname of the spreadconnection
     * @param data - the dataload which is sent to the other members
     * @param messagetype - primaryMessage or lobbyMessage
     */
    private static void sendSpreadMessage(SpreadConnection connection, String groupname, Object data, short messagetype) {
        try {
            SpreadMessage message = new SpreadMessage();
            message.setObject((Serializable) data);
            message.addGroup(groupname);
            message.setReliable();
            message.setType(messagetype);
            connection.multicast(message);
        } catch (SpreadException ex) {
            //TODO
        }
    }

    /**
     * spreadlobbymessage is used by the primary to send the new status from the lobbymanager, who holds
     * all lobbies with all players
     * whenever a new lobby is created or a player joins a lobby, this method is used
     */
    private void sendSpreadLobbyMessage() {
        if (isPrimary) {
            sendSpreadMessage(newConnection, spreadGroupName, lobbyManager.getLobbies(), lobbyMessage);
        }
    }

    /**
     * this method initiates a new spread group.
     * @param newConnection
     * @param spreadGroupName
     * @return
     */
    private SpreadGroup initSpreadGroup(SpreadConnection newConnection, String spreadGroupName) {
        SpreadGroup group = new SpreadGroup();
        try {
            group.join(newConnection, spreadGroupName);
        } catch (SpreadException ex) {
            Logger.getLogger(ServerImpl.class.getName()).log(Level.SEVERE, "Could not init spread connection", ex);
        }
        return group;
    }

    /**
     * two types of spreadMessage (primaryMessage and lobbyMessage)
     * first figure out which type and then decide the logic
     * @param spreadMessage
     */
    @Override
    public void regularMessageReceived(SpreadMessage spreadMessage) {
        if(spreadMessage.getType() == primaryMessage) {
            this.currentPrimaryGroup = spreadMessage.getSender();
            Logger.getLogger(ServerImpl.class.getName()).log(Level.INFO, "primary set: " + this.currentPrimaryGroup.toString());

        }
        if(spreadMessage.getType() == lobbyMessage){

            try {
                lobbyManager.setLobbies((ArrayList<Lobby>) spreadMessage.getObject());
            } catch (SpreadException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * recognize whether a new membership joined or leaved the group
     * furthermore this method decides which server is the primary server
     */
    @Override
    public void membershipMessageReceived(SpreadMessage spreadMessage) {

        definePrimary(spreadMessage);
    }


    /**
     * this method recognizes who is the primary. so if a group member leaves a group
     * a new primary is defined is the primary does not exist anymore
     * @param spreadMessage - contains the information who should be the new primary
     */
    private void definePrimary(SpreadMessage spreadMessage) {
        MembershipInfo info = spreadMessage.getMembershipInfo();
        printMembershipInfo(info);
        //falls ein neues Gruppenmitglied dazu kommt
        if (info.isCausedByJoin()) {
            if (info.getMembers().length == 1) {
                setMePrimary();
            }

            if (this.isPrimary == true) {
                sendSpreadMessage(newConnection, spreadGroupName, "", primaryMessage);
                Logger.getLogger(ServerImpl.class.getName()).log(Level.INFO, "primary message sent");

                sendSpreadMessage(newConnection, spreadGroupName, getLobbyManager().getLobbies(), lobbyMessage);
                Logger.getLogger(ServerImpl.class.getName()).log(Level.INFO, "Lobby message sent");

            }
        }

        //falls ein Gruppenmitglied aufgrund von Netzwerkausfall oder weil es die Gruppe verlässt rausgeht
        if (info.isCausedByDisconnect() || info.isCausedByLeave() || info.isCausedByNetwork()) {
            boolean primaryFound = false;
            Logger.getLogger(ServerImpl.class.getName()).log(Level.WARNING, "Member left Group: " + spreadMessage.getSender().toString());

            for (SpreadGroup member : info.getMembers()) {
                if (member.equals(this.currentPrimaryGroup)) {
                    primaryFound = true;
                    Logger.getLogger(ServerImpl.class.getName()).log(Level.INFO, "Primary still exists");
                    break;
                }
            }

            //elect new primary
            if (primaryFound == false) {
                Logger.getLogger(ServerImpl.class.getName()).log(Level.WARNING, "Primary is gone");

                if (info.getMembers().length == 1) {
                    setMePrimary();
                } else {
                    this.currentPrimaryGroup = info.getMembers()[0];
                    Logger.getLogger(ServerImpl.class.getName()).log(Level.INFO, "New primary: " + info.getMembers()[0].toString());

                    if (this.currentPrimaryGroup.equals(this.myGroup)) {
                        setMePrimary();
                    }
                }
            }
        }


    }

    /**
     * sets THIS server to the new primary with the value true.
     * the other server will have the boolean value isPrimary false
     */
    private void setMePrimary() {
        this.currentPrimaryGroup = this.myGroup;
        this.isPrimary = true;
        Logger.getLogger(ServerImpl.class.getName()).log(Level.WARNING, "New primary: " + myGroup.toString());
        setRMIforPrimary();
    }

    /**
     * sets the RMI-connection from the primary to the clients
     */
    public void setRMIforPrimary() {
        try {
            IServer stub = (IServer) UnicastRemoteObject.exportObject(this, 0);

            reg = LocateRegistry.createRegistry(1099);
            reg = LocateRegistry.getRegistry(1099);

            reg.rebind("Server", stub);
            Logger.getLogger(ServerImpl.class.getName()).log(Level.INFO, "New primary set for RMI");
        } catch (RemoteException ex) {
            Logger.getLogger(ServerImpl.class.getName()).log(Level.SEVERE, null, ex);
            //TODO ich habe es jetzt mal so direkt reingeschrieben. Damit wir hier nicht gleich zuviele Methoden auf einmal haben
            //refactoren auf 2 eine Methode
            try {
                this.serverGroup.leave();
                this.newConnection.disconnect();
            } catch (SpreadException e) {
                e.printStackTrace();
            }

            this.isRunning = false;
        }

    }

    public LobbyManager getLobbyManager() {
        return lobbyManager;
    }

    @Override
    public List<Lobby> availableLobbies() throws RemoteException {
        return lobbyManager.getLobbies();
    }

    @Override
    public boolean joinLobby(User user, UUID lobbyId) throws RemoteException, AssertionError {
        try {
            if (lobbyManager.checkIfUsernameIsUsed(user.getUsername()) || user.getUsername() == null) {
                throw new AssertionError("Username already taken");
            } else {
                if (lobbyManager.getLobby(lobbyId).getUsers().size() >= 4) {
                    throw new RemoteException("Lobby is full");

                } else {
                    lobbyManager.addUser(user, lobbyId);
                    //send to backupserver the lobbies with the updated user
                    sendSpreadLobbyMessage();
                    return true;
                }
            }
        } catch (NoSuchElementException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Lobby createLobby(User user) throws RemoteException, AssertionError {
        if (lobbyManager.checkIfUsernameIsUsed(user.getUsername()) || user.getUsername() == null) {
            throw new AssertionError("Username already taken");
        } else {
            Lobby lobby = lobbyManager.genLobby(user);
            //send to backupserver the lobbies with the updated user
            sendSpreadLobbyMessage();
            return lobby;
        }
    }

    @Override
    public boolean leaveLobby(User user, UUID lobbyId) throws RemoteException {
        try {
            lobbyManager.removeUserFromLobby(user, lobbyId);
            //send to backupserver the lobbies with the updated user
            sendSpreadLobbyMessage();
            return true;
        } catch (NoSuchElementException e) {
            e.printStackTrace();
            return false;
        }

    }

    @Override
    public Lobby startGame(UUID lobbyID) throws RemoteException, NoSuchElementException {
        try {
            Logger.getLogger(ServerImpl.class.getName()).log(Level.INFO, "Game started with lobbyID: " + lobbyID);

            int userCountInLobby = lobbyManager.getLobby(lobbyID).getUsers().size();
            if (userCountInLobby < 1  || userCountInLobby > 4) {
                Logger.getLogger(ServerImpl.class.getName()).log(Level.SEVERE, "Wrong lobby size: " + userCountInLobby);
                throw new RemoteException("wrong Lobby size =" + userCountInLobby);
            } else {

                Lobby lobby = lobbyManager.changeLobbyStatus(lobbyID);
                //send to backupserver the lobbies with the updated user
                sendSpreadLobbyMessage();
                lobbyManager.deleteLobby(lobbyID);
                return lobby;
            }
        } catch (Exception exception) {
            Logger.getLogger(ServerImpl.class.getName()).log(Level.SEVERE, "The game could not be started", exception);
            throw new RemoteException("error");
        }

    }

    // Print this membership data.  Does so in a generic way so identical
    // function is used in recThread and User.
    private void printMembershipInfo(MembershipInfo info) {
        SpreadGroup group = info.getGroup();
        if (info.isRegularMembership()) {
            SpreadGroup members[] = info.getMembers();
            MembershipInfo.VirtualSynchronySet virtual_synchrony_sets[] = info.getVirtualSynchronySets();
            MembershipInfo.VirtualSynchronySet my_virtual_synchrony_set = info.getMyVirtualSynchronySet();

            Logger.getLogger(ServerImpl.class.getName()).log(Level.INFO, "REGULAR membership for group " + group +
                    " with " + members.length + " members:");
            for (int i = 0; i < members.length; ++i) {
                Logger.getLogger(ServerImpl.class.getName()).log(Level.INFO, "\t\t" + members[i]);
            }
            Logger.getLogger(ServerImpl.class.getName()).log(Level.INFO, "Group ID is " + info.getGroupID());

            if (info.isCausedByJoin()) {
                Logger.getLogger(ServerImpl.class.getName()).log(Level.INFO, "the JOIN of " + info.getJoined());
            } else if (info.isCausedByLeave()) {
                Logger.getLogger(ServerImpl.class.getName()).log(Level.INFO, "the LEAVE of " + info.getLeft());
            } else if (info.isCausedByDisconnect()) {
                Logger.getLogger(ServerImpl.class.getName()).log(Level.INFO, "the DISCONNECT of " + info.getDisconnected());
            } else if (info.isCausedByNetwork()) {
                Logger.getLogger(ServerImpl.class.getName()).log(Level.INFO, "NETWORK change");
                for (int i = 0; i < virtual_synchrony_sets.length; ++i) {
                    MembershipInfo.VirtualSynchronySet set = virtual_synchrony_sets[i];
                    SpreadGroup setMembers[] = set.getMembers();
                    if (set == my_virtual_synchrony_set) {
                        Logger.getLogger(ServerImpl.class.getName()).log(Level.INFO, "(LOCAL) ");
                    } else {
                        Logger.getLogger(ServerImpl.class.getName()).log(Level.INFO, "(OTHER) ");
                    }
                    Logger.getLogger(ServerImpl.class.getName()).log(Level.INFO, "Virtual Synchrony Set " + i + " has " +
                            set.getSize() + " members:");

                    for (int j = 0; j < set.getSize(); ++j) {
                        System.out.println("\t\t\t" + setMembers[j]);
                    }
                }
            }
        } else if (info.isTransition()) {
            Logger.getLogger(ServerImpl.class.getName()).log(Level.INFO, "TRANSITIONAL membership for group " + group);
        } else if (info.isSelfLeave()) {
            Logger.getLogger(ServerImpl.class.getName()).log(Level.INFO, "SELF-LEAVE message for group " + group);

        }
    }

}
