package Jesty.TCPBridge;

import java.net.InetAddress;
import java.util.ArrayList;


/**
 * Created by Evan on 10/19/2016.
 *
 * A Server class that can handle and send messages to multiple clients
 * Extend this to use it in your project
 */
public abstract class Server {

    protected Clients clients;

    int raw_port, web_port;

    public Server(int raw_port, int web_port) {
        this.web_port = web_port;
        this.raw_port = raw_port;
    }

    public Server(int port, boolean serverType) {
        if (serverType) web_port = port;
        else raw_port = port;
    }

    public Server() {
        raw_port = 16000; web_port = 8080;
    }


    public void start() {
        if (raw_port > 0) {
            clients = new Clients(this);
            ClientFactory clientFactory = new ClientFactory(clients, raw_port);
            new Thread(clientFactory).start();
        }
        if (web_port > 0) {
            WebFactory webFactory = new WebFactory(clients, web_port);
            webFactory.start();
        }
        try {
            //Cut of the PC name part of the ip address
            System.out.println("The IP Address of the Server: " + InetAddress.getLocalHost().toString().split("/")[1]);
            System.out.println("Raw port: " + raw_port);
            System.out.println("Web port: " + web_port);
        }
        catch (Exception e) {
            System.out.println("The IP Address of the Server: localhost");
        }
        //
    }

    public abstract void onMessage(ClientWorker clientWorker, String message);
    public abstract void onClose(ClientWorker clientWorker, int code);
    public abstract void onOpen(ClientWorker clientWorker, int code);

}
