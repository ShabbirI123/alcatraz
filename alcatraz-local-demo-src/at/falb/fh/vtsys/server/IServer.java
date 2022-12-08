package at.falb.fh.vtsys.server;


import at.falb.fh.vtsys.common.Lobby;
import at.falb.fh.vtsys.common.User;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

public interface IServer extends Remote {
    public List<Lobby> availableLobbies() throws RemoteException;

    public boolean joinLobby(User user, UUID lobbyId) throws RemoteException, AssertionError;

    public Lobby createLobby(User user) throws RemoteException, AssertionError;

    public boolean leaveLobby(User user, UUID lobbyId) throws RemoteException;

    public Lobby startGame(UUID lobbyID) throws RemoteException, NoSuchElementException;

}
