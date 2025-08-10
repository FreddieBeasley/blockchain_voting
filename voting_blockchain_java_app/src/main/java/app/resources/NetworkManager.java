package app.resources;

import app.resources.JSONParsers.BlockchainParser;
import app.resources.JSONParsers.NetworkParser;
import app.resources.exceptions.InvalidException;
import app.resources.network.MessageReceiver;
import app.resources.network.MessageSender;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.LocalNode;
import app.resources.network.*;
import app.resources.network.resources.RemotePeer;
import app.resources.util.Cryptography;
import app.resources.blockchain.resources.*;


import java.security.*;
import java.util.Random;

public class NetworkManager {
    private final Logger logger;

    private final LocalNode localNode;

    private final String host;
    private final int port;

    private final String publicKey;
    private final String privateKey;

    private final KnownPeers knownPeers;


    private final MessageSender messageSender;
    private final MessageReceiver messageReceiver;

    public NetworkManager(String host, int port, LocalNode localNode, KnownPeers knownPeers) throws NoSuchAlgorithmException {
        this.messageSender = new MessageSender(this);
        this.messageReceiver = new MessageReceiver(port, this);

        this.logger = LoggerFactory.getLogger(NetworkManager.class);

        this.localNode = localNode;

        this.host = host;
        this.port = port;

        this.knownPeers = knownPeers;

        KeyPair keypair = Cryptography.generateKeyPair();
        this.publicKey = Cryptography.publicKeyToString(keypair.getPublic());
        this.privateKey = Cryptography.privateKeyToString(keypair.getPrivate());
    }

