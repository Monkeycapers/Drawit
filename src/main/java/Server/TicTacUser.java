package Server;

public class TicTacUser {

    User user;

    char display;

    int score;

    public TicTacUser(User user, char display) {
        this.user = user;
        this.display = display;
        score = 0;
    }
}
