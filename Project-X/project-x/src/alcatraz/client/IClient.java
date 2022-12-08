package alcatraz.client;

import alcatraz.common.Lobby;
import alcatraz.common.Move;
import alcatraz.common.User;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IClient extends Remote {
    public void presentPlayers(Lobby lobby) throws RemoteException;

    public void Move(User user, Move move) throws RemoteException;

    public void startGame() throws RemoteException;

    public void endGame() throws RemoteException;

}
