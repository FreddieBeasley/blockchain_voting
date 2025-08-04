package network;

import blockchain.Block;
import blockchain.Blockchain;


import blockchain.Vote;
import exceptions.LoadException;
import exceptions.PersistenceException;
import org.json.JSONException;
import org.json.JSONObject;
import util.CryptographyUtils;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.ExceptionUtils;

public class LocalPeer {
    Logger logger = LoggerFactory.getLogger(LocalPeer.class);

    private final String host;
    private final int port;

    private final Persistence persistence;
    private final NetworkManager networkManager;

    private final Blockchain blockchain; // contains pending votes
    private final KnownPeers knownPeers;


    // Initialisation
    public LocalPeer(String host, int port) throws Exception {
        // Network Information
        this.host = host;
        this.port = port;

        logger.info("Peer Initialising with: " + getNodeID());

        // Linked Modules:
        this.persistence = new Persistence(); // Used to store other modules
        this.networkManager = new NetworkManager(host, port);

        // Load Blockchain
        Blockchain tempBlockchain;
        try {
            tempBlockchain = persistence.attemptBlockchainLoad();
            logger.info("Blockchain Load Successful");
        } catch (IOException | LoadException e) {
            logger.warn("Failed to recreate existing blockchain" + ExceptionUtils.buildExceptionChain(e));


            try {
                tempBlockchain = persistence.attemptBlockchainCreationWithRegisteredVoters();
                logger.info("Blockchain Creation Successful with RegisteredVoters");
            } catch (IOException | JSONException ex) {
                logger.warn("Failed to create blockchain with registered voters" + ExceptionUtils.buildExceptionChain(ex));

                tempBlockchain = new Blockchain();
                logger.info("Blockchain Creation Successful without RegisteredVoters");
            }
        }

        this.blockchain = tempBlockchain;

        // Load knownPeers
        KnownPeers tempKnownPeers;
        try {
            tempKnownPeers = persistence.attemptPeersLoad();
            logger.info("Peer Load Successful");
        } catch (IOException | JSONException ex) {
            logger.warn("Failed to load known peers" + ExceptionUtils.buildExceptionChain(ex));
            tempKnownPeers = new KnownPeers();
        }

        this.knownPeers = tempKnownPeers;

        persistState(); // updates potentially corrupted blockchain with newly create blockchain
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

    public KnownPeers getKnownPeers(){
        return knownPeers;
    }

    // Methods

    // Blockchain
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
                persistence.persistKnownPeers(knownPeers);
                break;
            } catch (PersistenceException e) {
                if (i == 2) {
                    logger.warn("Unable to persist known peers: " + e.getMessage());
                }
            }
        }
    }
}