package alcatraz.server;

import alcatraz.common.Lobby;
import alcatraz.common.User;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.UUID;

public class LobbyManager {
    private ArrayList<Lobby> lobbies = new ArrayList<>();

    public ArrayList<Lobby> getLobbies() {
        return lobbies;
    }

    public void setLobbies(ArrayList<Lobby> lobbies) {
        this.lobbies = lobbies;
    }

    public Lobby genLobby(User user) {
        Lobby lobby = new Lobby();
        lobby.addPlayer(user);
        lobbies.add(lobby);
        return lobby;
    }

    public void addUser(User user, UUID uuid) throws NoSuchElementException {
        for (Lobby lobby : lobbies) {
            if (lobby.getLobbyId().equals(uuid)) {
                lobby.addPlayer(user);
                return;
            }
        }
        throw new NoSuchElementException("No Lobby with the id=" + uuid);
    }

    public void removeUserFromLobby(User user, UUID uuid) throws NoSuchElementException, NoSuchElementException {
        for (Lobby lobby : lobbies) {
            if (lobby.getLobbyId().equals(uuid)) {
                lobby.removeUser(user);
                return;
            }
        }
        throw new NoSuchElementException("No Lobby with the id=" + uuid);
    }

    public void deleteLobby(UUID uuid) throws NoSuchElementException {
        for (Lobby lobby : lobbies) {
            if (lobby.getLobbyId().equals(uuid)) {
                lobbies.remove(lobby);
                return;
            }
        }
        throw new NoSuchElementException("No Lobby with the id=" + uuid);
    }

    public Lobby getLobby(UUID uuid) throws NoSuchElementException {
        for (Lobby lobby : lobbies) {
            if (lobby.getLobbyId().equals(uuid)) {
                return lobby;
            }
        }
        throw new NoSuchElementException("No Lobby with the id=" + uuid);
    }

    public Lobby changeLobbyStatus(UUID uuid) throws NoSuchElementException {
        for (Lobby lobby : lobbies) {
            if (lobby.getLobbyId().equals(uuid)) {
                lobby.setGameRunning(true);
                return lobby;
            }
        }
        throw new NoSuchElementException("No Lobby with the id=" + uuid);
    }

    public boolean checkIfUsernameIsUsed(String username) {
        for (Lobby lobby : lobbies) {
            for (User user : lobby.getUsers()) {
                if (user.getUsername().equals(username)) {
                    return true;
                }
            }

        }
        return false;
    }


}
