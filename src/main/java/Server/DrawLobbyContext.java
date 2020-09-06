package Server;

import org.json.JSONObject;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

import java.util.ArrayList;
import java.util.List;

public class DrawLobbyContext extends LobbyContext {

    private final String[] CAPABILITIES = new String[]{"DrawIt", "FreeDraw"};

    PolicyFactory inputPolicy;

    public DrawLobbyContext(PolicyFactory policyBuilder) {
        this.inputPolicy = policyBuilder;
    }

    @Override
    public Lobby create(User user, JSONObject input, int lobbyId, String lobbyToken) {
        Lobby lobby;
        if (input.has("pass")) {
            lobby = new DrawLobby(this, user, inputPolicy.sanitize(input.getString("name")), inputPolicy.sanitize(input.getString("pass")), lobbyId,
                    input.getString("gamemode").equals("FreeDraw"), lobbyToken);
        }
        else {
            lobby = new DrawLobby(this, user, inputPolicy.sanitize(input.getString("name")), lobbyId, input.getString("gamemode").equals("FreeDraw"), lobbyToken);
        }
        return lobby;
    }

    @Override
    public void join(Lobby lobby, User user, JSONObject input) {
        DrawLobby drawLobby = (DrawLobby)lobby;
        drawLobby.onConnect(user);
        //Todo: if needed, more setup / different setup can be done here
    }

    @Override
    public String[] getCapabilities() {
        return CAPABILITIES;
    }

    @Override
    public List<Lobby> getDefaultLobbies() {
        //Todo: lobbyToken
        List<Lobby> lobbies = new ArrayList<>();
        Lobby defaultLobby = new DrawLobby(this, "Default", 0, true, 0);
        lobbies.add(defaultLobby);
//        for (int i = 1; i < 100; i ++) {
//            lobbies.add(new DrawLobby(this, "lol", i, true));
//        }
        return lobbies;
    }

    @Override
    public String getPath() {
        return "drawit";
    }

    @Override
    public void onConnect(User user, JSONObject input) {
        //Todo: Some way for the context to get all of its lobbys currently stored in lobbys....
    }
}
