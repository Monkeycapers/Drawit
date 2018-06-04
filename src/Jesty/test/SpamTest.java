package Jesty.test;

import Jesty.TCPBridge.Client;

import java.util.Scanner;

/**
 * Created by Evan on 11/10/2016.
 *
 * Tests the kahoot quiz with n amount of clients
 */
public class SpamTest extends Client {

    int id = 0;

    public SpamTest(int id) {
        super("localhost", 16000);
        this.id = id;

    }

    public static void main(String[] args) {

        //new GameServer(16000, 8080).start();
        SpamTest[] spamTests = new SpamTest[1000];
        for (int i = 0; i < spamTests.length; i++) {
            spamTests[i] = new SpamTest(i);
            spamTests[i].start();
        }
        new Scanner(System.in).nextLine();
        for (SpamTest s: spamTests) {
            s.sendMessage("Co0 TestUser" + s.id);
            try {Thread.sleep(1); } catch (Exception e) { }
        }
        System.out.println("registerd");
        new Scanner(System.in).nextLine();
        for (SpamTest s: spamTests) {
            s.sendMessage("Lo0 r" + (int)(Math.random() * 4) );
            try {Thread.sleep(1); } catch (Exception e) { }
        }
        System.out.println("registerd");
        new Scanner(System.in).nextLine();
        for (SpamTest s: spamTests) {
            s.sendMessage("Lo0 r" + (int)(Math.random() * 4));
            try {Thread.sleep(1); } catch (Exception e) { }
        }
        System.out.println("registerd");
    }
    @Override
    public void onMessage(String message) {

    }
    @Override
    public void onOpen() {

    }
    @Override
    public void onClose() {

    }



}
