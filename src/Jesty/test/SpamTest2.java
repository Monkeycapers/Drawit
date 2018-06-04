package Jesty.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Evan on 12/13/2016.
 */
public class SpamTest2 extends testClient {

    public static List<testClient> testClients;

    public SpamTest2(String host, int port) {
        super(host, port);
        start();
    }

    public static void main(String[] args) {
        testClients = new ArrayList<>();
        for (int x = 0; x < 1000; x++) {
            System.out.println("Batch " + x);
            for (int i = 0; i < 1000; i++) {
                testClients.add(new SpamTest2("localhost", 16000));
            }
            //try {Thread.sleep(100);}catch (Exception e) {}
            new Scanner(System.in).nextLine();
        }
    }
}
