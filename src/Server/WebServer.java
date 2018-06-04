package Server;

import org.nanohttpd.webserver.SimpleWebServer;

import java.io.File;

/**
 * Created by Evan on 7/21/2017.
 */
public class WebServer extends SimpleWebServer {

    public WebServer(String host, int port) {
        //super(host, port, new File("Web"), true);
        super(host, port, new File("src/Web"), true);
        try {
            start();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
