package Jesty.TCPBridge;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by Evan on 11/3/2016.
 *
 * A client class to connect with a Server.
 * Extend it to use it in your project
 *
 *
 */
public abstract class Client implements Runnable {

    private String hostName;
    private int portNumber;

    private DataOutputStream out;

    private boolean isRunning = false;


    public Client(String hostName, int portNumber) {
        this.hostName = hostName;
        this.portNumber = portNumber;
    }

    public Client() {
        this.hostName = "localhost";
        this.portNumber = 16000;
    }

    //Creates a new thread that handles server connections
    public void start() {
        new Thread(this).start();
    }
    //

    //Run this thread. Only returns when the server disconnects
    public void waitStart() {
        new Thread(this).run();
    }
    //

    public void run() {
        isRunning = true;
        try (
                Socket echoSocket = new Socket(hostName, portNumber);     //new InputStreamReader(System.in))
                OutputStream outToServer = echoSocket.getOutputStream();
                DataOutputStream out = new DataOutputStream(outToServer);
                InputStream inFromServer = echoSocket.getInputStream();
                DataInputStream in = new DataInputStream(inFromServer);
        ) {
            //Setup Code
            this.out = out;
            onOpen();
            while (isRunning) {
                //Read message in
                String strIn = in.readUTF();
                onMessage(strIn);
            }
        }
        catch (Exception e) {
            onClose();
        }
    }

    public abstract void onMessage(String message);


    public abstract void onOpen();


    public abstract void onClose();

    public boolean sendMessage(String message) {
        try {
            out.writeUTF(message);
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
