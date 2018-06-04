package Client;

import Jesty.TCPBridge.Client;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONWriter;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.io.StringWriter;
import java.util.*;

/**
 * Created by Evan on 7/1/2017.
 */
public class DrawClient extends Client {

    DrawGUI gui;

    LobbyBrowser lobbyBrowser;

    public DrawClient(String host, int port) {
        super(host, port);
        gui = new DrawGUI(this);
        lobbyBrowser = new LobbyBrowser(this);
    }

    @Override
    public void onOpen() {
        System.out.println("Connected to server");
    }

    @Override
    public void onClose() {
        System.out.println("Lost connection to server");
    }

    @Override
    public void onMessage(String message) {
        System.out.println("From server: " + message);

        try {
            JSONObject input = new JSONObject(message);

            switch (input.getString("argument")) {
                case "startdrawing" : {
                    //System.out.println("Time to draw: " + input.getInt("time"));
                    //System.out.println("Word to draw: " + input.getString("word"));
                    gui.addChatMessage("You are DRAWING this round! You have " + input.getInt("time") + " seconds to draw." +
                            "\n" + "Your word to draw is: \n'" + input.getString("word") + "'.\nGood Luck!");
                    gui.startDrawing();
                    gui.setCountDown(input.getInt("time"));
                    break;
                }
                case "startguessing" : {
                    //System.out.println("Time to guess: " + input.getInt("time"));
                    gui.addChatMessage("You are GUESSING this round!\n You have " + input.getInt("time") + " seconds to guess." +
                            "\n The word is " + input.getInt("wordlength") + " chars long.\nGood Luck!");
                    gui.startGuessing();
                    gui.setCountDown(input.getInt("time"));
                    break;
                }
                case "endguessing" : {
//                    System.out.println("The word was: " + input.getString("word"));
//                    System.out.println(input.getInt("correctguesses") + " people guessed right!");
//
//                    System.out.println("Time until next round: " + input.getInt("time"));

                    gui.addChatMessage("ROUND OVER!\nThe word was: " + input.getString("word") + ".\nThere were "
                            + input.getInt("correctguesses") + " correct guesses.\n" + input.getString("scoreboard") + "Time until next round: " + input.getInt("time") + " seconds");
                    gui.setCountDown(input.getInt("time"));

                    JSONArray users = input.getJSONArray("users");
                    for (Object o: users) {
                        JSONObject json = new JSONObject(String.valueOf(o));
                        UserEntry userEntry = new UserEntry(json.getString("nick"), json.getInt("id"), json.getInt("score"));
                        gui.addUserEntry(userEntry);
                    }
                    break;
                }
                case "countdown" : {
                    gui.addChatMessage("Time until next round: " + input.getInt("time"));
                    gui.setCountDown(input.getInt("time"));
                    //System.out.println("Time until next round: " + input.getInt("time"));
                    break;
                }
                case "chat" : {

                    if (input.getBoolean("result")) {
                        //System.out.println("Guess for: " + input.getString("message") + " Was correct!");
                        gui.addChatMessage("Guess for: " + input.getString("message") + " Was correct!");
                    }
                    else {
                        //System.out.println("Chat message: " + input.getString("message"));
                        gui.addChatMessage(input.getString("message"));
                    }
                    break;
                }
                case "drawpoint" : {

                    gui.addPoint(input.getInt("x"), input.getInt("y"), new Color(input.getInt("cR"), input.getInt("cG"), input.getInt("cB"), input.getInt("cA")), input.getInt("stroke"));

                    break;
                }
                case "drawline" : {
                    //Todo: Reset the temp line (pointsX and pointsY)
                    int amountPoints = input.getInt("apoints");

                    List<Object> xPoints = input.getJSONArray("lineX").toList();
                    List<Object> yPoints = input.getJSONArray("lineY").toList();

                    int[] X = new int[xPoints.size()];
                    int[] Y = new int[yPoints.size()];

                    for (int i = 0; i < xPoints.size(); i ++) {
                        X[i] = (int)xPoints.get(i);
                        Y[i] = (int)yPoints.get(i);
                    }

                    gui.addLine(X, Y, new Color(input.getInt("cR"), input.getInt("cG"), input.getInt("cB"), input.getInt("cA")), input.getInt("stroke"));

                    break;
                }
                case "deleteline" : {
                    gui.undo();
                    break;
                }
                case "deleteall" : {
                    gui.reset();
                    break;
                }


                case "lobbyopen" : {
                    //gui.addChatMessage("Lobby opened!\nYour lobby id is: " + input.getInt("id") );
                    gui.addChatMessage(input.getString("chatmessage"));
                    gui.stateMode = input.getString("gamemode").equals("FreeDraw");
                    gui.setTitle(input.getString("name") + " | id: " + input.getInt("id") );
                    if (!input.getString("gamemode").equals("FreeDraw"))
                    lobbyBrowser.hideAll();
                    break;
                }
//                case "lobbyjoin" : {
//                    gui.addChatMessage("Connected to lobby " + input.getString("name") + "\nLobby id: " + input.getInt("id"));
//                    gui.setTitle(input.getString("name") + " | id: " + input.getInt("id") );
//                    break;
//                }

                //Todo: get lobbyEntry from a fct that takes the input json

                case "addlobby" : {
                    LobbyEntry lobbyEntry = new LobbyEntry(input.getInt("id"), input.getString("name"),
                            input.getBoolean("private"), input.getInt("players"),  input.getInt("maxplayers"),
                            input.getString("gamemode") , input.getString("status"));
                    lobbyBrowser.addLobbyEntry(lobbyEntry);
                    break;
                }
                case "removelobby" : {
                    LobbyEntry lobbyEntry = new LobbyEntry(input.getInt("id"), input.getString("name"),
                            input.getBoolean("private"), input.getInt("players"),  input.getInt("maxplayers"),
                            input.getString("gamemode") , input.getString("status"));
                    lobbyBrowser.removeLobbyEntry(lobbyEntry);
                    break;
                }

                case "addlobbys" : {
                    JSONArray lobbys = input.getJSONArray("lobbys");
                    for (Object o: lobbys) {
                        JSONObject json = new JSONObject(String.valueOf(o));
                        LobbyEntry lobbyEntry = new LobbyEntry(json.getInt("id"), json.getString("name"),
                                json.getBoolean("private"), json.getInt("players"),  json.getInt("maxplayers"),
                                json.getString("gamemode") , json.getString("status"));
                        lobbyBrowser.addLobbyEntry(lobbyEntry);
                    }
                    break;
                }

                case "adduser" : {
                    UserEntry userEntry = new UserEntry(input.getString("nick"), input.getInt("id"), input.getInt("score"));
                    gui.addUserEntry(userEntry);
                    break;
                }
                case "removeuser" : {
                    UserEntry userEntry = new UserEntry(input.getString("nick"), input.getInt("id"), input.getInt("score"));
                    gui.removeUserEntry(userEntry);
                    break;
                }
                case "addusers" : {
                    JSONArray users = input.getJSONArray("users");
                    for (Object o: users) {
                        JSONObject json = new JSONObject(String.valueOf(o));
                        UserEntry userEntry = new UserEntry(json.getString("nick"), json.getInt("id"), json.getInt("score"));
                        gui.addUserEntry(userEntry);
                    }
                    break;
                }



                case "lobbyinfo" : {
                    LobbyEntry lobbyEntry = new LobbyEntry(input.getInt("id"), input.getString("name"),
                            input.getBoolean("private"), input.getInt("players"),  input.getInt("maxplayers"),
                            input.getString("gamemode") , input.getString("status"));
                    if (lobbyBrowser.joinDialog != null) {
                        if (lobbyBrowser.joinDialog.isVisible()) {
                            lobbyBrowser.joinDialog.refresh(lobbyEntry);
                        }
                    }
                }

            }
        }
        catch (UnsupportedOperationException e) {
            System.err.println("Operation not implemented");
        }
        catch (Exception e) {
            e.printStackTrace();
        }


    }

