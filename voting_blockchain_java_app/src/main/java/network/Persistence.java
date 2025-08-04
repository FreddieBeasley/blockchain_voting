package network;

import blockchain.Block;
import blockchain.Blockchain;
import exceptions.*;
import org.json.JSONArray;
import org.json.JSONException;
import util.FileHandlingUtils;
import util.ParserUtils;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class Persistence {

    // Blockchain
    private final File persistentBlockchain;

    // Network
    private final File persistentKnownPeers;

    private final File registeredVoters;

    // default initialisation
    public Persistence() {
        this.persistentBlockchain = new File("src/main/data/blockchain.json");

        this.persistentKnownPeers = new File("src/main/data/known_peers.json");

        this.registeredVoters = new File("src/main/data/registered_voters.json");

    }

    // custom initialisation
    public Persistence(String persistentBlockchain, String persistentKnownPeers, String registeredVoters) {
        this.persistentBlockchain = new File(persistentBlockchain);

        this.persistentKnownPeers = new File(persistentKnownPeers);

        this.registeredVoters = new File(registeredVoters);
    }

    // Blockchain persistence
    public Blockchain attemptBlockchainLoad() throws IOException, LoadException {
        JSONObject blockchainJson;
        try {
            blockchainJson = (JSONObject) FileHandlingUtils.readFromJSONFile(persistentBlockchain.getPath());
        } catch (JSONException e) {
            throw new  LoadException("Blockchain is malformed", e);
        }

        if (blockchainJson == null){
            throw new IOException("No persistent blockchain found");
        }

        Blockchain newBlockchain;

        try {
            newBlockchain = ParserUtils.JSONToBlockchain(blockchainJson); // Throws MalformedJSONException
            newBlockchain.isValid(); // Throws InvalidBlockchainException
        } catch (MalformedJSONException | InvalidBlockchainException e) {
            throw new LoadException("Blockchain is malformed or invalid", e);
        }

        return newBlockchain;
    }

    public Blockchain attemptBlockchainCreationWithRegisteredVoters() throws IOException, LoadException {
        return new Blockchain(attemptRegisteredVotersLoad());
    }

    private Set<String> attemptRegisteredVotersLoad() throws IOException, LoadException {
        JSONArray RegisteredVotersJson;

        try {
            RegisteredVotersJson = (JSONArray) FileHandlingUtils.readFromJSONFile(registeredVoters.getPath());
        } catch (JSONException e){
            throw new LoadException("Failed to load registered Voters", e);
        }

        if (RegisteredVotersJson == null){
            throw new IOException("No registered voters found");
        }
        
        return new HashSet<>(ParserUtils.JSONToRemainingVoters(RegisteredVotersJson));
    }

    public void persistBlockchain(Blockchain blockchain) throws PersistenceException {
        JSONObject persistentBlockchainJson = ParserUtils.BlockchainToJSON(blockchain);

        try {
            JSONObject persistentBlockchainJSON = ParserUtils.BlockchainToJSON(blockchain);
            FileHandlingUtils.writeToJSONFile(persistentBlockchain.getPath(), persistentBlockchainJSON);
        } catch (IOException e) {
            throw new PersistenceException(e.getMessage());
        }

        // Testing separating information
        try {
            FileHandlingUtils.writeToJSONFile("src/main/data/blockchain/chain.json", persistentBlockchainJson.get("chain"));
            FileHandlingUtils.writeToJSONFile("src/main/data/blockchain/pending_votes.json", persistentBlockchainJson.get("pendingVotes"));
            FileHandlingUtils.writeToJSONFile("src/main/data/blockchain/remaining_voters.json", persistentBlockchainJson.get("remainingVoters"));
            FileHandlingUtils.writeToJSONFile("src/main/data/blockchain/metadata.json", persistentBlockchainJson.get("metadata"));
        } catch (IOException e){
            System.out.println("[ TESTING ] Failed to persist blockchain: " + e.getMessage());
        }
    }

    // Peers persistence
    public KnownPeers attemptPeersLoad() throws IOException, LoadException {
        JSONArray knownPeersJson = (JSONArray) FileHandlingUtils.readFromJSONFile(persistentKnownPeers.getPath()); // Throws IOException

        if (knownPeersJson == null){
            throw new IOException("No persistent known peers found");
        }

        KnownPeers knownPeers;

        try {
            knownPeers = new KnownPeers(ParserUtils.JSONToKnownPeersList(knownPeersJson));
        } catch (MalformedJSONException e) {
            throw new LoadException(e.getMessage());
        }

        return knownPeers;
    }

    public void persistKnownPeers(KnownPeers knownPeers) throws PersistenceException {
      JSONArray knownPeersJson = ParserUtils.KnownPeersListToJSON(knownPeers.getKnownPeers());

        try {
            FileHandlingUtils.writeToJSONFile(persistentKnownPeers.getPath(), knownPeersJson);
        } catch (IOException e) {
            throw new PersistenceException(e.getMessage());
        }
    }

    // Pending Votes persistence

    /*

    public Queue<Vote> attemptPendingVotesListQueue(){
        JSONArray pendingVotesJson;
        Queue<Vote> pendingVotes = new LinkedList<>();

        try {
            pendingVotesJson = (JSONArray) FileHandlingUtils.readFromJSONFile(persistentPendingVotes.getPath());
        } catch (IOException e) {
            logger.info("No persistent pendingVotes found");

            return pendingVotes;
        }

        if (pendingVotesJson == null){
            logger.info("No persistent pendingVotes found");
            return pendingVotes;
        }

        for (Object o: pendingVotesJson){
            JSONObject pendingVoteJson = (JSONObject) o;
            Vote pendingVote;

            try {
                pendingVote = ParserUtils.JSONToVote(pendingVoteJson);
            } catch (MalformedJSONVoteException e) {
                logger.warn(e.getMessage() + e);
                continue;
            }

            pendingVotes.add(pendingVote);
        }

        return pendingVotes;
    }

    public void persistPendingVotesQueue(Queue<Vote> pendingVotes){
        JSONArray pendingVotesJson = ParserUtils.PendingVotesToJSON(pendingVotes);

        try {
            FileHandlingUtils.writeToJSONFile(persistentPendingVotes.getPath(), pendingVotesJson);
        } catch (IOException e) {
            logger.warn("Failed to persist pending votes: " + e.getMessage());
        }
    }

     */




}

