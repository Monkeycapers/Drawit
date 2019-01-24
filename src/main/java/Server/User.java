package Server;

import Jesty.TCPBridge.ClientWorker;

/**
 * Created by Evan on 7/1/2017.
 */
public final class User {

    private String nick;

    private Lobby currentLobby;

    public ClientWorker clientWorker;

    private int id;

    public User (ClientWorker clientWorker, int id) {
        this.clientWorker = clientWorker;
        nick = "guest" + id;
        this.id = id;
    }

    public User (ClientWorker clientWorker, String defaultUser, int id) {
        this.clientWorker = clientWorker;
        this.nick = defaultUser;
        this.id = id;
    }

    public String getNick() {
        return nick;
    }
    public void setNick(String nick) {
        this.nick = nick;
    }

    public Lobby getCurrentLobby () {
        return currentLobby;
    }

    public boolean setCurrentLobby(Lobby lobby) {
        this.currentLobby = lobby;
        return true;
    }

    public int getId() {
        return id;
    }

    public boolean isInLobby () {
        return currentLobby != null;
    }



}
