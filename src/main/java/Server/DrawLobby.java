package Server;

import jdk.nashorn.internal.parser.JSONParser;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONWriter;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicBorders;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Evan on 7/1/2017.
 */
public class DrawLobby extends Lobby {

    List<DrawUser> users;

    int countdown;

    final int START_COUNTDOWN_TIME = 3;

    final int DRAW_GUESS_COUNTDOWN_TIME = 60;

    int currentDrawer = 0;

    Color selectedColor;

    int strokeSize;
    /*
    0: Normal (drag mouse)
    1: Line
     */
    int drawMode;

    int correctGuesses;

    List<DrawLine> lines;

    DrawLine tempLine;

    String currentWord;

    PolicyFactory inputPolicy;

    List<String> words;



    private enum Status {
        Stopped, CountDown, Drawing, FreeDraw
    }

    Status status = Status.Stopped;

    DrawLobbyContext context;

    boolean destructOnClose;

    public DrawLobby(DrawLobbyContext context, User user, String name, int id, boolean isFreeDraw, String lobbyToken ) {
        status = isFreeDraw ? Status.FreeDraw : Status.Stopped;
        users = new ArrayList<>();
        users.add(new DrawUser(user));
        this.id = id;
        this.name = name;
        this.context = context;
        this.lobbyToken = lobbyToken;
        this.maxSize = 20;
        destructOnClose = true;
        reset();
    }

    public DrawLobby(DrawLobbyContext context, User user, String name, String password, int id, boolean isFreeDraw, String lobbyToken) {
        status = isFreeDraw ? Status.FreeDraw : Status.Stopped;
        users = new ArrayList<>();
        users.add(new DrawUser(user));
        this.isPrivate = true;
        this.password = password;
        this.name = name;
        this.id = id;
        this.context = context;
        this.lobbyToken = lobbyToken;
        this.maxSize = 20;
        destructOnClose = true;
        reset();
    }

    public DrawLobby(DrawLobbyContext context, String name, int id, boolean isFreeDraw, int maxSize) {
        status = isFreeDraw ? Status.FreeDraw : Status.Stopped;
        users = new ArrayList<>();
        this.name = name;
        this.id = id;
        this.maxSize = maxSize;
        this.context = context;
        destructOnClose = false;
        reset();
    }

    private void reset() {
        inputPolicy = new HtmlPolicyBuilder()
                .allowElements()
                .toFactory();
        lines = new ArrayList<>();
        drawMode = 0;
        selectedColor = Color.black;
        strokeSize = 4;
        currentDrawer = 0;
        tempLine = new DrawLine(Color.black, strokeSize);
        words = DrawWords.getWords();
    }

    @Override
    public void onConnect(User user) {
        DrawUser u = new DrawUser(user);
        users.add(u);
        if (status == Status.FreeDraw) {
            //Todo - fix temp lines in free draw
            user.clientWorker.sendMessage(sendLines());
        }
        else if (status == Status.Stopped) {
            status = Status.CountDown;
           // countdown = START_COUNTDOWN_TIME;
            countdown = 1;
            StringWriter stringWriter = new StringWriter();
            new JSONWriter(stringWriter).object()
                    .key("argument").value("countdown")
                    .key("time").value(countdown)
                    .endObject();
            sendToAll(stringWriter.toString());
            start();
        }
        else if (status == Status.Drawing) {
            StringWriter stringWriter = new StringWriter();
            new JSONWriter(stringWriter).object()
                    .key("argument").value("startguessing")
                    .key("wordlength").value(currentWord.length())
                    .key("time").value(countdown)
                    .endObject();
            user.clientWorker.sendMessage(stringWriter.toString());
            user.clientWorker.sendMessage(sendLines());
        }
        StringWriter stringWriter = new StringWriter();
        new JSONWriter(stringWriter).object()
                .key("argument").value("addusers")
                .key("users").value(getUsersJson())
                .endObject();
        user.clientWorker.sendMessage(stringWriter.toString());
        updateUsers(u, false);
    }


    @Override
    public boolean onClose(User user) {
        DrawUser drawUser = getUser(user);
        DrawUser drawer = null;
        if (currentDrawer < users.size() - 1) {
            drawer = users.get(currentDrawer);
        }
        users.remove(drawUser);
        if (users.size() <= 1 && status != Status.FreeDraw) {
            status = Status.Stopped;
        }
        else if (drawer != null) {
            if (drawer == drawUser) countdown = 0;
        }
//        else if (drawUser.equals(currentDrawer)) {
//            countdown = 0;
//        }

        if (users.isEmpty() && (status != Status.FreeDraw || destructOnClose)) {
            return true;
        }
        updateUsers(drawUser, false);
        return false;
    }