    public static void main(String[] args) {

        try {
            // Set System L&F
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e) {
            e.printStackTrace();
        }


        //DrawClient client = new DrawClient("138.197.170.7", 16000);
        DrawClient client = new DrawClient("localhost", 16000);
        client.start();

        while (true) {
            String in = new Scanner(System.in).nextLine();
            client.sendMessage(in);
        }
    }

    public void setLobbyBrowserVisible(boolean visible) {
        lobbyBrowser.setVisible(visible);
    }

    public void sendLine(int[] x, int[] y, Color c, int strokeSize) {
        StringWriter stringWriter = new StringWriter();
        new JSONWriter(stringWriter).object()
                .key("argument").value("lobby")
                .key("type").value("drawline")
                .key("lineX").value(x)
                .key("lineY").value(y)
                .key("stroke").value(strokeSize)
                .key("cR").value(c.getRed())
                .key("cG").value(c.getGreen())
                .key("cB").value(c.getBlue())
                .key("cA").value(c.getAlpha())
                .key("apoints").value(x.length)
                .endObject();
        sendMessage(stringWriter.toString());
    }

    public void sendPoint(int x, int y) {
        StringWriter stringWriter = new StringWriter();
        new JSONWriter(stringWriter).object()
                .key("argument").value("lobby")
                .key("type").value("drawpoint")
                .key("x").value(x)
                .key("y").value(y)
                .endObject();
        sendMessage(stringWriter.toString());
    }

