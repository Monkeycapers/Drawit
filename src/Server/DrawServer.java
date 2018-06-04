package Server;

import Jesty.TCPBridge.ClientWorker;
import Jesty.TCPBridge.Server;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import java.io.File;
import java.io.StringWriter;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;



/**
 * Created by Evan on 7/1/2017.
 */
public class DrawServer extends Server {

    List<User> users;

    public Lobbys lobbys;
    //ID of CURRENT User
    int cUserId;

    DrawLobby defaultLobby;

    WebServer webServer;

    PolicyFactory inputPolicy;

    Jump jump;

    String url = "localhost";

    Timer discordTimer;

    int webServerPort = 8081;

    public DrawServer(int raw_port, int web_port) {
        super (raw_port, web_port);
        setup();
    }

    public DrawServer(int port, boolean type) {
        super (port, type);
        setup();
    }

    private void setup() {
        inputPolicy = new HtmlPolicyBuilder()
                .allowElements()
                .toFactory();
        try {
            DrawWords.loadFromFile(new File("words.txt"));
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        webServer = new WebServer(url, webServerPort);
        users = new ArrayList<User>();
        lobbys = new Lobbys(inputPolicy);
        jump = new Jump();

//        discordTimer = new Timer("Discord");
//        discordTimer.scheduleAtFixedRate(new TimerTask() {
//            @Override
//            public void run() {
//                DrawLobby lobby = (DrawLobby)lobbys.getLobbyById(1);
//                lobby.drawToDiscord();
//            }
//        }, 300000, 300000);
//
//        Http2.sendDrawItServerOpenMessage();


    }

    @Override
    public void onOpen(ClientWorker clientWorker, int code) {
        cUserId++;
        User user = new User(clientWorker, cUserId);
        users.add(user);
        clientWorker.clientData = user;
        try {
            Thread.sleep(10);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        //clientWorker.sendMessage("Acknowledging presence");
        if (lobbys.getList().isEmpty()) return;
        JSONArray jsonArray = new JSONArray();
        for (Lobby lobby: lobbys.getList()) {
            JSONObject object = new JSONObject();
            object.put("id", lobby.id);
            object.put("name", lobby.name);
            object.put("private", lobby.isPrivate);
            object.put("players", lobby.getPlayerCount());
            object.put("maxplayers", lobby.maxSize);
            object.put("gamemode", lobby.getGameMode());
            object.put("status", lobby.getStatus());
            jsonArray.put(object);
            //jsonArray.put(new LobbyEntry(lobby.id, lobby.name, lobby.isPrivate, lobby.getPlayerCount(), lobby.maxSize));
        }
        StringWriter stringWriter = new StringWriter();
        new JSONWriter(stringWriter).object()
                .key("argument").value("addlobbys")
                .key("lobbys").value(jsonArray)
                .endObject();
        clientWorker.sendMessage(stringWriter.toString());
        //Todo: Only if client is [capable]
        //Todo: client sends capabilites
//        defaultLobby.onConnect(user);
//        user.setCurrentLobby(defaultLobby);
//        updateLobby(defaultLobby, false);
//        clientWorker.sendMessage(getLobbyEntryString("lobbyopen", "Welcome to DrawIt!", user.getCurrentLobby()));
    }

    @Override
    public void onClose(ClientWorker clientWorker, int code) {
        User user = (User)(clientWorker.clientData);
        users.remove(user);
        if (user.isInLobby()) {
            Lobby lobby = user.getCurrentLobby();
            lobbys.removeUser(lobby, user);
            updateLobby(lobby, false);
        }

    }

    @Override
    public void onMessage(ClientWorker clientWorker, String message) {
        User user = (User)(clientWorker.clientData);
        System.out.println("From client: " + message);
        JSONObject input;
        try {
            input = new JSONObject(message);
        }
        catch (JSONException e) {
            e.printStackTrace();
            return;
        }


        String toSend = "";

        switch (input.getString("argument")) {
            case "join": {

                if (input.has("nick")) {
                    String nick = inputPolicy.sanitize(input.getString("nick"));
                    if (!nick.equals("")) user.setNick(nick);
                }

                Lobby lobby;
                if (input.has("id")) {
                    lobby = lobbys.getLobbyById(input.getInt("id"));
                }
                else {
                    String name = inputPolicy.sanitize(input.getString("name"));
                    lobby = lobbys.getLobbyByName(name);
                }

                if (user.isInLobby()) {
                    if (user.getCurrentLobby().equals(lobby)) {
                        //User tried to join the lobby they are connected to
                        break;
                    }
                    else {
                        lobbys.removeUser(user.getCurrentLobby(), user);
                    }
                }


                //Lobby lobby = lobbys.getLobbyByName(name);
                if (lobby == null) {
                    System.err.println("NO_SUCH_LOBBY , user: " + user.getNick());
                    break;
                }

                if (lobby.isPrivate) {
                    if (!input.has("pass")) break;
                    if (!lobby.password.equals(inputPolicy.sanitize(input.getString("pass")))) {
                        break;
                    }
                }


                if (lobby.canConnect(user)) {

                    //Check if the user's current page is capable

                    List<Object> capabilities = input.getJSONArray("capabilities").toList();

                    String[] c = new String[capabilities.size()];
                    if (lobbys.isCapable(lobby, capabilities.toArray(c))) {
                        lobbys.join(lobby, user, input);
                    }
                    else {
                        //Jump to another page
                        //Todo: cleanup
                        String page = lobby.getLobbyContext().getPath();
//                        for (String s: lobby.getLobbyContext().getCapabilities()) {
//                            if (s.equals("TicTacToe")) {
//                                page = "TicTac.html";
//                            }
//                            else {
//                                page = "index.html";
//                            }
//                        }

                        String token = jump.addRequest(user, lobby);

                        StringWriter stringWriter = new StringWriter();
                        new JSONWriter(stringWriter).object()
                                .key("argument").value("jump")
                                .key("page").value(page)
                                .key("token").value(token)
                                .endObject();
                        toSend = stringWriter.toString();
                        break;
                    }

                }
                else break;

                System.out.println("User joined the lobby.");
                updateLobby(lobby, false);
                String path = url + ":" + webServerPort + "/" + lobby.getLobbyContext().getPath();
                if (!lobby.lobbyToken.equals("")) {
                    path += "?id=" + lobby.id + "&token=" + lobby.lobbyToken;
                }
                toSend = getLobbyEntryString("lobbyopen", "Connected to lobby #" + lobby.id + "\nLobby name: " + lobby.name, lobby, path);

                break;
            }
            case "jointoken" : {
                if (input.has("nick")) {
                    String nick = inputPolicy.sanitize(input.getString("nick"));
                    if (!nick.equals("")) user.setNick(nick);
                }
                Lobby lobby = lobbys.getLobbyById(input.getInt("id"));
                if (!lobby.canConnect(user)) break;
                if (!lobby.lobbyToken.equals(input.getString("token"))) break;
                lobbys.join(lobby, user, input);
                System.out.println("User joined the lobby. (Token)");
                updateLobby(lobby, false);
                String path = url + ":" + webServerPort + "/" + lobby.getLobbyContext().getPath();
                if (!lobby.lobbyToken.equals("")) {
                    path += "?id=" + lobby.id + "&token=" + lobby.lobbyToken;
                }
                toSend = getLobbyEntryString("lobbyopen", "Connected to lobby #" + lobby.id + "\nLobby name: " + lobby.name, lobby, path);
                break;
            }
            case "leave": {
                if (!user.isInLobby()) {
                    //Todo: Already left the lobby
                    break;
                }
                Lobby lobby = user.getCurrentLobby();
                int id = lobby.id;
                if (lobbys.removeUser(lobby, user)) {
                    updateLobby(lobby, true);
                }
                else {
                    updateLobby(lobby, false);
                }
                System.out.println("User: " + user.getNick() + " left lobby #" + id);
                toSend = getLeaveMessage("Left the lobby successfully.");
                break;
            }
            case "create": {
                if (user.isInLobby()) {
                    lobbys.removeUser(user.getCurrentLobby(), user);
                }
                if (input.has("nick")) {
                    user.setNick(inputPolicy.sanitize(input.getString("nick")));
                }
                //todo return a lobby here
                lobbys.create(user, input);
                System.out.println("Created lobby.");
                Lobby lobby = user.getCurrentLobby();
                String path = url + ":" + webServerPort + "/" + lobby.getLobbyContext().getPath();
                if (!lobby.lobbyToken.equals("")) {
                    path += "?id=" + lobby.id + "&token=" + lobby.lobbyToken;
                }
                toSend = getLobbyEntryString("lobbyopen", "Created lobby #" + user.getCurrentLobby().id + "\nLobby name: " + user.getCurrentLobby().name, user.getCurrentLobby(), path);
                updateLobby(user.getCurrentLobby(), false);
                break;
            }
            case "lobbyinfo": {
                Lobby lobby = lobbys.getLobbyById(input.getInt("id"));
                if (lobby == null) break;
                toSend = getLobbyEntryString("lobbyinfo", lobby, lobby.getLobbyContext().getPath());
                break;
            }
            case "lobby": {
                //Todo: refactor, sanitize the chat message *here*
//                if (input.get("type").equals("chat")) {
//                    String msg = input.getString("message");
//                    if (msg.startsWith("/")) {
////                        if (msg.startsWith("/join")) {
////                            if (user.isInLobby()) {
////                                lobbys.removeUser(user.getCurrentLobby(), user);
////                            }
////
////                            int id = Integer.parseInt(msg.split(" ")[1]);
////                            Lobby lobby = lobbys.getLobbyById(id);
////
////                            if (lobby == null) {
////                                //Todo: Error: NO_SUCH_LOBBY
////                                System.err.println("NO_SUCH_LOBBY , user: " + user.getNick());
////                                break;
////                            }
//////                            if (lobby.isPrivate && input.has("pass")) {
//////                                if (lobby.password.equals(input.getString("pass"))) {
//////                                    if (lobby.canConnect(user)) {
//////                                        lobby.onConnect(user);
//////                                        user.setCurrentLobby(lobby);
//////                                    }
//////                                }
//////                                else {
//////                                    System.err.println("ACCESS_DENIED (wrong PW)");
//////                                }
//////                                break;
//////                            }
////                            if (lobby.canConnect(user)) {
////                                lobby.onConnect(user);
////                                user.setCurrentLobby(lobby);
////                                System.out.println("User joined the lobby.");
////
////                                StringWriter stringWriter = new StringWriter();
////                                new JSONWriter(stringWriter).object()
////                                        .key("argument").value("lobbyjoin")
////                                        .key("name").value(lobby.name)
////                                        .key("id").value(lobby.id)
////                                        .endObject();
////                                toSend = stringWriter.toString();
////
////                            }
////                            break;
////                        }
////                        else if (msg.startsWith("/create")) {
////                            if (user.isInLobby()) {
////                                lobbys.removeUser(user.getCurrentLobby(), user);
////                            }
////                            else {
////                                lobbys.create(user, msg.split(" ")[1]);
////                            }
////                            System.out.println("Created lobby.");
////                            StringWriter stringWriter = new StringWriter();
////                            new JSONWriter(stringWriter).object()
////                                    .key("argument").value("lobbyopen")
////                                    .key("id").value(user.getCurrentLobby().id)
////                                    .key("name").value(user.getCurrentLobby().name)
////                                    .endObject();
////                            toSend = stringWriter.toString();
////                            break;
////                        }
////                        else if (msg.startsWith("/leave")) {
////                            if (!user.isInLobby()) {
////                                //Todo: Already left the lobby
////                                break;
////                            }
////                            int id = user.getCurrentLobby().id;
////                            lobbys.removeUser(user.getCurrentLobby(), user);
////                            System.out.println("User: " + user.getNick() + " left lobby #" + id);
////                            break;
////                        }
//                        //Todo: NotLikeThis
//                        if (msg.startsWith("/stop fDQ758kqMhOgFmRt3AjE")) {
//                            System.exit(0);
//                            break;
//                        }
//                    }
//                }
                if (user.isInLobby()) {
                    user.getCurrentLobby().onMessage(user, input);
                }
                break;
            }
            case "jump": {
                JumpRequest r = jump.getRequest(input.getString("token"));
                if (r != null) {
                    //Todo: full migration of user object
                    user.setNick(r.getUser().getNick());
                    //Todo: safeguards
                    //Todo: not the same kind of input
                    lobbys.join(r.getLobby(), user, input);
                    System.out.println("User Jumped and joined the lobby.");
                    updateLobby(r.getLobby(), false);
                    String path = url + ":" + webServerPort + "/" + r.getLobby().getLobbyContext().getPath();
                    if (!r.getLobby().lobbyToken.equals("")) {
                        path += "?id=" + r.getLobby().id + "&token=" + r.getLobby().lobbyToken;
                    }
                    toSend = getLobbyEntryString("lobbyopen", "Connected to lobby #" + r.getLobby().id + "\nLobby name: " + r.getLobby().name, r.getLobby(), path);
                }
                else {
                    System.out.println("Request was null");
                }
                break;
            }
            case "capabilities": {
                List<Object> capabilities = input.getJSONArray("capabilities").toList();

                String[] c = new String[capabilities.size()];
                c = capabilities.toArray(c);
                lobbys.onUserConnect(user, c, input);
                break;
            }


        }
        if (!toSend.equals("")) {
            clientWorker.sendMessage(toSend);
        }
    }

    private String getLeaveMessage(String chatMessage) {
        StringWriter toSend = new StringWriter();
        new JSONWriter(toSend).object()
                .key("argument").value("leave")
                .key("chatmessage").value(chatMessage)
                .endObject();
        return toSend.toString();
    }

    public void updateLobby (Lobby lobby, boolean remove) {
       sendToAll(getLobbyEntryString(remove ? "removelobby" : "addlobby", lobby, lobby.getLobbyContext().getPath()));
    }

    public void sendToAll(String s) {
        for (User u: users) {
            u.clientWorker.sendMessage(s);
        }
    }

    public String getLobbyEntryString(String argument, Lobby lobby, String inviteUrl) {
        StringWriter stringWriter = new StringWriter();
        new JSONWriter(stringWriter).object()
                .key("argument").value(argument)
                .key("id").value(lobby.id)
                .key("players").value(lobby.getPlayerCount())
                .key("name").value(lobby.name)
                .key("private").value(lobby.isPrivate)
                .key("maxplayers").value(lobby.maxSize)
                .key("gamemode").value(lobby.getGameMode())
                .key("status").value(lobby.getStatus())
                .key("path").value(inviteUrl)
                .endObject();
        return stringWriter.toString();
    }

    public String getLobbyEntryString(String argument, String chatMessage, Lobby lobby, String inviteUrl) {
        StringWriter stringWriter = new StringWriter();
        new JSONWriter(stringWriter).object()
                .key("argument").value(argument)
                .key("id").value(lobby.id)
                .key("players").value(lobby.getPlayerCount())
                .key("name").value(lobby.name)
                .key("private").value(lobby.isPrivate)
                .key("maxplayers").value(lobby.maxSize)
                .key("gamemode").value(lobby.getGameMode())
                .key("status").value(lobby.getStatus())
                .key("path").value(inviteUrl)
                .key("chatmessage").value(chatMessage)
                .endObject();
        return stringWriter.toString();
    }


    public static void main(String[] args) {
        if (args.length == 0) {
            //Todo
            //System.out.println("Arguments: ...");
            DrawServer drawServer = new DrawServer(16000, 8080);
            drawServer.start();

            while (true) {
                String in = new Scanner(System.in).nextLine();
                if (in.equals("/l lobbys")) {
                    List<Lobby> lobbys = drawServer.lobbys.getList();
                    for (Lobby l: lobbys) {
                        System.out.println("Lobby #" + l.id + ":" + l);
                    }
                }
                else if (in.equals("/stats")) {
                    System.out.println(drawServer.lobbys.getSize() + " lobbys. " + drawServer.users.size() + " users.");
                    System.out.println(drawServer.jump.getList().size() + " jump requests in q.");
                }
                else if (in.equals("/l jump")) {
                    for (JumpRequest request: drawServer.jump.getList()) {
                        System.out.println(request.getToken());
                    }
                }
                else if (in.equals("/mem")) {
                    double currentMemory = ( (double)((double)(Runtime.getRuntime().totalMemory()/1024)/1024))- ((double)((double)(Runtime.getRuntime().freeMemory()/1024)/1024));
                    System.out.println("Memory usage: " + currentMemory + "mb");
                }
                else if (in.equals("/discord")) {
                    DrawLobby lobby = (DrawLobby)drawServer.lobbys.getLobbyById(1);
                    lobby.drawToDiscord();
                }
                else if (in.equals("/stitch")) {
                    drawServer.lobbys.stitchImage();
                }
            }
        }
    }


}
