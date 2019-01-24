package Jesty.TCPBridge;

import java.net.ServerSocket;
import java.util.ArrayList;

/**
 * Created by S199753733 on 10/18/2016.
 *
 * Handles raw tcp/ip connections, assigning new clientworker threads to the incoming client
 */


public class ClientFactory implements Runnable  {

    Clients clients;

    private ServerSocket serverSocket;

    int portNumber;

    boolean isRunning;

    int id;

    public ClientFactory(Clients clients, int portNumber) {
        this.clients = clients;
        this.portNumber = portNumber;

    }

    public void run() {
        id = 0;
        isRunning = true;
        serverSocket = null;
        try {
            serverSocket = new ServerSocket(portNumber);
        }
        catch (Exception e) {
            System.err.println("Could not create a socket");
        }
        //Logger
        while (isRunning) {
            try {
                clients.add(serverSocket.accept());
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
