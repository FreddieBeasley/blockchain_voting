import app.LocalNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestingNode2 {
    private static final Logger logger = LoggerFactory.getLogger("Main2");

    public static void main(String[] args) throws Exception {
        System.out.println("CWD: " + System.getProperty("user.dir"));

        // initialise peer
        LocalNode localpeer2= new LocalNode("src/main/data2/blockchain.json","src/main/data2/network_manager.json", "src/main/data2/registered_voters.json", 2000, 2001, 2002);

        // start peer
        localpeer2.start();
        
    }
}