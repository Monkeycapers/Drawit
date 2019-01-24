package Server;

import org.json.JSONObject;
import org.json.JSONWriter;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class TicTacLobby extends Lobby {

    TicTacContext context;

    List<TicTacUser> users;

    int currentPlayer;

    char[][] board;

    boolean inGame;

    public TicTacLobby(TicTacContext context, User user, String name, String pass, int id, String lobbyToken ) {
        this(context, user, name, id, lobbyToken);
        this.password = pass;
    }

    public TicTacLobby(TicTacContext context, User user, String name, int id, String lobbyToken ) {
        this.context = context;
        users = new ArrayList<>();
        TicTacUser ticTacUser = new TicTacUser(user, 'X');
        users.add(ticTacUser);
        this.maxSize = 2;
        this.name = name;
        this.id = id;
        this.lobbyToken = lobbyToken;
        inGame = false;
    }

    @Override
    public void onConnect(User user) {
        TicTacUser ticTacUser = new TicTacUser(user, getOppositeChar(users.get(0).display));
        users.add(ticTacUser);
        startgame();

        //start();
    }

    public void startgame() {
        reset();
        inGame = true;
        users.get(1).user.clientWorker.sendMessage(getStartMessage(users.get(1).display, false));
        users.get(0).user.clientWorker.sendMessage(getStartMessage(users.get(0).display, true));
    }

    private String getStartMessage(char display, boolean isSelecting) {
        StringWriter stringWriter = new StringWriter();
        new JSONWriter(stringWriter).object()
                .key("argument").value("start")
                .key("display").value(display)
                .key("selecting").value(isSelecting)
                .endObject();
        return stringWriter.toString();
    }

    private String getNextTurnMessage(boolean isSelecting, char display, int x, int y) {
        StringWriter stringWriter = new StringWriter();
        new JSONWriter(stringWriter).object()
                .key("argument").value("turn")
                .key("display").value(display)
                .key("selecting").value(isSelecting)
                .key("x").value(x)
                .key("y").value(y)
                .endObject();
        return stringWriter.toString();
    }

    private String getEndMessage(int type, char display, int x, int y) {
        StringWriter stringWriter = new StringWriter();
        new JSONWriter(stringWriter).object()
                .key("argument").value("end")
                .key("type").value(type)
                .key("display").value(display)
                .key("finalx").value(x)
                .key("finaly").value(y)
                .endObject();
        return stringWriter.toString();
    }

    @Override
    public boolean onClose(User user) {
        TicTacUser ticTacUser = getUserById(user.getId());
        if (ticTacUser == null) return false;
        users.remove(ticTacUser);
        inGame = false;
        return users.isEmpty();
    }

    @Override
    public void onMessage(User user, JSONObject input) {
        TicTacUser ticTacUser = getUserById(user.getId());
        switch (input.getString("type")) {
            case "selection": {
                if (!inGame) break;
                if (users.indexOf(ticTacUser) != currentPlayer) break;
                int ix = input.getInt("x");
                int iy = input.getInt("y");
                //Bounds check
                if (ix < 0 || ix > 2) break;
                if (iy < 0 || iy > 2) break;
                if (board[ix][iy] != ' ') break;
                board[ix][iy] = ticTacUser.display;
                char winningChar = ' ';
                //Todo: redo with better assumptions (check only possible win conditons based on selection rather than all)
                for (int i = 0; i < board.length; i ++) {
                    String ySum = "";
                    String xSum = "";

                    for (int x = 0; x < board[i].length; x ++) {
                       ySum += board[i][x];
                       xSum += board[x][i];
                    }

                    if (checkSum(xSum.toCharArray())) {
                        winningChar = xSum.charAt(0);
                        break;
                    }
                    if (checkSum(ySum.toCharArray())) {
                        winningChar = ySum.charAt(0);
                        break;
                    }
                    //if (checkSum(ySum)) set winningchar and break;
                    //checkSum(xSum);
                }

                if (winningChar == ' ') {
                    //Check diagonals
                    String diagSum1 = "";
                    String diagSum2 = "";
                    for (int i = 0; i < board.length; i ++) {
                        diagSum1 += board[i][i];
                        diagSum2 += board[(board.length - 1) - i][(board.length - 1) - i];
                    }
                    if (checkSum(diagSum1.toCharArray())) {
                        winningChar = diagSum1.charAt(0);
                    }
                    if (checkSum(diagSum2.toCharArray())) {
                        winningChar = diagSum2.charAt(0);
                    }

                }

                if (winningChar == ' ') {
                    //Check for tie
                    boolean isTie = true;
                    for (int i = 0; i < board.length && isTie; i ++) {
                        for (int x = 0; x < board[i].length; x ++) {
                            isTie = (board[i][x] != ' ');
                            if (!isTie) break;
                        }
                    }
                    if (isTie) {
                        //Todo: a tie....
                        sendToAll(getEndMessage(0, ticTacUser.display, ix, iy));
                        //Todo: countdown...
                        //Todo: rotate starting player
                        startgame();
                        break;
                    }
                    else {
                        //Todo: send updated board to other player and make it his turn
                        sendMessage(ticTacUser, getNextTurnMessage(false, ticTacUser.display, ix, iy));
                        if (currentPlayer == 1) {
                            currentPlayer = 0;
                        }
                        else {
                            currentPlayer = 1;
                        }
                        sendMessage(users.get(currentPlayer), getNextTurnMessage(true, ticTacUser.display, ix, iy));
                    }
                }
                else if (winningChar == 'X' || winningChar == 'O') {
                    //has to be the user who just went
                    System.out.println(winningChar + " Won!");
                    sendMessage(ticTacUser, getEndMessage(1, ticTacUser.display, ix, iy));
                    if (currentPlayer == 1) sendMessage(users.get(0), getEndMessage(-1, ticTacUser.display, ix, iy));
                    else sendMessage(users.get(1), getEndMessage(1, ticTacUser.display, ix, iy));
                    startgame();
                }
                break;
            }
            case "chat": {
                String message = "#" + user.getId() + " " + user.getNick() + ": " + context.inputPolicy.sanitize(input.getString("message"));
                StringWriter stringWriter = new StringWriter();
                new JSONWriter(stringWriter).object()
                        .key("argument").value("chat")
                        .key("message").value(message)
                        .key("userid").value(user.getId())
                        .endObject();
                sendMessage(ticTacUser, stringWriter.toString());
                break;
            }
        }
    }

    private void sendToAll(String message) {
        for (TicTacUser user: users) {
            sendMessage(user, message);
        }
    }

    public boolean checkSum(char[] sum) {
        for (int i = 1; i < sum.length; i ++) {
            if (sum[i] != sum[0]) return false;
        }
        return true;
    }

    public void sendMessage(TicTacUser user, String message) {
        user.user.clientWorker.sendMessage(message);
    }

    public void reset() {
        for (TicTacUser user: users) {
            user.score = 0;
        }
        //Todo: alternate who gets to start
        currentPlayer = 0;
        board = new char[3][3];
        System.out.println("resetting board");
        for (int i = 0; i < board.length; i ++) {
            for (int x = 0; x < board[i].length; x++) {
                board[i][x] = ' ';
            }
        }
    }

    public TicTacUser getUserById(int id) {
        for (TicTacUser u: users) {
            if (u.user.getId() == id) {
                return u;
            }
        }
        return null;
    }

    public char getOppositeChar(char d) {
        if (d == 'X') return 'O';
        return 'X';
    }

    @Override
    public void pause() {
        //todo: time limits
    }

    @Override
    public String getGameMode() {
        return "TicTacToe";
    }

    @Override
    public String getStatus() {
        return inGame ? "In Game: " + users.get(currentPlayer).display + "'s turn" : "Waiting for players...";
    }

    @Override
    public boolean canConnect(User user) {
        return users.size() < 2;
    }

    @Override
    public List<User> getUsers() {
        List<User> u = new ArrayList<>();
        for (TicTacUser user: users) {
            u.add(user.user);
        }
        return u;
    }

    @Override
    public int getPlayerCount() {
        return users.size();
    }

    @Override
    public LobbyContext getLobbyContext() {
        return context;
    }

    @Override
    public void run() {
        //todo: time limits
    }

}