    public void sendChatMessage(String text) {
        StringWriter stringWriter = new StringWriter();
        new JSONWriter(stringWriter).object()
                .key("argument").value("lobby")
                .key("type").value("chat")
                .key("message").value(text)
                .endObject();
        sendMessage(stringWriter.toString());
    }

    public void sendUndoMessage() {
        StringWriter stringWriter = new StringWriter();
        new JSONWriter(stringWriter).object()
                .key("argument").value("lobby")
                .key("type").value("deleteline")
                .endObject();
        sendMessage(stringWriter.toString());
    }
    public void sendResetMessage() {
        StringWriter stringWriter = new StringWriter();
        new JSONWriter(stringWriter).object()
                .key("argument").value("lobby")
                .key("type").value("deleteall")
                .endObject();
        sendMessage(stringWriter.toString());
    }

    public void sendLeaveMessage() {
        StringWriter stringWriter = new StringWriter();
        new JSONWriter(stringWriter).object()
                .key("argument").value("leave")
                .endObject();
        sendMessage(stringWriter.toString());
    }

    public void sendJoinMessage(int id, String nick) {
        StringWriter stringWriter = new StringWriter();
        new JSONWriter(stringWriter).object()
                .key("argument").value("join")
                .key("nick").value(nick)
                .key("id").value(id)
                .endObject();
        sendMessage(stringWriter.toString());
    }

    public void sendJoinMessage(int id, String nick, String password) {
        StringWriter stringWriter = new StringWriter();
        new JSONWriter(stringWriter).object()
                .key("argument").value("join")
                .key("nick").value(nick)
                .key("pass").value(password)
                .key("id").value(id)
                .endObject();
        sendMessage(stringWriter.toString());
    }
    //Todo: api rename (to something like updateState)
    public void sendDrawColorMessage(Color c, int strokeSize) {
        StringWriter stringWriter = new StringWriter();
        new JSONWriter(stringWriter).object()
                .key("argument").value("lobby")
                .key("type").value("setdrawcolor")
                .key("stroke").value(strokeSize)
                .key("cR").value(c.getRed())
                .key("cG").value(c.getGreen())
                .key("cB").value(c.getBlue())
                .key("cA").value(c.getAlpha())
                .endObject();
        sendMessage(stringWriter.toString());
    }

    public void sendCreateMessage(String nick, String name) {
        StringWriter stringWriter = new StringWriter();
        new JSONWriter(stringWriter).object()
                .key("argument").value("create")
                .key("nick").value(nick)
                .key("name").value(name)
                .endObject();
        sendMessage(stringWriter.toString());
    }

    public void sendCreateMessage(String nick, String name, String pass) {
        StringWriter stringWriter = new StringWriter();
        new JSONWriter(stringWriter).object()
                .key("argument").value("create")
                .key("nick").value(nick)
                .key("name").value(name)
                .key("pass").value(pass)
                .endObject();
        sendMessage(stringWriter.toString());
    }

    public void showLobbyBrowser() {
        lobbyBrowser.setVisible(true);
    }

    public void sendLobbyInfoMessage(int id) {
        StringWriter stringWriter = new StringWriter();
        new JSONWriter(stringWriter).object()
                .key("argument").value("lobbyinfo")
                .key("id").value(id)
                .endObject();
        sendMessage(stringWriter.toString());
    }
}
