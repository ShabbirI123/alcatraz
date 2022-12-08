package alcatraz.common;

import java.io.Serializable;
import java.util.*;

public class Lobby implements Serializable {
    private UUID lobbyId = UUID.randomUUID();

    private List<User> users = new ArrayList<>();

    private boolean isGameRunning = false;


    public Lobby() {

    }

    public boolean isGameRunning() {
        return isGameRunning;
    }

    public void setGameRunning(boolean gameRunning) {
        isGameRunning = gameRunning;
    }

    public void addPlayer(User user) {
        users.add(user);
    }

    public void removeUser(User user) {
        users.remove(user);
    }

    public UUID getLobbyId() {
        return lobbyId;
    }

    public List<User> getUsers() {
        return users;
    }

    public int getUserPosition(User user) throws NoSuchElementException {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).equals(user)) {
                return i;
            }
        }
        throw new NoSuchElementException("No user " + user.toString());
    }

    @Override
    public String toString() {
        return "Lobby{" +
                "lobbyId=" + lobbyId +
                ", users=" + users +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Lobby lobby = (Lobby) o;
        return isGameRunning == lobby.isGameRunning && Objects.equals(lobbyId, lobby.lobbyId) && Objects.equals(users, lobby.users);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lobbyId, users, isGameRunning);
    }
}