    @Override
    public void onMessage(User user, JSONObject input) {
        DrawUser u = getUser(user);
        switch (input.getString("type")) {
            case "chat": {
                boolean result;
                String message;
                //No cheating!
                if ((u.equals(users.get(currentDrawer)) && status == Status.Drawing ) || u.hasGuessed) break;
                if (input.getString("message").equalsIgnoreCase(currentWord)) {
                    //Guessed right
                    //Todo: Check if the user already has guessed and gotten correct
                    u.score ++;
                    u.hasGuessed = true;
                    correctGuesses++;
                    if (correctGuesses == users.size() - 1) {
                        countdown = 1;
                    }
                    //If result is true, the guess was correct (and this is only sent back)
                    //else it was not correct (and sent to all)
                    //input.put("result", true);
                    result = true;
                    message = inputPolicy.sanitize(input.getString("message"));
                    //sendMessage(u, input.toString());
                }
                else {
                    //input.put("result", false);
                    //Send the chat message (or, wrong answer) to all
                    result = false;
                    message = "#" + user.getId() + " " + user.getNick() + ": " + inputPolicy.sanitize(input.getString("message"));
                    //sendToAll(input.toString());
                }
                StringWriter stringWriter = new StringWriter();
                new JSONWriter(stringWriter).object()
                        .key("argument").value("chat")
                        .key("message").value(message)
                        .key("userid").value(user.getId())
                        .key("result").value(result)
                        .endObject();
                if (result) {
                    sendMessage(u, stringWriter.toString());
                }
                else {
                    sendToAll(stringWriter.toString());
                }
                break;
            }
            case "drawline": {
                if (!u.equals(users.get(currentDrawer)) && status != Status.FreeDraw) break;
                tempLine = new DrawLine(selectedColor, strokeSize);
                int amountPoints = input.getInt("apoints");
                List<Object> xPoints = input.getJSONArray("lineX").toList();
                List<Object> yPoints = input.getJSONArray("lineY").toList();
                List<DrawPoint> points = new ArrayList<>();

                for (int i = 0; i < amountPoints; i++) {
                    points.add(new DrawPoint((int)xPoints.get(i), (int)yPoints.get(i)));
                }

                DrawLine drawLine = new DrawLine(new Color(input.getInt("cR"),input.getInt("cG"),input.getInt("cB"), input.getInt("cA")), points, input.getInt("stroke"));
                lines.add(drawLine);
                System.out.println("STATUS: " + status.name());
                if (status == Status.FreeDraw) {
                    sendToAllBut(u, sendDrawLine(drawLine));
                }
                else {
                    sendToAllButDrawer(sendDrawLine(drawLine));
                }
                break;

            }
            case "drawpoint": {
                if (!u.equals(users.get(currentDrawer)) || status == Status.FreeDraw) break;
                tempLine.points.add(new DrawPoint(input.getInt("x"), input.getInt("y")));
//                input.remove("argument");
//                input.remove("type");
//                input.append("argument", "drawpoint");
//                input.append("cR", selectedColor.getRed());
//                input.append("cG", selectedColor.getGreen());
//                input.append("cB", selectedColor.getBlue());
//                input.append("cA", selectedColor.getAlpha());

                StringWriter stringWriter = new StringWriter();
                new JSONWriter(stringWriter).object()
                        .key("argument").value("drawpoint")
                        .key("x").value(input.getInt("x"))
                        .key("y").value(input.getInt("y"))
                        .key("cR").value(selectedColor.getRed())
                        .key("cG").value(selectedColor.getGreen())
                        .key("cB").value(selectedColor.getBlue())
                        .key("cA").value(selectedColor.getAlpha())
                        .key("stroke").value(strokeSize)
                        .endObject();

                sendToAllButDrawer(stringWriter.toString());
                break;
            }
            case "deleteline": {
                if (!u.equals(users.get(currentDrawer)) && status != Status.FreeDraw) break;

                if (status == Status.FreeDraw && !user.equals(users.get(0).user)) break;

                if (!lines.isEmpty()) {
                    lines.remove(lines.get(lines.size() - 1));
                }

                StringWriter stringWriter = new StringWriter();
                new JSONWriter(stringWriter).object()
                        .key("argument").value("deleteline")
                        .endObject();
                sendToAllButDrawer(stringWriter.toString());

                break;
            }
            case "deleteall": {
                if (!u.equals(users.get(currentDrawer)) && status != Status.FreeDraw) break;

                if (status == Status.FreeDraw && !user.equals(users.get(0).user)) break;

                lines = new ArrayList<>();

                StringWriter stringWriter = new StringWriter();
                new JSONWriter(stringWriter).object()
                        .key("argument").value("deleteall")
                        .endObject();
                sendToAllButDrawer(stringWriter.toString());

                break;
            }
            case "setdrawcolor" : {
                if (!u.equals(users.get(currentDrawer)) || status == Status.FreeDraw) break;
                selectedColor = new Color(input.getInt("cR"),input.getInt("cG"),input.getInt("cB"), input.getInt("cA"));
                strokeSize = input.getInt("stroke");
                break;
            }
        }
    }



