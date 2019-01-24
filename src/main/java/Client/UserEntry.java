package Client;

/**
 * Created by Evan on 7/6/2017.
 */
public class UserEntry {

    String nick;
    int id;
    int score;

    public UserEntry() {
        nick = "";
        id = 0;
        score = 0;
    }

    public UserEntry(String nick, int id, int score) {
        this.nick = nick;
        this.id = id;
        this.score = score;
    }

    public String[] toStringArray() {
        return new String[] {"" + id, nick, "" + score};
    }
}
