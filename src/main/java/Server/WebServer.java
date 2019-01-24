package Server;

import fi.iki.elonen.SimpleWebServer;

import java.io.File;

/**
 * Created by Evan on 7/21/2017.
 */
public class WebServer extends SimpleWebServer {

    public WebServer(String host, int port) {
        //super(host, port, new File("Web"), true);
        super(host, port, new File("src/main/java/Web"), true);
        try {
            start();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
