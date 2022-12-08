package alcatraz.common;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.UUID;

public class User implements Serializable {

    private UUID userId;

    private String username;

    private int rmiPort;

    private String ipAddress;

    public User() {

    }

    public void setIpAddress() throws UnknownHostException {
        ipAddress=InetAddress.getLocalHost().getHostAddress();
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int getRmiPort() {
        return rmiPort;
    }

    public void setRmiPort(int rmiPort) {
        this.rmiPort = rmiPort;
    }

    public User(String username) {
        this.username = username;
        this.userId = UUID.randomUUID();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return rmiPort == user.rmiPort && Objects.equals(userId, user.userId) && Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, username, rmiPort);
    }
}
