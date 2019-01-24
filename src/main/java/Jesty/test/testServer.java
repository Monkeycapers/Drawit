package Jesty.test;

import Jesty.TCPBridge.Client;
import Jesty.TCPBridge.ClientWorker;
import Jesty.TCPBridge.Server;

import java.util.Scanner;

/**
 * Created by Evan on 12/13/2016.
 */
public class testServer extends Server {

    public testServer(int raw, int web) {
        super (raw, web);
    }
    @Override
    public void onMessage(ClientWorker clientWorker, String message) {
        System.out.println("From client:" + message);
//        for (ClientWorker w: clients.getList()) {
//            w.sendMessage(message);
//        }
    }

    @Override
    public void onClose(ClientWorker clientWorker, int code) {
    }

    @Override
    public void onOpen(ClientWorker clientWorker, int code) {
        System.out.println("Welcome to the server");
    }

    public static void main (String[] args) {
        testServer test =  new testServer(16000, 8080);
        test.start();
        while (true) {
            try {
                String in = new Scanner(System.in).nextLine();
                if (in.equals("c")) {
                    System.out.println(test.clients.getList().size());
                }
                else {
                    for (ClientWorker w: test.clients.getList()) {
                        w.sendMessage(in);
                    }
                }

            }
            catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public void sendMessage(String s) {
        for (ClientWorker clientWorker: clients.getList()) {
            clientWorker.sendMessage(s);
        }
    }
}
