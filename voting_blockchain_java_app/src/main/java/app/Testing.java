package app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Testing {
    private static final Logger logger = LoggerFactory.getLogger("Main2");

    public static void main(String[] args) throws Exception {
        // initialise peer
        LocalPeer localpeer = new LocalPeer(1000, 2000);

        // start web server
        localpeer.start();
        
    }
}