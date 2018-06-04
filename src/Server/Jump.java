package Server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//Handles jumping to another page in order to join a lobby.
public class Jump {

    private List<JumpRequest> requests;

    public Jump() {
        requests = new ArrayList<>();
    }

    public JumpRequest getRequest(String token) {
        Iterator<JumpRequest> i = requests.iterator();
        while (i.hasNext()) {
            JumpRequest r = i.next();
            if (r.expired()) {
                i.remove();
            }
            else if (r.getToken().equals(token)) {
                i.remove();
                return r;
            }
        }
        return null;
    }

    public String addRequest(User user, Lobby lobby) {
        //Todo: Check for other requests involving users
        JumpRequest r = new JumpRequest(user, lobby, System.currentTimeMillis());
        requests.add(r);
        return r.getToken();
    }

    public void cleanUp() {

    }


    public List<JumpRequest> getList() {
        return requests;
    }
}
