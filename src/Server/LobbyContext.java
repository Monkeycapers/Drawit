package Server;

import org.json.JSONObject;

import java.util.List;

//The stuff outside of the lobby itself (creating, joining, etc)
public abstract class LobbyContext {

    //Called when creating a lobby, create the lobby here
    public abstract Lobby create(User user, JSONObject input, int lobbyId, String lobbyToken);
    //
    public abstract void join(Lobby lobby, User user, JSONObject input);
    //Return all gamemodes the lobby supports
    public abstract String[] getCapabilities();
    //Default lobbys to be automatically created on start (return null if no need)
    public abstract List<Lobby> getDefaultLobbies();
    //Get the path on the server to serve to a jumping client
    public abstract String getPath();
    //Announces presence of new user. Can do things like auto connect to a default lobby.
    public abstract void onConnect(User user, JSONObject input);

}
