package at.falb.fh.vtsys.client;

import at.falb.fh.vtsys.common.Lobby;
import at.falb.fh.vtsys.common.Move;
import at.falb.fh.vtsys.common.User;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IClient extends Remote {
    public void presentPlayers(Lobby lobby) throws RemoteException;

    public void Move(User user, Move move) throws RemoteException;

    public void startGame() throws RemoteException;

    public void endGame() throws RemoteException;

}

