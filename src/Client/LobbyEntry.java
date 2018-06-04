package Client;

/**
 * Created by Evan on 7/3/2017.
 */
public class LobbyEntry {

    int id;
    String name;
    boolean isPrivate;
    int players;
    int maxPlayers;
    String status;
    String gamemode;

//    final int MAX_ID_SIZE = 8;
//    final int MAX_NAME_SIZE = 40;
//    final int MAX_PLAYERS_OVER_MAX = 10;


    public LobbyEntry(int id, String name, boolean isPrivate, int players, int maxPlayers, String status, String gamemode) {
        this.id = id;
        this.name = name;
        this.isPrivate = isPrivate;
        this.players = players;
        this.maxPlayers = maxPlayers;
        this.status = status;
        this.gamemode = gamemode;
    }

    @Override
    public String toString() {
        return "#" + id + " | " + name + " | " + players + "/" + maxPlayers + " | " + gamemode + " | " + status + " | " + (isPrivate ? "private" : "public");
        //return "#" + genPadding("" + id, 6) + " | " + genPadding(name, MAX_NAME_SIZE) + " | " + genPadding(players + "/" + maxPlayers, MAX_PLAYERS_OVER_MAX) + " | " + (isPrivate ? "private" : "public");
    }

//    public String genPadding(String def, int target) {
//        if (def.length() > target) {
//            return def.subSequence(0, target - 4) + "...";
//        }
//        while(def.length() < target) {
//            def += " ";
//        }
//        return def;
//    }

    public String[] toStringArray() {
        return new String[] {"" + id, name, "" + players + "/" + maxPlayers, (isPrivate ? "private" : "public"), status};
    }
}
