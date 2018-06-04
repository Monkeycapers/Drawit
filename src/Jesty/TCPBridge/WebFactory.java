package Jesty.TCPBridge;

import org.java_websocket.WebSocket;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Evan on 10/19/2016.
 *
 * Uses org.java_websocket to create a websocket server, redirects to Server
 */
public class WebFactory extends WebSocketServer {

   Clients clients;

    public WebFactory(Clients clients, int port) {
        super(new InetSocketAddress(port));
        this.clients = clients;
        //this.jServer = jServer;
    }


    public void onOpen(WebSocket client, ClientHandshake handshake) {
        //client.send("You have connected");
        //System.out.println("got a web connection: " + client.getRemoteSocketAddress().getAddress().getHostAddress());
        //jServer.clientFactory.addWebClient(client);

        //Todo: Make clients a Class and make this and clientFactory use a method in clients to add a web or raw client

        clients.add(client);

    }

    public void onClose (WebSocket client, int code, String reason, boolean remote) {
        //this.sendToAll("lost a client.");
        System.out.println(code + ", " + reason + ", " + remote);
        //jServer.removeClient(jServer.getClient(client).id);
        clients.get(client).disconnect();

    }

    public void onMessage(WebSocket client, String message) {
        clients.get(client).receiveMessage(message);
    }

    public void onFragment(WebSocket client, Framedata fragment) {

    }

    public void onError(WebSocket client, Exception ex) {
        ex.printStackTrace();
    }

    //public void sendToAll( String text ) {
       // Collection<WebSocket> con = connections();
        //synchronized ( con ) {
        //    for( WebSocket c : con ) {
        //        c.send( text );
        //    }
      //  }

    //}


}