    @Override
    public List<User> getUsers() {
        List<User> userlist = new ArrayList<>();
        for (DrawUser u: users) {
            userlist.add(u.user);
        }
        return userlist;
    }

    @Override
    public boolean canConnect(User user) {
        if (maxSize == 0) return true;
        else return (users.size() + 1) < maxSize;
    }

    @Override
    public int getPlayerCount() {
        return users.size();
    }

    @Override
    public void pause() {

    }

    @Override
    public String getGameMode() {
        if (status == Status.FreeDraw) return "FreeDraw";
        else return "DrawIt";
    }

    @Override
    public String getStatus() {
        if (status == Status.Stopped) {
            return "Waiting";
        }
        if (status == Status.CountDown) {
            return "Starting";
        }
        return status.name();
    }

    public DrawUser getUser(User user) {
        for (DrawUser u: users) {
            if (u.user.equals(user)) return u;
        }
        return null;
    }

    public JSONArray getUsersJson() {
        JSONArray jsonArray = new JSONArray();
        for (DrawUser u: users) {
            JSONObject object = new JSONObject();
            object.put("id", u.user.getId());
            object.put("nick", u.user.getNick());
            object.put("score", u.score);
            jsonArray.put(object);
        }
        return jsonArray;
    }

    public void updateUsers(DrawUser user, boolean remove) {
        sendToAll(getUserEntryString(remove ? "removeuser": "adduser", user));
    }

    public String getUserEntryString(String argument, DrawUser u) {
        StringWriter stringWriter = new StringWriter();
        new JSONWriter(stringWriter).object()
                .key("argument").value(argument)
                .key("id").value(u.user.getId())
                .key("nick").value(u.user.getNick())
                .key("score").value(u.score)
                .endObject();
        return stringWriter.toString();
    }

    public String sendDrawLine(DrawLine drawLine) {
        int[] pointsX = drawLine.getPointsX();
        int[] pointsY = drawLine.getPointsY();
        StringWriter stringWriter = new StringWriter();
        new JSONWriter(stringWriter).object()
                .key("argument").value("drawline")
                .key("lineX").value(pointsX)
                .key("lineY").value(pointsY)
                .key("cR").value(drawLine.getLineColor().getRed())
                .key("cG").value(drawLine.getLineColor().getGreen())
                .key("cB").value(drawLine.getLineColor().getBlue())
                .key("cA").value(drawLine.getLineColor().getAlpha())
                .key("stroke").value(drawLine.getStrokeSize())
                .key("apoints").value(pointsX.length)
                .key("delete").value(status != Status.FreeDraw)
                .endObject();
        return stringWriter.toString();
    }



    private String sendLines() {
        StringWriter stringWriter = new StringWriter();
        new JSONWriter(stringWriter).object()
                .key("argument").value("drawlines")
                .key("lines").value(getLinesJson())
                .endObject();
        return stringWriter.toString();
    }

