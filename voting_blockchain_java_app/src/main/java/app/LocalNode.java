package app;

import app.resources.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.resources.JSONParsers.*;

import app.resources.blockchain.resources.Block;
import app.resources.blockchain.resources.Vote;

import app.resources.exceptions.PersistenceException;

import java.io.IOException;

public class LocalNode {
    // Fields
    Logger logger = LoggerFactory.getLogger(LocalNode.class);

    // Network Manager
    String host = "127.0.0.1";

    // Modules
    private final Persistence persistence; // sends all data to database
    private final NetworkManager networkManager; // Communicates with other nodes
    private Blockchain blockchain; // Hold election information immutably ( not final for consensus )
    private final WebServer webServer; // Communicated with webpage
    private final ControlServer controlServer; // Allow owner to interact with the blockchain


    // Initialisation
    public LocalNode(int peerPort, int webPort, int hostPort) {
        // Network Information

        // Linked Modules:
        this.persistence = new Persistence(host, peerPort, this);

        this.networkManager = persistence.loadNetworkManager();

        this.blockchain = persistence.loadBlockchain();

        this.webServer = new WebServer(host, webPort, this);

        this.controlServer = new ControlServer(host, hostPort, this);

        persistState();
    }

    public LocalNode(String persistentBlockchain, String persistentNetworkManager, String registeredVoters, int peerPort, int webPort, int hostPort) {
        // Network Information

        // Linked Modules:
        this.persistence = new Persistence(persistentBlockchain, persistentNetworkManager, registeredVoters, host, peerPort, this);

        this.networkManager = persistence.loadNetworkManager();

        this.blockchain = persistence.loadBlockchain();

        this.webServer = new WebServer(host, webPort, this);

        this.controlServer = new ControlServer(host, hostPort, this);

        persistState();
    }

    // Getter Methods - for Control server
    public JSONObject getBlockchainJSON(){
        return BlockchainParser.BlockchainToJSON(blockchain);
    }

    public JSONObject getNetworkPeersJSON(){
        return NetworkParser.knownPeersToJSON(networkManager.getKnownPeers());
    }

    public void connectToNewPeer(String host, int port){
        networkManager.formulateOutgoingConnectionRequest(host, port);
    }

    // main method
    public void start(){
        // ( Runs Voting and Registering Server )
        Thread webThread = new Thread(webServer::start, "WebServer-Thread");
        webThread.start();

        // ( Runs Network Communication Server )
        Thread networkThread = new Thread(networkManager::start, "Network-Thread");
        networkThread.start();

        // ( Runs Control Server )
        Thread controlThread = new Thread(controlServer::start, "ControlServer-Thread");
        controlThread.start();

        // ( Requests Blockchain From Peer Every Minute )
        Thread consensusThread = new Thread(() -> {
            while (true) {
                try {
                    //noinspection BusyWait
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    logger.error("Error with consensus thread: ", e);
                }
                networkManager.formulateOutgoingBlockchainRequest();
            }
        }, "Consensus-Thread");
        consensusThread.start();

        // ( Persists Blockchain Every Minute )
        Thread persistenceThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    logger.error("Error with persistence thread: ", e);
                }
                persistState();
            }
        }, "Persistence-Thread");
        persistenceThread.start();

        // ( Attempts Mining Every Minute )
        Thread miningThread = new Thread(() -> {
            while (true) {
                try {
                    //noinspection BusyWait
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    logger.error("Error with mining thread: ", e);
                }
                blockchain.createNewBlock();
            }
        }, "Mining-Thread");
        miningThread.start();

        try {
            webThread.join();
        } catch (InterruptedException e) {
            logger.error("Startup interrupted", e);
            Thread.currentThread().interrupt();
        }

    }

    // Handling created data
    public void handleWebVote(Vote vote){
        logger.info("Vote received from web server");
        logger.info("Vote sent to blockchain pending votes");
        Thread blockchainThread = new Thread(() -> {

            blockchain.handleNewVote(vote);

        });
        blockchainThread.start();

        logger.info("Vote sent to network for distribution");
        Thread networkThread = new Thread(() -> {
            networkManager.formulateOutgoingVote(vote);
        });
        networkThread.start();
    }

    public void handleWebVoter(String voter){
        logger.info("New voter received from web server");
        logger.info("New voter sent to blockchain for remaining voters");
        sendVoterToBlockchain(voter);

        logger.info("new voter sent to network for distribution");
        Thread networkThread = new Thread(() -> {

            networkManager.formulateOutgoingVoter(voter);
        });
        networkThread.start();

    }

    public void handleNewBlock(Block block){
        logger.info("New block received from blockchain");
        logger.info("New block sent to network for distribution");
        Thread networkThread = new Thread(() -> {
            networkManager.formulateOutgoingBlock(block);
        });
        networkThread.start();


    }

    // Helper methods for handleWebVoter and handleNetworkVoter
    private void sendVoterToBlockchain(String voter){
        // Must be done first
        try {
            persistence.addVoterToRegisteredVoters(voter);
        } catch (IOException e) {
            logger.warn("Error adding voter to registered voters: ", e);
            return;
        }


        Thread blockchainThread = new Thread(() -> {
            blockchain.handleNewVoter(voter);
        });

        blockchainThread.start();
    }


    // Handling received data
    public void handleNetworkVote(Vote vote){
        logger.info("Vote received from network");
        logger.info("Vote sent to blockchain pending votes");
        Thread networkThread = new Thread(() -> {
            blockchain.handleNewVote(vote);
        });
        networkThread.start();
    }

    public void handleNetworkVoter(String voter){
        sendVoterToBlockchain(voter);
    }

    public void handleNetworkBlock(Block block){
        logger.info("Block received from network");
        logger.info("Block sent to blockchain for review");
        Thread blockchainThread = new Thread(() -> {
            blockchain.handleNewBlock(block);
        });
        blockchainThread.start();
    }

    // ( Performing consensus mechanism )

    public void handleNetworkBlockchain(Blockchain newBlockchain) {
        logger.info("New blockchain received from network");
        if(newBlockchain.getLength() < blockchain.getLength()){
            logger.info("New blockchain discarded: length shorter");
            return;
        }

        if(newBlockchain.getDifficulty() < blockchain.getDifficulty()){
            logger.info("New blockchain discarded: difficulty shorter");
            return;
        }

        if((newBlockchain.getLength() == blockchain.getLength()) && (newBlockchain.getDifficulty() == blockchain.getDifficulty())){
            logger.info("New blockchain discarded: difficulty and length equal");
            return;
        }

        logger.info("New blockchain accepted");
        blockchain = newBlockchain;
    }

    // NetworkConnectionHandled in NetworkManager

    // Persistence
    public void persistBlockchain() throws PersistenceException {
        persistence.persistBlockchain(blockchain);
    }

    public void persistNetworkManager() throws PersistenceException {
        persistence.persistNetworkManager(networkManager);
    }

    public void persistState(){
        try {
            persistBlockchain();
            persistNetworkManager();
            logger.info("Successfully persisted node");
        }  catch (PersistenceException e) {
            logger.error("Error persisting node: " + e.getMessage());
        }
    }

}
