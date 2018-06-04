package Server;

import org.mindrot.jbcrypt.BCrypt;

public class JumpRequest {

    final int EXPIRE_TIME = 20000;

    private User user;
    private Lobby lobby;
    private String token;
    //In seconds
    private long expirery;

    public JumpRequest(User user, Lobby lobby, long expirery) {
        this.user = user;
        this.lobby = lobby;
        this.expirery = expirery;
        generateToken();
    }

    private void generateToken() {
        token = BCrypt.hashpw(user.getNick() + user.getId() + System.currentTimeMillis(), BCrypt.gensalt());
    }

    //Todo: never allow token to escape, make a method to check with given token instead
    public String getToken() {
        return token;
    }

    public boolean expired() {
        //todo: fix
        System.out.println("Time to jump: " + (System.currentTimeMillis() - expirery));
        //return false;
        return (System.currentTimeMillis() - expirery) > EXPIRE_TIME;
    }

    public User getUser() {
        return user;
    }

    public Lobby getLobby() {
        return lobby;
    }

}
