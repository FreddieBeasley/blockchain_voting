import resources.*;

import resources.blockchain.resources.Vote; // Adding Votes
import resources.blockchain.resources.Block; // Adding Blocks


import resources.exceptions.LoadException;
import resources.exceptions.PersistenceException;
import resources.NetworkManager;
import resources.network.KnownPeers;
import org.json.JSONException;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import resources.util.Exceptions;

public class LocalPeer {
    Logger logger = LoggerFactory.getLogger(LocalPeer.class);

    private final String host;
    private final int port;

    private final Persistence persistence;
    private final NetworkManager networkManager;

    private final Blockchain blockchain; // contains pending votes


    // Initialisation
    public LocalPeer(String host, int port) throws Exception {
        // Network Information
        this.host = host;
        this.port = port;

        // Linked Modules:
        this.persistence = new Persistence(host, port);

        this.networkManager = persistence.loadNetworkManager();

        this.blockchain = persistence.loadBlockchain();

        persistState();
        logger.info("Local Peer state persisted");
    }

    // Getter
    public String getHost(){
        return host;
    }

    public int getPort(){
        return port;
    }

    public String getNodeID(){
        return getHost() +  ":" + getPort();
    }

    public List<Block> getBlockchain(){
        return blockchain.getChain();
    }

    public NetworkManager getNetworkManager(){
        return networkManager;
    }

    // Methods

    // resources.Blockchain
    public void addNewVote(Vote vote){
        blockchain.addNewVote(vote);
    }

    public void mine(){
        blockchain.createNewBlock();
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