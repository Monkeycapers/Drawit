package Server;

import org.json.JSONObject;
import org.owasp.html.PolicyFactory;

import java.util.List;


public class TicTacContext extends LobbyContext {
    PolicyFactory inputPolicy;

    private final String[] capabilites = new String[] {"TicTacToe"};

    public TicTacContext(PolicyFactory inputPolicy) {
        this.inputPolicy = inputPolicy;
    }

    @Override
    public Lobby create(User user, JSONObject input, int lobbyId, String lobbyToken) {
        Lobby lobby;
        if (input.has("pass")) {
            lobby = new TicTacLobby(this, user, inputPolicy.sanitize(input.getString("name")), inputPolicy.sanitize(input.getString("pass")), lobbyId, lobbyToken);
        }
        else {
            lobby = new TicTacLobby(this, user, inputPolicy.sanitize(input.getString("name")), lobbyId, lobbyToken);
        }
        return lobby;
    }

    @Override
    public void join(Lobby lobby, User user, JSONObject input) {
        TicTacLobby ticTacLobby = (TicTacLobby)lobby;
        ticTacLobby.onConnect(user);
    }

    @Override
    public String[] getCapabilities() {
        return capabilites;
    }

    @Override
    public List<Lobby> getDefaultLobbies() {
        return null;
    }

    @Override
    public String getPath() {
        return "TicTac.html";
    }

    @Override
    public void onConnect(User user, JSONObject input) {
        //Todo: Some way for the context to get all of its lobbys currently stored in lobbys....
    }

}
