package app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.resources.Blockchain;
import app.resources.NetworkManager;
import app.resources.Persistence;
import app.resources.WebServer;
import app.resources.blockchain.resources.Block;
import app.resources.blockchain.resources.Vote;
import app.resources.exceptions.PersistenceException;
import java.util.List;

public class LocalPeer {
    // Fields
    Logger logger = LoggerFactory.getLogger(LocalPeer.class);

    // Network Manager
    private final String host;
    private final int peerPort;

    // Web Server
    private final int webPort;

    // Modules
    private final Persistence persistence; // sends all data to database
    private final NetworkManager networkManager; // Sends votes, blocks from peer to blockchain, sends nodes to knownNodes
    private final Blockchain blockchain; // contains pending votes, remaining voters and blockchain
    private final WebServer webServer; // Sends votes from webpage to blockchain


    // Initialisation
    public LocalPeer(int peerPort, int webPort) {
        // Network Information
        this.host = "localhost";
        this.peerPort = peerPort;
        this.webPort = webPort;

        // Linked Modules:
        this.persistence = new Persistence(host, peerPort, this);

        this.networkManager = persistence.loadNetworkManager();

        this.blockchain = persistence.loadBlockchain();

        this.webServer = new WebServer(host, webPort, this);

        persistState();
        logger.info("Local Peer state persisted");
    }

    // Getter
    public String getHost(){
        return host;
    }

    public int getPeerPort(){
        return peerPort;
    }

    public int getWebPort(){
        return webPort;
    }

    public String getNodeID(){
        return getHost() +  ":" + getPeerPort();
    }

    public List<Block> getBlockchain(){
        return blockchain.getChain();
    }

    public NetworkManager getNetworkManager(){
        return networkManager;
    }

    // Methods
    public void handleNewVote(Vote vote){
        blockchain.addNewVote(vote);
        logger.info("New vote added to pending votes: " + vote.toString());
    }

    // resources.Blockchain
    public void start(){
        Thread peerThread = new Thread(() -> networkManager.start());
        Thread webThread = new Thread(() -> webServer.start());

        peerThread.start();
        webThread.start();

    }

    public void persistState(){
        for (int i = 0; i<3; i++) {
            try {
                persistence.persistBlockchain(blockchain);
                break;
            } catch (PersistenceException e) {
                if (i == 2) {
                    logger.warn("Unable to persist blockchain: " + e.getMessage());
                }
            }
        }

        for (int i = 0; i<3; i++) {
            try {
                persistence.persistNetworkManager(networkManager);
                break;
            } catch (PersistenceException e) {
                if (i == 2) {
                    logger.warn("Unable to persist known peers: " + e.getMessage());
                }
            }
        }
    }
}
