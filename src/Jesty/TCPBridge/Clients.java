package Jesty.TCPBridge;

import org.java_websocket.WebSocket;

import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by Evan on 10/20/2016.
 *
 * A class that contains a arraylist of clients, including util methods
 */
public class Clients {

    private ArrayList<ClientWorker> clients;

    private int id;

    private Server server;

    public Clients(Server server) {
        this.id = 0;
        clients = new ArrayList<ClientWorker>();
        this.server = server;
    }

    public ArrayList<ClientWorker> getList() {
        return clients;
    }

    public ClientWorker get(int clientId) {
        for (ClientWorker client: clients) {
            if (client.id == clientId) {
                return client;
            }
        }
        return null;
    }

    public void add(Socket socket) {
        try {
            ClientWorker w;
            w = new ClientWorker(id, socket, server);
            clients.add(w);
            w.t = new Thread(w);
            w.t.start();
            w.connect();
            id ++;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void add(WebSocket socket) {
        try {
            ClientWorker w;
            w = new ClientWorker(id, socket, server);
            clients.add(w);
            w.connect();
            id ++;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ClientWorker get(ClientWorker clientWorker) {
        for (ClientWorker client: clients) {
            if (client.equals(clientWorker)) {
                return client;
            }
        }
        return null;
    }

    public void set(ArrayList<ClientWorker> clients) {
        this.clients = clients;
    }

    public ClientWorker get( WebSocket webSocket) {
        for (ClientWorker c: clients) {
            if (c.socketType == SocketType.WEB_SOCKET) {
                if (c.webSocket.equals(webSocket)) {
                    return c;
                }
            }
        }
        return null;
    }

    public void remove(ClientWorker clientWorker) {
        clients.remove(clientWorker);
    }

}