    public NetworkManager(String host, int port, LocalNode localNode, KnownPeers knownPeers, String publicKey, String privateKey) throws InvalidException {
        this.messageSender = new MessageSender(this);
        this.messageReceiver = new MessageReceiver(port, this);
        this.logger = LoggerFactory.getLogger(NetworkManager.class);

        this.localNode = localNode;

        this.host = host;
        this.port = port;
        this.knownPeers = knownPeers;

        // Ensures valid public and private key has been passed in
        Cryptography.stringToPublicKey(publicKey); // Throws InvalidException
        Cryptography.stringToPublicKey(privateKey); // Throws InvalidException

        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    // Getter Method
    public String getPublicKey(){
        return publicKey;
    }

    public String getPrivateKey(){
        return privateKey;
    }

    public KnownPeers getKnownPeers() {
        return knownPeers;
    }

    // Method - network
    public void start(){
    Thread serverThread = new Thread(messageReceiver);
    serverThread.start();
    }

    // Methods - Outgoing

    // ( occurs on WebServer vote submission )
    public void formulateOutgoingVote(Vote vote){
        JSONObject data = BlockchainParser.VoteToJSON(vote);
        JSONObject message = formulateOutgoingMessage(data, "send_vote");
        distributeMessage(message);
    }

    // ( occurs on Blockchain block creation )
    public void formulateOutgoingBlock(Block block){
        JSONObject data = BlockchainParser.BlockToJSON(block);
        JSONObject message = formulateOutgoingMessage(data, "send_block");
        distributeMessage(message);
    }

    // ( occurs on WebServer voter registration )
    public void formulateOutgoingVoter(String voter){
        JSONObject data = new JSONObject();
        data.put("voter", voter);
        JSONObject message = formulateOutgoingMessage(data, "send_voter");
        distributeMessage(message);
    }

    // ( occurs intermittedly ( every minute ) )
    public void formulateOutgoingBlockchainRequest(){
        JSONObject data = new JSONObject(); // No data
        JSONObject message = formulateOutgoingMessage(data, "request_blockchain");
        distributeMessage(message);
    }

    // ( occurs on command from ControlServer )
    public void formulateOutgoingConnectionRequest(String foreignHost, int foreignPort){
        JSONObject data = new JSONObject();
        data.put("host", host);
        data.put("port", port);
        data.put("publicKey", publicKey);
        JSONObject message = formulateOutgoingMessage(data, "request_connection");
        messageSender.send(foreignHost, foreignPort, message);
    }


    private JSONObject formulateOutgoingMessage(JSONObject data, String messageType){
        JSONObject message = new JSONObject();
        message.put("sender", publicKey);
        message.put("message_type", messageType);
        message.put("data", data);

        String signature;
        try {
            signature = Cryptography.sign(data.toString(), privateKey);
        } catch (Exception e) {
            logger.error("Failed to sign message", e);
            return null;
        }

        message.put("signature", signature);

        return message;
    }

    public void distributeMessage(JSONObject message){
        Random rand = new Random();
        for (int i = 0; i < 3 && i < knownPeers.getSize(); i++){
            int randomIndex =  rand.nextInt(knownPeers.getSize());
            RemotePeer peer = knownPeers.getPeer(randomIndex);

            String peerHost = peer.getHost();
            int peerPort = peer.getPort();

            messageSender.send(peerHost, peerPort, message);
        }
    }


    // Handle response from outgoing messages
    public void handleIncomingResponse(JSONObject message){
        String sender;
        String messageType;
        String data;
        String signature;

        try {
            sender = message.getString("sender");
            messageType = message.getString("message_type");
            data = message.getString("data");
            signature = message.getString("signature");
        } catch (JSONException e) {
            logger.warn("Response malformed: " + e.getMessage());
            return;
        }

        try {
            Cryptography.verify(data,signature,sender);
        } catch (Exception e) {
            logger.warn("Response has invalid signature: " + e.getMessage());
            return;
        }

        if (messageType.equals("rejected")){
            logger.warn(messageType + " : " + data + " : " + sender);
        } else if (messageType.equals("accepted")) {
            logger.info(messageType + " : " + data + " : " + sender);
        } else {
            logger.warn("Response have invalid message type: " + messageType);
        }


    }

    // Handles incoming messages and response - very messy
    public JSONObject handleIncomingMessage(JSONObject received_message) throws Exception{
        //Received Fields
        String received_sender;
        String received_messageType;
        JSONObject received_data;
        String received_signature;

        // Response Fields
        JSONObject response_message = new JSONObject();
        JSONObject response_data = new JSONObject();


        // Verify all fields
        try {
            received_sender = received_message.getString("sender");
            received_messageType = received_message.getString("message_type");
            received_data = received_message.getJSONObject("data");
            received_signature = received_message.getString("signature");

        } catch  (JSONException e) {
            response_message.put("message_type", "rejected");
            response_message.put("data", e.getMessage());
            response_message.put("signature", Cryptography.sign(e.getMessage(), privateKey));
            return response_message;

        }

        try {
            Cryptography.verify(received_data.toString(), received_signature, received_sender);
        }
        catch (InvalidException e){
            response_message.put("message_type", "rejected");
            response_message.put("data", e.getMessage());
            response_message.put("signature", Cryptography.sign(e.getMessage(), privateKey));
            return response_message;
        }

        try {
            if (received_messageType.startsWith("send")) {
                if (received_messageType.endsWith("vote")) {
                    Vote vote = BlockchainParser.JSONToVote(received_data);
                    localNode.handleNetworkVote(vote);

                    response_message.put("message_type", "accepted");
                    response_message.put("data", "send_vote");
                    response_message.put("signature", Cryptography.sign("send_vote", privateKey));

                } else if (received_messageType.endsWith("block")) {
                    Block block = BlockchainParser.JSONToBlock(received_data);
                    localNode.handleNetworkBlock(block);

                    response_message.put("message_type", "accepted");
                    response_message.put("data", "send_block");
                    response_message.put("signature", Cryptography.sign("send_block", privateKey));


                } else if (received_messageType.endsWith("voter")) {
                    String voter = received_data.getString("voter");
                    localNode.handleNetworkVoter(voter);

                    response_message.put("message_type", "accepted");
                    response_message.put("data", "send_voter");
                    response_message.put("signature", Cryptography.sign("send_voter", privateKey));


                } else {
                    response_message.put("message_type", "rejected");
                    response_message.put("data", "unrecognized");
                    response_message.put("signature", Cryptography.sign("unrecognized", privateKey));

                }


            } else if (received_messageType.startsWith("request")) {
                if (received_messageType.endsWith("blockchain")) {
                    JSONObject blockchainJSON = localNode.getBlockchainJSON();
                    response_message.put("message_type", "accepted");
                    response_message.put("data", blockchainJSON);
                    response_message.put("signature", Cryptography.sign(blockchainJSON.toString(), privateKey));

                } else if (received_messageType.endsWith("connection")) {
                    try {
                        RemotePeer newPeer = NetworkParser.JSONToRemotePeer(received_data);
                        knownPeers.addPeer(newPeer);

                        response_message.put("message_type", "accepted");

                        response_data.put("host", host);
                        response_data.put("port", port);
                        response_data.put("public_key", publicKey);
                        response_message.put("data", response_data);

                        response_message.put("signature", Cryptography.sign(response_data.toString(), privateKey));

                    } catch (JSONException e) {
                        response_message.put("message_type", "rejected");
                        response_message.put("data", e.getMessage());
                        response_message.put("signature", Cryptography.sign(e.getMessage(), privateKey));
                    }


                }


            } else {
                response_message.put("message_type", "rejected");
                response_message.put("data", "unrecognized");
                response_message.put("signature", Cryptography.sign("unrecognized", privateKey));
            }
        } catch (Exception e) {
            response_message.put("message_type", "rejected");
            response_message.put("data", e.getMessage());
            response_message.put("signature", Cryptography.sign(e.getMessage(), privateKey));
        }

        return response_message;
    }
}

