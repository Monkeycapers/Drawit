package Jesty.test;

import Jesty.TCPBridge.Client;

import java.util.Scanner;


/**
 * Created by Evan on 12/13/2016.
 */
public class testClient extends Client {

    public testClient(String host, int port) {
        super(host, port);
    }
    @Override
    public void onMessage(String message) {
        System.out.println("From server: " + message);
    }

    @Override
    public void onOpen() {
        System.out.println("Connected");
    }
    @Override
    public void onClose() {
        System.out.println("Server closed");
    }

    public static void main(String[] args) {
        testClient testClient = new testClient("localhost", 16000);
        testClient.start();
        while (true) {
            String in = new Scanner(System.in).nextLine();
            testClient.sendMessage(in);
        }
    }
}
