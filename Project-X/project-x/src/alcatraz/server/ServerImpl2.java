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

public class ServerImpl2 implements IServer, AdvancedMessageListener {
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
    private final short playerMessage = 3;
    LobbyManager lobbyManager = new LobbyManager();


    public static void main(String[] args) {
        ServerImpl2 remoteObject = new ServerImpl2();

        while (remoteObject.isRunning) {
            try {
                Thread.sleep(17000);
            } catch (InterruptedException ex) {
                //TODO or not, whatever
            }
        }
        System.out.println("Programm ended");


    }

    public ServerImpl2() {
        this.serverId = UUID.randomUUID().toString();
        newConnection = new SpreadConnection();
        try {
            newConnection.connect(InetAddress.getByName("127.0.0.1"), 4803, this.serverId, false, true);
            //add advanced Message listener
            newConnection.add(this);
            this.serverGroup = initSpreadGroup(newConnection, "spreadGroupName");
            //TODO verstehe nicht was hier genau passiert. Wozu braucht man eine private Group?
            this.myGroup = newConnection.getPrivateGroup();
            // sendSpreadMessage(newConnection,"spreadGroupName","test", (short) 2);
        } catch (SpreadException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }


    }

    private static void sendSpreadMessage(SpreadConnection connection, String groupname, Object data, short messagetype) {
        try {
            SpreadMessage message = new SpreadMessage();
            message.setObject((Serializable) data);
            message.addGroup(groupname);
            //TODO: laut Doku wird setReliable() per default aufgerufen. Es sollte also hier nicht notwendig sein es wieder zu setzen
            message.setReliable();
            message.setType(messagetype);
            connection.multicast(message);
        } catch (SpreadException ex) {
            //TODO
        }
    }

    private void sendSpreadLobbyMessage() {
        if (isPrimary) {
            sendSpreadMessage(newConnection, "spreadGroupName", lobbyManager.getLobbies(), lobbyMessage);
        }
    }


    private SpreadGroup initSpreadGroup(SpreadConnection newConnection, String spreadGroupName) {
        SpreadGroup group = new SpreadGroup();
        try {
            group.join(newConnection, spreadGroupName);
        } catch (SpreadException ex) {
            System.err.println("Spread Exception: " + ex.getMessage() + Arrays.toString(ex.getStackTrace()));
        }
        return group;
    }

    @Override
    public void regularMessageReceived(SpreadMessage spreadMessage) {
        if (spreadMessage.getType() == primaryMessage) {
            this.currentPrimaryGroup = spreadMessage.getSender();
            Logger.getLogger(ServerImpl2.class.getName()).log(Level.INFO, "primary set: " + this.currentPrimaryGroup.toString());

        }

        if (spreadMessage.getType() == lobbyMessage) {
            try {
                lobbyManager.setLobbies((ArrayList<Lobby>) spreadMessage.getObject());
                Logger.getLogger(ServerImpl2.class.getName()).log(Level.INFO, "Lobbies updated");
            } catch (SpreadException ex) {
                //TODO catch me if you can
            }
        }
        //TODO Player fehlen noch bei uns
            /*
                if(msg.getType() == playerMessage)
                {
                    try {
                        this.AllPlayers = (ArrayList<User>) message.getObject();
                        System.out.println("User list updated");

                    } catch (SpreadException ex) {
                        //TODO something useful
                    }
                }
                */

        DisplayMessage(spreadMessage);
    }

    /**
     * hier wird festgestellt ob ein neues Gruppenmitglied dazugekommen ist
     * außerdem wird hier festgestellt ob der derzeitige Server der Primary ist
     * Primary falls er der erste ist welche in der Gruppe ist
     */
    @Override
    public void membershipMessageReceived(SpreadMessage spreadMessage) {
        definePrimary(spreadMessage);
        DisplayMessage(spreadMessage);
    }

