package Server;

import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Evan on 12/28/2016.
 *
 * Contains all lobbys, removes users from lobbys and dissolves the lobby if all users have left it
 */
public class Lobbys {

    //Current ID of LAST CREATED LOBBY
    int cId;

    private List<Lobby> lobbys;

    private List<LobbyContext> contexts;

    private PolicyFactory inputPolicy;

    public Lobbys(PolicyFactory inputPolicy) {
        lobbys = new ArrayList<>();
        contexts = new ArrayList<>();
        this.inputPolicy = inputPolicy;
        cId = 0;
        loadContexts();
        //cId = 0;
    }

    public void addNewLobby(Lobby lobby) {
        lobbys.add(lobby);
    }

    public void removeLobby (Lobby lobby) {
        lobbys.remove(lobby);
    }

    public void doLobbyMessage(User user, String name, JSONObject input) {
        Lobby lobby = getLobbyByName(name);
        if (lobby == null) return;
        lobby.onMessage(user, input);
    }
    //Returns a lobby by the lobby name
    public Lobby getLobbyByName (String name) {
        for (Lobby lobby: lobbys) {
            if (lobby.name.equals(name)) return lobby;
        }
        return null;
    }
    //Returns a lobby by the lobby id
    public Lobby getLobbyById(int id) {
        for (Lobby lobby: lobbys) {
            if (lobby.id == id) return lobby;
        }
        return null;
    }
    //Remove the user from all lobbys.
    public void removeUser (User user) {
        for (Lobby lobby: lobbys) {
            if (lobby.onClose(user)) {
                //Dissolve the lobby
                removeLobby(lobby);
                lobby.isRunning = false;
                try {
                    lobby.thread.join();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }
    //Remove the user from the specified lobby
    public boolean removeUser(Lobby lobby, User user) {
        if (lobby != null && user != null) {
            //If the lobby needs to be dissolved, onClose will return true
            if (lobby.onClose(user)) {
                //Dissolve the lobby
                removeLobby(lobby);
                //Wait for the lobbys thread to end (reach the end of public void run() {});
                //This ensures that the lobby can end safely.
                lobby.isRunning = false;
                try {
                    if (lobby.thread != null) {
                        lobby.thread.join();
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }
            //clear the users current lobby
            user.setCurrentLobby(null);
        }
        return false;
    }
    //Return a list of the lobbys
    public List<Lobby> getList() {
        return lobbys;
    }

//    public void create(User user, String name, String pass) {
////        cId++;
////        DrawLobby lobby = new DrawLobby(user, name, pass, cId);
////        addNewLobby(lobby);
////        user.setCurrentLobby(lobby);
//    }
//
//    public void create(User user, String name) {
////        cId++;
////        DrawLobby lobby = new DrawLobby(user, name, cId);
////        addNewLobby(lobby);
////        user.setCurrentLobby(lobby);
//    }

    public void create(User user, JSONObject input) {
        LobbyContext context = getCapableLobby(input.getString("gamemode"));
        if (context == null) {
            System.err.println("NO_CONTEXTS_WITH_CAPABILITIES");
            return;
        }
        cId ++;
        Lobby lobby = context.create(user, input, cId, getNewToken(cId));
        addNewLobby(lobby);
        user.setCurrentLobby(lobby);
    }

    private String getNewToken(int cId) {
        return BCrypt.hashpw("" + cId + System.currentTimeMillis(), BCrypt.gensalt());
    }

    public void join(Lobby lobby, User user, JSONObject input) {
        LobbyContext context = lobby.getLobbyContext();
        context.join(lobby, user, input);
        user.setCurrentLobby(lobby);
    }

    public int getSize() {
        return lobbys.size();
    }

    public void loadContexts() {
        contexts.add(new DrawLobbyContext(inputPolicy));
        contexts.add(new TicTacContext(inputPolicy));

        System.out.println("Loaded " + contexts.size() + " lobby contexts.");

        loadDefaults();
    }

    public void loadDefaults() {
        for (LobbyContext context: contexts) {
            List<Lobby> lobbies = context.getDefaultLobbies();
            if (lobbies != null) {
                for (Lobby lobby: lobbies) {
                    cId ++;
                    lobby.id = cId;
                    lobbys.add(lobby);
                }
            }
        }
        System.out.println("Loaded " + lobbys.size() + " default lobbies." );
    }

    public LobbyContext getCapableLobby(String gameMode) {
        for (LobbyContext context: contexts) {
            for (String s: context.getCapabilities()) {
                if (gameMode.equals(s)) {
                    return context;
                }
            }
        }
        return null;
    }

    public LobbyContext getCapableLobby(String[] gameModes) {
        for (LobbyContext context: contexts) {
            String[] capabilities = context.getCapabilities();
            for (String s: gameModes) {
                for (String z : capabilities) {
                    if (s.equals(z)) {
                        return context;
                    }
                }
            }
        }
        return null;
    }


    public boolean isCapable(Lobby lobby, String[] capabilities) {
        LobbyContext context = lobby.getLobbyContext();
        for (String s: capabilities) {
            for (String cs: context.getCapabilities()) {
                if (s.equals(cs)) {
                    return true;
                }
            }
        }
        return false;
    }
    //Call the capable lobby
    public void onUserConnect(User user, String[] c, JSONObject input) {

        LobbyContext context = getCapableLobby(c);
        if (context == null) return;
        //Todo: need support for context to get all of its default lobbies
        context.onConnect(user, input);

    }

    public void stitchImage() {

        int sizeX = 1000;
        int sizeY = 800;

        List<DrawLobby> drawLobbies = new ArrayList<>();

        for (Lobby lobby: lobbys) {
            System.out.println("Gamemode: " + lobby.getGameMode());
            if (lobby.getGameMode().equals("FreeDraw") || lobby.getGameMode().equals("DrawIt")) {
                drawLobbies.add((DrawLobby)lobby);
            }
        }

        int expandedX = sizeX * drawLobbies.size();
        //int expandedY = sizeY * drawLobbies.size();

        BufferedImage paintImage = new BufferedImage(expandedX, sizeY, BufferedImage.TYPE_3BYTE_BGR);
        Graphics g = paintImage.createGraphics();
        Graphics2D g2 = (Graphics2D)g;
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, expandedX, sizeY);

        for (int i = 0; i < drawLobbies.size(); i ++) {
            DrawLobby lobby = drawLobbies.get(i);
            g2.setColor(Color.black);
            g2.setStroke(new BasicStroke(3));
            g2.drawRect(i * sizeX, 0, sizeX, sizeY);
            for (DrawLine drawLine: lobby.lines) {
                g2.setColor(drawLine.getLineColor());
                g2.setStroke(new BasicStroke(drawLine.getStrokeSize()));

                int[] pointsX = drawLine.getPointsX();
                int[] pointsY = drawLine.getPointsY();

                int[] offSetPointsX = new int[pointsX.length];
                int[] offSetPointsY = new int[pointsY.length];

                for (int x = 0; x < pointsX.length; x ++) {
                    offSetPointsX[x] = pointsX[x] + (sizeX * i);
                    offSetPointsY[x] = pointsY[x];
                    //offSetPointsY[x] = (x * sizeY);
                }

                g2.drawPolyline(offSetPointsX, offSetPointsY, offSetPointsX.length);
            }
        }

        g2.dispose();
        g.dispose();

        try {
            ImageIO.write(paintImage, "PNG", new File("stitch.png"));
            //Http2.sendDrawItMessage();
        }
        catch (Exception e) {
            e.printStackTrace();
        }


    }
}
