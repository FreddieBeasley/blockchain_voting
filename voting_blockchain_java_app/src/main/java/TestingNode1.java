import app.LocalNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestingNode1 {
    private static final Logger logger = LoggerFactory.getLogger("Main2");

    public static void main(String[] args) throws Exception {
        System.out.println("CWD: " + System.getProperty("user.dir"));

        // initialise peer
        LocalNode localpeer1 = new LocalNode(3000, 3001, 3002);

        // start peer
        localpeer1.start();
        
    }
}