    public JSONArray getLinesJson() {
        JSONArray jsonArray = new JSONArray();
        for (DrawLine drawLine: lines) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("lineX", drawLine.getPointsX());
            jsonObject.put("lineY", drawLine.getPointsY());
            jsonObject.put("cR", drawLine.getLineColor().getRed());
            jsonObject.put("cG", drawLine.getLineColor().getGreen());
            jsonObject.put("cB", drawLine.getLineColor().getBlue());
            jsonObject.put("cA", drawLine.getLineColor().getAlpha());
            jsonObject.put("stroke", drawLine.getStrokeSize());
            jsonObject.put("apoints", drawLine.getPointsX().length);
            jsonArray.put(jsonObject);
        }
        return jsonArray;
    }



    @Override
    public void run() {
        while (isRunning) {
            try {
                switch (status) {
                    case Stopped: {
                        isRunning = false;
                        break;
                    }
                    case CountDown: {
                        sleep(1000);
                        countdown--;
                        if (countdown <= 0) {
                            //Start the game (or next round)
                            //currentWord = "house";

                            if (words.size() <= 0) {
                                words = DrawWords.getWords();
                            }
                            System.out.println((int)(Math.random() * words.size()));
                            currentWord = words.get((int)(Math.random() * words.size()));
                            words.remove(words.indexOf(currentWord));
                            if (words.isEmpty()) words = DrawWords.getWords();
                            correctGuesses = 0;

                            countdown = DRAW_GUESS_COUNTDOWN_TIME;
                            status = Status.Drawing;
                            lines = new ArrayList<>();

                            for (DrawUser user: users) {
                                user.hasGuessed = false;
                            }

                            StringWriter stringWriter = new StringWriter();
                            new JSONWriter(stringWriter).object()
                                    .key("argument").value("startdrawing")
                                    .key("word").value(currentWord)
                                    .key("time").value(countdown)
                                    .endObject();
                            sendMessage(users.get(currentDrawer), stringWriter.toString());
                            //stringWriter = new StringWriter();
                            StringWriter stringWriter2 = new StringWriter();
                            new JSONWriter(stringWriter2).object()
                                    .key("argument").value("startguessing")
                                    .key("wordlength").value(currentWord.length())
                                    .key("time").value(countdown)
                                    .endObject();
//                        System.out.println("---------------------");
//                        System.out.println(stringWriter.toString());
//                        System.out.println(stringWriter2.toString());
//                        System.out.println("---------------------");
                            sendToAllButDrawer(stringWriter2.toString());
                        }
                        break;
                    }
                    case Drawing: {
                        sleep(1000);
                        countdown --;
                        if (countdown <= 0) {
                            countdown = START_COUNTDOWN_TIME;
                            status = Status.CountDown;
                            StringWriter stringWriter = new StringWriter();
                            new JSONWriter(stringWriter).object()
                                    .key("argument").value("endguessing")
                                    .key("word").value(currentWord)
                                    .key("correctguesses").value(correctGuesses)
                                    .key("time").value(countdown)
                                    .key("scoreboard").value(getScoreBoard())
                                    .key("users").value(getUsersJson())
                                    .endObject();
                            sendToAll(stringWriter.toString());
                            currentDrawer++;
                            if (currentDrawer >= users.size()) {
                                currentDrawer = 0;
                                //Todo: wraps back here. Reset?
                            }

                        }
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getScoreBoard() {

        DrawUser[] a = new DrawUser[users.size()];
        a = users.toArray(a);
        Arrays.sort(a);
        String toSend = "";
//        for (DrawUser u: a) {
//            toSend += "#" + u.user.getId() + " " + u.user.getNick() + " " + u.score + "\n";
//        }
        for (int i = 0; i < a.length; i ++) {
            toSend += "#" + (i + 1) + " " + a[i].user.getNick() + " " + a[i].score + "\n";
        }
        return toSend;
    }

    void sleep(int time) {
        try {
            Thread.sleep(time);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(DrawUser user, String message) {
        user.user.clientWorker.sendMessage(message);
    }

    public void sendToAll (String message) {
        for (DrawUser u: users) {
            sendMessage(u, message);
        }
    }

    public void sendToAllButDrawer(String message) {
        if (status == Status.FreeDraw) {
            sendToAll(message);
        }
        else {
            for (DrawUser u: users) {
                if (!u.equals(users.get(currentDrawer))) sendMessage(u, message);
            }
        }

    }

    private void sendToAllBut(DrawUser u, String message) {
        //System.out.println("Looking for: " + u.user.getId());
        for (DrawUser user: users) {
            //System.out.println("User id: " + user.user.getId());
            if (user.equals(u)) {
                //System.out.println("Match found... " + "User id: " + user.user.getId());
            }
            else {
                //System.out.println("Not a match, sending message");
                sendMessage(user, message);
            }
        }
    }


    public boolean canDraw(User u) {
        if (status == Status.FreeDraw) return true;
        DrawUser user = getUser(u);
        return user.equals(users.get(currentDrawer));
    }

    @Override
    public String toString() {
       return (" " + name + " | " + getGameMode() + "\n" + getScoreBoard());
    }

    @Override
    public LobbyContext getLobbyContext() {
        return context;
    }

    public void drawToDiscord() {
//        int sizeX = 1000;
//        int sizeY = 800;
//        BufferedImage paintImage = new BufferedImage(sizeX, sizeY, BufferedImage.TYPE_3BYTE_BGR);
//        Graphics g = paintImage.createGraphics();
//        Graphics2D g2 = (Graphics2D)g;
//        g2.setColor(Color.WHITE);
//        //g.setColor(Color.WHITE);
//        //g.clearRect(0, 0, sizeX, sizeY);
//        g2.fillRect(0, 0, sizeX, sizeY);
//        for (DrawLine drawLine: lines) {
//            g2.setColor(drawLine.getLineColor());
//            g2.setStroke(new BasicStroke(drawLine.getStrokeSize()));
//            g2.drawPolyline(drawLine.getPointsX(), drawLine.getPointsY(), drawLine.getPointsX().length);
//        }
//        g2.dispose();
//        g.dispose();
//        try {
//            ImageIO.write(paintImage, "PNG", new File("discord.png"));
//            Http2.sendDrawItMessage();
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }

    }

}
