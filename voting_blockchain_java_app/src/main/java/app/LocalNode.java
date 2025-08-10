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
        logger.info("Local Peer state persisted");
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

    // Methods - From WebServer
    public void start(){
        Thread webThread = new Thread(webServer::start);
        webThread.start();

        Thread networkThread = new Thread(networkManager::start);
        networkThread.start();

        Thread controlThread = new Thread(controlServer::start);
        controlThread.start();

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
        });
        consensusThread.start();

        Thread persistenceThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    logger.error("Error with persistence thread: ", e);
                }
                persistState();
            }
        });
        persistenceThread.start();

        // want a thread that attempts consensus every 30 seconds ( for testing, in reality every 5 minutes )

        try {
            webThread.join();
        } catch (InterruptedException e) {
            logger.error("Startup interrupted", e);
            Thread.currentThread().interrupt();
        }

    }

    public void handleWebVote(Vote vote){
        logger.info("Vote received from web server");
        Thread blockchainThread = new Thread(() -> {
            logger.info("Vote sent to blockchain pending votes");
            blockchain.handleNewVote(vote);

        });
        blockchainThread.start();

        Thread networkThread = new Thread(() -> {
            logger.info("Vote sent to network for distribution");
            networkManager.formulateOutgoingVote(vote);
        });
        networkThread.start();
    }

    public void handleWebVoter(String voter){
        logger.info("New voter received from web server");

        sendVoterToBlockchain(voter);

        Thread networkThread = new Thread(() -> {
            logger.info("Voter sent to network for distribution");
            networkManager.formulateOutgoingVoter(voter);
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
            logger.info("Voter sent to blockchain");
            blockchain.handleNewVoter(voter);
        });

        blockchainThread.start();
    }


    // Methods - From Blockchain
    public void handleNewBlock(Block block){
        logger.info("New block received from blockchain");
        Thread networkThread = new Thread(() -> {
            logger.info("New block sent to network for distribution");
            networkManager.formulateOutgoingBlock(block);
            persistBlockchain(); // handleNewBlock() only called when new block added to chain
        });
        networkThread.start();


    }

    // Methods - Received from network
    public void handleNetworkVote(Vote vote){
        logger.info("Vote received from network");
        Thread networkThread = new Thread(() -> {
            logger.info("Vote sent to blockchain pending votes");
            blockchain.handleNewVote(vote);
            persistBlockchain();
        });
        networkThread.start();
    }

    public void handleNetworkVoter(String voter){
        sendVoterToBlockchain(voter);
    }

    public void handleNetworkBlock(Block block){
        logger.info("Block received from network");
        Thread blockchainThread = new Thread(() -> {
            logger.info("Block sent to blockchain");
            blockchain.handleNewBlock(block);
        });
        blockchainThread.start();
    }

    // ( Performing consensus mechanism )
    public void handleNetworkBlockchain(Blockchain newBlockchain) {
        logger.info("New blockchain received from blockchain");
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
        persistBlockchain();
    }

   // Persistence
    public void persistBlockchain(){
        try {
            persistence.persistBlockchain(blockchain);
            logger.info("Blockchain persisted");
        } catch (PersistenceException e) {
            logger.error("Persistence error with NetworkManager: ", e);
        }
    }

    public void persistNetworkManager(){
        try {
            persistence.persistNetworkManager(networkManager);
            logger.info("NetworkManager persisted");
        } catch (PersistenceException e) {
            logger.error("Persistence error with NetworkManager: ", e);
        }
    }

    public void persistState(){
        persistBlockchain();
        persistNetworkManager();
    }

}
