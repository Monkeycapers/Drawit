package Server;

/**
 * Created by Evan on 7/1/2017.
 */
public class DrawUser implements Comparable {

    User user;

    int score;

    //TYPE:
    /*
     0: Player
     1: Spectator
     2: Admin
     */

    int type;

    boolean hasGuessed;

    public DrawUser (User user) {
        this.user = user;
        this.type = 0;
    }

    @Override
    public int compareTo(Object o) {
        DrawUser u = (DrawUser)o;
        if (score < u.score) return 1;
        else if (score > u.score) return -1;
        return 0;
    }
}