    private void DisplayMessage(SpreadMessage msg) {
        try {
            if (msg.isRegular()) {
                //TODO nur für Testzwecke derzeit drinnen gelassen
                //für Fehleranalyse kann man es wieder auskommentieren
               /* System.out.print("Received a ");
                if(msg.isUnreliable())
                    System.out.print("UNRELIABLE");
                else if(msg.isReliable())
                    System.out.print("RELIABLE");
                else if(msg.isFifo())
                    System.out.print("FIFO");
                else if(msg.isCausal())
                    System.out.print("CAUSAL");
                else if(msg.isAgreed())
                    System.out.print("AGREED");
                else if(msg.isSafe())
                    System.out.print("SAFE");
                System.out.println(" message.");

                System.out.println("Sent by  " + msg.getSender() + ".");

                System.out.println("Type is " + msg.getType() + ".");

                if(msg.getEndianMismatch() == true)
                    System.out.println("There is an endian mismatch.");
                else
                    System.out.println("There is no endian mismatch.");

                SpreadGroup groups[] = msg.getGroups();
                System.out.println("To " + groups.length + " groups.");
                byte data[] = msg.getData();
                System.out.println("The data is " + data.length + " bytes.");
                System.out.println("The message is: " + new String(data));
                  
                */
            } else if (msg.isReject()) {
                // Received a Reject message
                System.out.print("Received a ");
                if (msg.isUnreliable())
                    System.out.print("UNRELIABLE");
                else if (msg.isReliable())
                    System.out.print("RELIABLE");
                else if (msg.isFifo())
                    System.out.print("FIFO");
                else if (msg.isCausal())
                    System.out.print("CAUSAL");
                else if (msg.isAgreed())
                    System.out.print("AGREED");
                else if (msg.isSafe())
                    System.out.print("SAFE");
                System.out.println(" REJECTED message.");

                System.out.println("Sent by  " + msg.getSender() + ".");

                System.out.println("Type is " + msg.getType() + ".");

                if (msg.getEndianMismatch() == true)
                    System.out.println("There is an endian mismatch.");
                else
                    System.out.println("There is no endian mismatch.");

                SpreadGroup groups[] = msg.getGroups();
                System.out.println("To " + groups.length + " groups.");

                byte data[] = msg.getData();
                System.out.println("The data is " + data.length + " bytes.");

                System.out.println("The message is: " + new String(data));
            } else {
                System.out.println("Message is of unknown type: " + msg.getServiceType());
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    //es funktioniert, dass er erkannt wird wer der Primary ist, allerdings erkennt er nicht
    //falls ein Server ausfällt wer der neue Primary ist.
    private void definePrimary(SpreadMessage spreadMessage) {
        MembershipInfo info = spreadMessage.getMembershipInfo();
        printMembershipInfo(info);
        //falls ein neues Gruppenmitglied dazu kommt
        if (info.isCausedByJoin()) {
            if (info.getMembers().length == 1) {
                setMePrimary();
            }

            if (this.isPrimary == true) {
                sendSpreadMessage(newConnection, "spreadGroupName", "", primaryMessage);
                System.out.println("primary message sent");
                sendSpreadMessage(newConnection, "spreadGroupName", getLobbyManager().getLobbies(), lobbyMessage);
                System.out.println("Lobby message sent");
                //TODO Player fehlen bei uns noch
                /*
                sendSpreadMessage(newConnection, "spreadGroupName", AllPlayers, playerMessage );
                System.out.println("Player message sent");

                 */
            }
        }

        //falls ein Gruppenmitglied aufgrund von Netzwerkausfall oder weil es die Gruppe verlässt rausgeht
        if (info.isCausedByDisconnect() || info.isCausedByLeave() || info.isCausedByNetwork()) {
            boolean primaryFound = false;
            //auskommentiert weil wir message hier nicht mitgeben
            System.out.println("Member left Group: " + spreadMessage.getSender().toString());

            for (SpreadGroup member : info.getMembers()) {
                if (member.equals(this.currentPrimaryGroup)) {
                    primaryFound = true;
                    System.out.println("Primary still exists");
                    break;
                }
            }

            //elect new primary
            if (primaryFound == false) {
                Logger.getLogger(ServerImpl2.class.getName()).log(Level.WARNING, "Primary is gone");

                if (info.getMembers().length == 1) {
                    setMePrimary();
                } else {
                    this.currentPrimaryGroup = info.getMembers()[0];
                    Logger.getLogger(ServerImpl2.class.getName()).log(Level.INFO, "New primary: " + info.getMembers()[0].toString());

                    if (this.currentPrimaryGroup.equals(this.myGroup)) {
                        setMePrimary();
                    }
                }
            }
        }


    }

    private void setMePrimary() {
        this.currentPrimaryGroup = this.myGroup;
        this.isPrimary = true;
        System.out.println("New primary: " + myGroup.toString());
        Logger.getLogger(ServerImpl2.class.getName()).log(Level.INFO, "New primary: " + myGroup.toString());

        setRMIforPrimary();
    }

    private void setRMIforPrimary() {
        try {
            IServer stub = (IServer) UnicastRemoteObject.exportObject(this, 0);

            reg = LocateRegistry.createRegistry(1099);
            reg = LocateRegistry.getRegistry(1099);

            reg.rebind("Server", stub);
            Logger.getLogger(ServerImpl2.class.getName()).log(Level.INFO, "New primary set for RMI");
        } catch (RemoteException ex) {
            Logger.getLogger(ServerImpl2.class.getName()).log(Level.SEVERE, null, ex);

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
            System.out.println(lobbyID);
            Logger.getLogger(ServerImpl2.class.getName()).log(Level.INFO, "startGame gestartet mit lobbyID: " + lobbyID);

            int userCountInLobby = lobbyManager.getLobby(lobbyID).getUsers().size();
            if (userCountInLobby < 2 || userCountInLobby > 4) {
                System.out.println("lobby s=" + userCountInLobby);
                Logger.getLogger(ServerImpl2.class.getName()).log(Level.SEVERE, "Useranzahl ist nicht für startGame in Ordnung, Anzahl +" + userCountInLobby);
                throw new RemoteException("wrong Lobby size =" + userCountInLobby);
            } else {

                Lobby lobby = lobbyManager.changeLobbyStatus(lobbyID);
                //send to backupserver the lobbies with the updated user
                sendSpreadLobbyMessage();
                return lobby;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
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

            System.out.println("REGULAR membership for group " + group +
                    " with " + members.length + " members:");
            for (int i = 0; i < members.length; ++i) {
                System.out.println("\t\t" + members[i]);
            }
            System.out.println("Group ID is " + info.getGroupID());

            System.out.print("\tDue to ");
            if (info.isCausedByJoin()) {
                System.out.println("the JOIN of " + info.getJoined());
            } else if (info.isCausedByLeave()) {
                System.out.println("the LEAVE of " + info.getLeft());
            } else if (info.isCausedByDisconnect()) {
                System.out.println("the DISCONNECT of " + info.getDisconnected());
            } else if (info.isCausedByNetwork()) {
                System.out.println("NETWORK change");
                for (int i = 0; i < virtual_synchrony_sets.length; ++i) {
                    MembershipInfo.VirtualSynchronySet set = virtual_synchrony_sets[i];
                    SpreadGroup setMembers[] = set.getMembers();
                    System.out.print("\t\t");
                    if (set == my_virtual_synchrony_set) {
                        System.out.print("(LOCAL) ");
                    } else {
                        System.out.print("(OTHER) ");
                    }
                    System.out.println("Virtual Synchrony Set " + i + " has " +
                            set.getSize() + " members:");
                    for (int j = 0; j < set.getSize(); ++j) {
                        System.out.println("\t\t\t" + setMembers[j]);
                    }
                }
            }
        } else if (info.isTransition()) {
            System.out.println("TRANSITIONAL membership for group " + group);
        } else if (info.isSelfLeave()) {
            System.out.println("SELF-LEAVE message for group " + group);
        }
    }

}
