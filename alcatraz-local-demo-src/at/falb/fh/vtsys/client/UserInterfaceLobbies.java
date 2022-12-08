package at.falb.fh.vtsys.client;

import at.falb.fh.vtsys.common.Lobby;
import at.falb.fh.vtsys.common.User;

import javax.swing.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;

public class UserInterfaceLobbies {

    private ClientImpl client;

    private JPanel root;
    private JPanel usernamePanel;
    private JTextField userNameTextField;
    private JLabel userNameInfoLabel;
    private JScrollPane lobbiesScrollPane;
    private JButton createLobbyButton;
    private JButton startGamebutton;
    private JPanel lobbyPanel;
    private JRadioButton radioButtonReload;


    private JFrame frame;

    public ClientImpl getClient() {
        return client;
    }

    public void setClient(ClientImpl client) {
        this.client = client;
    }

    public void init() {
        startGamebutton.setVisible(false);
        createLobbyButton.addActionListener(e -> {
            createLobbyButtonFunction();
        });

        lobbyPanel.setLayout(new BoxLayout(lobbyPanel, BoxLayout.Y_AXIS));

        startGamebutton.addActionListener(e -> {
            gameStartButtonFunction();
        });

        radioButtonReload.addActionListener(e -> {
            reloadButtonFunction();
        });

        fillLobbiesScrollPane(true);
    }

    private void reloadButtonFunction() {
        System.out.println("reload");
        fillLobbiesScrollPane(true);
    }

    private void gameStartButtonFunction() {
        try {
            Lobby lobby = client.serverStartGame();
            client.setLobby(lobby);

            client.connectToTheClients();
            client.sendUsersToOtherClients();
            client.sendStartToOtherClients();

            client.startAlcatrazGame();
            closeWindow();

        } catch (RemoteException remoteException) {
            remoteException.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }

    private void createLobbyButtonFunction() {
        String username = userNameTextField.getText();
        if (!(username.equals("") || username == null)) {

            client.getThisUser().setUsername(username);
            try {
                Lobby lobby = client.serverCreateLobby();
                fillLobbiesScrollPane(false);
                createLobbyButton.setVisible(false);
                addLeaveLobbieButton(lobby);

                startGamebutton.setVisible(true);
                userNameTextField.setEnabled(false);

            } catch (RemoteException ex) {
                ex.printStackTrace();

            } catch (AssertionError assertionError) {
                assertionError.printStackTrace();
                userNameTextField.setEnabled(true);

                userNameTextField.setText("Username already taken");
            }
        } else {
            userNameTextField.setText("Please enter a valid username!");
        }
    }

    public void closeWindow() {
        this.frame.setVisible(false);
    }

    public void showWindow(){
        this.frame.setVisible(true);
    }


    private void fillLobbiesScrollPane(Boolean generateButtons) {
        try {
            List<Lobby> lobbies = client.serverGetLobbies();

            lobbyPanel.removeAll();

            for (Lobby lobby : lobbies) {
                JPanel jPanel = new JPanel();
                JLabel jLabel = new JLabel();

                String labelText = "Lobby Nr" + lobby.getLobbyId();

                for (User user : lobby.getUsers()) {
                    labelText += " User:" + user.getUsername() + " ";
                }

                jLabel.setText(labelText);
                jPanel.add(jLabel);
                if (generateButtons) {
                    JButton jButton = new JButton();
                    jButton.setText("Join Lobby");

                    jButton.addActionListener(e -> {
                        joinLobbyButtonFunction(lobby, jButton);
                    });
                    jPanel.add(jButton);
                }
                lobbyPanel.add(jPanel);
                lobbyPanel.revalidate();
                lobbyPanel.repaint();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void joinLobbyButtonFunction(Lobby lobby, JButton jButton) {
        String username = userNameTextField.getText();
        if (!(username.equals("") || username == null)) {
            try {
                client.getThisUser().setUsername(username);
                client.serverJoinLobby(lobby.getLobbyId());
                jButton.setVisible(false);
                client.setLobby(lobby);

                addLeaveLobbieButton(lobby);

                startGamebutton.setVisible(true);
                createLobbyButton.setVisible(false);
                userNameTextField.setEnabled(false);

            } catch (RemoteException ex) {
                ex.printStackTrace();
            } catch (AssertionError assertionError) {
                assertionError.printStackTrace();

                userNameTextField.setText("Username already taken");
            }
        } else {
            userNameTextField.setText("Please enter a valid username!");
        }
    }


    private void addLeaveLobbieButton(Lobby lobby) {
        lobbyPanel.removeAll();
        JButton jButton = new JButton();
        jButton.setText("Leave lobby Nr " + lobby.getLobbyId() + "Users: " + lobby.getUsers());

        jButton.addActionListener(e -> {
            leafLobbyButtonFunction(lobby);
        });
        lobbyPanel.add(jButton);
        lobbyPanel.revalidate();
        lobbyPanel.repaint();

        radioButtonReload.setVisible(false);

        //Start Client RMI
        try {
            if (!client.isRmiStarted()) {
                client.startClientRMI();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void leafLobbyButtonFunction(Lobby lobby) {
        try {
            client.serverLeaveLobby(lobby.getLobbyId());
            fillLobbiesScrollPane(true);
            createLobbyButton.setVisible(true);
            startGamebutton.setVisible(false);

            radioButtonReload.setVisible(true);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    //draws the Window
    public void generateWindow() {
        frame = new JFrame("UserInterface");
        frame.setContentPane(this.root);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        init();
    }

    public UserInterfaceLobbies(ClientImpl client) {
        this.client = client;

    }


}

