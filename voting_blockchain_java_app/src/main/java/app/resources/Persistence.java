package app.resources;

import app.LocalNode;

import app.resources.exceptions.InvalidException;
import app.resources.exceptions.LoadException;
import app.resources.exceptions.MalformedJSONException;
import app.resources.exceptions.PersistenceException;
import app.resources.network.MessageCache;
import app.resources.util.Exceptions;
import app.resources.network.KnownPeers;
import app.resources.util.FileHandlers;

import app.resources.JSONParsers.BlockchainParser;
import app.resources.JSONParsers.NetworkParser;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

public class Persistence {

    // TEMP - Needed for NetworkManager
    private final String host;
    private final int port;
    private final LocalNode localNode;

    // Fields
    private final File persistentBlockchain; // resources.Blockchain

    private final File persistentNetworkManager; // resources.Blockchain

    private final File registeredVoters; // resources.Blockchain

    private final Logger logger; // logging

    // Initialisation
    public Persistence(String host, int port, LocalNode localNode) { // default initialisation
        this.host = host;
        this.port = port;
        this.localNode = localNode;

        this.persistentBlockchain = new File("src/main/data/blockchain.json");

        this.persistentNetworkManager = new File("src/main/data/network_manager.json");

        this.registeredVoters = new File("src/main/data/registered_voters.json");

        this.logger = LoggerFactory.getLogger(Persistence.class);
    }

    public Persistence(String persistentBlockchain, String persistentNetworkManager, String registeredVoters, String host, int port, LocalNode localNode) { // custom initialisation
        this.host = host;
        this.port = port;
        this.localNode = localNode;

        this.persistentBlockchain = new File(persistentBlockchain);

        this.persistentNetworkManager = new File(persistentNetworkManager);

        this.registeredVoters = new File(registeredVoters);

        this.logger = LoggerFactory.getLogger(Persistence.class);
    }

    // Blockchain Load
    private Blockchain attemptBlockchainLoad() throws LoadException {
        JSONObject blockchainJson;
        try {
            blockchainJson = (JSONObject) FileHandlers.readFromJSONFile(persistentBlockchain.getPath());
        } catch (IOException e) {
            throw new LoadException("data/blockchain is missing or empty", e);
        } catch (JSONException e) {
            throw new LoadException("data/blockchain is malformed", e);
        }

        if (blockchainJson == null) {
            throw new LoadException("data/blockchain is missing or empty");
        }

        Blockchain newBlockchain;

        try {
            newBlockchain = BlockchainParser.JSONToBlockchain(blockchainJson, localNode); // Throws MalformedJSONException
        } catch (MalformedJSONException e) {
            throw new LoadException("data/blockchain is malformed", e);
        }

        try {
            newBlockchain.isValid(); // Throws InvalidBlockchainException
        } catch (InvalidException e) {
            throw new LoadException("Loaded blockchain is invalid", e);
        }

        return newBlockchain;

    }

    private Set<String> attemptRegisteredVotersLoad() throws LoadException {
        JSONArray RegisteredVotersJson;

        try {
            RegisteredVotersJson = (JSONArray) FileHandlers.readFromJSONFile(registeredVoters.getPath());
        } catch (JSONException e) {
            throw new LoadException("data.registered_voters.json is malformed", e);
        } catch (IOException e) {
            throw new LoadException("data.registered_voters.json is missing", e);
        }

        if (RegisteredVotersJson == null) {
            throw new LoadException("data.registered_voters.json is missing");
        }

        return new HashSet<>(BlockchainParser.JSONToRemainingVoters(RegisteredVotersJson));
    }

    public Blockchain attemptBlockchainCreationWithRegisteredVoters() throws LoadException {
        return new Blockchain(localNode, attemptRegisteredVotersLoad()); // throws LoadException
    }

    private Blockchain createNewBlockchain() {
        return new Blockchain(localNode);
    }

    public Blockchain loadBlockchain() {
        try {
            Blockchain newBLockchain = attemptBlockchainLoad();
            logger.info("Successfully loaded blockchain");
            return newBLockchain;
        } catch (LoadException e) {
            logger.warn("Error loading blockchain", e);
        }

        try {
            Blockchain newBLockchain = attemptBlockchainCreationWithRegisteredVoters();
            logger.info("Successfully loaded blockchain from registered voters");
            return newBLockchain;
        } catch (LoadException e) {
            logger.warn("Error creating blockchain from registered voters", e);
        }

        logger.info("Empty blockchain created");
        return createNewBlockchain();
    }

    // NetworkManager Load
    private NetworkManager attemptNetworkManagerLoad() throws LoadException {
        JSONObject networkManagerJSON;

        try {
             networkManagerJSON = (JSONObject) FileHandlers.readFromJSONFile(persistentNetworkManager.getPath()); // Throws IOException
        } catch (IOException e) {
            throw new LoadException("data/network_manager.json is missing or empty", e);
        }

        if (networkManagerJSON == null) {
            throw new LoadException("data/network_manager.json is missing or empty");
        }

        NetworkManager networkManager;

        try {
            networkManager = NetworkParser.JSONToNetworkManager(networkManagerJSON, host, port, localNode);
        } catch (MalformedJSONException | NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            throw new LoadException("data/network_manager.json is malformed", e);
        }

        return networkManager;
    }

    private NetworkManager createNewNetworkManager() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        return new NetworkManager(host, port, localNode, new KnownPeers(), new MessageCache());
    }

    public NetworkManager loadNetworkManager() {
        try {
            NetworkManager newNetworkManager = attemptNetworkManagerLoad();
            logger.info("Successfully loaded networkManager");
            return newNetworkManager;
        } catch (LoadException e) {
            logger.warn("Error loading network manager" + Exceptions.buildExceptionChain(e) );
        }

        try {
            NetworkManager newNetworkManager = createNewNetworkManager();
            logger.info("New network manager created");
            return newNetworkManager;
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            logger.error("Error creating new network manager", e);
            throw new RuntimeException();
        }
    }

    // Persistence
    public void persistBlockchain(Blockchain blockchain) throws PersistenceException {
        JSONObject persistentBlockchainJSON = BlockchainParser.BlockchainToJSON(blockchain);

        try {
            FileHandlers.writeToJSONFile(persistentBlockchain.getPath(), persistentBlockchainJSON);
        } catch (IOException e) {
            throw new PersistenceException(e.getMessage());
        }
    }

    public void persistNetworkManager(NetworkManager networkManager) throws PersistenceException  {
        JSONObject persistentNetworkManagerJSON = NetworkParser.networkManagerToJSON(networkManager);

        try {
            FileHandlers.writeToJSONFile(persistentNetworkManager.getPath(), persistentNetworkManagerJSON);
        } catch (IOException e) {
            throw new PersistenceException(e.getMessage());
        }
   }

   // Registered Voters
    public void addVoterToRegisteredVoters(String voter) throws IOException {
        FileHandlers.appendToJSONFileArray(registeredVoters.getPath(),voter);
    }

}

