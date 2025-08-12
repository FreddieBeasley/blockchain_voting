package app.resources;

import app.resources.JSONParsers.BlockchainParser;
import app.resources.JSONParsers.NetworkParser;

import app.resources.exceptions.InvalidException;

import app.resources.network.*;
import app.resources.network.resources.RemotePeer;
import app.resources.network.MessageReceiver;
import app.resources.network.MessageSender;

import app.LocalNode;

import app.resources.util.Cryptography;

import app.resources.blockchain.resources.*;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.*;
import java.util.ArrayList;
import java.util.List;

public class NetworkManager {
    private final Logger logger;

    private final LocalNode localNode;

    private final String host;
    private final int port;

    private final String publicKey;
    private final String privateKey;

    private final KnownPeers knownPeers;
    private final MessageCache messageCache;

    private final MessageReceiver messageReceiver;

    public NetworkManager(String host, int port, LocalNode localNode, KnownPeers knownPeers, MessageCache messageCache) throws NoSuchAlgorithmException {
        this.messageReceiver = new MessageReceiver(port, this);

        this.logger = LoggerFactory.getLogger(NetworkManager.class);

        this.localNode = localNode;

        this.host = host;
        this.port = port;

        this.knownPeers = knownPeers;
        this.messageCache = messageCache;

        KeyPair keypair = Cryptography.generateKeyPair();
        this.publicKey = Cryptography.publicKeyToString(keypair.getPublic());
        this.privateKey = Cryptography.privateKeyToString(keypair.getPrivate());
    }

    public NetworkManager(String host, int port, LocalNode localNode, KnownPeers knownPeers, MessageCache messageCache, String publicKey, String privateKey) throws InvalidException {
        this.messageReceiver = new MessageReceiver(port, this);
        this.logger = LoggerFactory.getLogger(NetworkManager.class);

        this.localNode = localNode;

        this.host = host;
        this.port = port;

        this.knownPeers = knownPeers;
        this.messageCache = messageCache;

        // Ensures valid public and private key has been passed in
        Cryptography.stringToPublicKey(publicKey); // Throws InvalidException
        Cryptography.stringToPublicKey(privateKey); // Throws InvalidException

        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    // Getter Method
    public String getPublicKey() {
        return publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public KnownPeers getKnownPeers() {
        return knownPeers;
    }

    public MessageCache getMessageCache() {
        return messageCache;
    }


    /* <---------Begin---Listening---For---Messages---------> */
    public void start() {
        messageReceiver.run();
    }


    /* <---------Message---Cycle---For---Send---------> */

    // ( distributes send messages to 3 random known peers )
    public void distributeSendMessage(JSONObject message){
        logger.info("Distributing message of type {}", message.getString("message_type"));
        for (RemotePeer peer : knownPeers.getRandomPeers(3)) {
            String foreignHost = peer.getHost();
            int foreignPort = peer.getPort();

            new Thread(() -> {
                JSONObject response;
                try {
                    logger.debug("Attempting to send message to {}:{}", foreignHost, foreignPort);
                    response = MessageSender.send(foreignHost, foreignPort, message);
                } catch (Exception e) {
                    logger.warn("Unable to send message to {}:{} - {}",  foreignHost, foreignPort, e.getMessage(), e);
                    return;
                }

                if(response.getBoolean("accepted")) {
                    logger.info("{} accepted by {}:{}", response.getString("message_type"), foreignHost, foreignPort);
                } else {
                    logger.error("Error sending {} to {}:{} - {}", message.getString("message_type"), foreignHost, foreignPort, response.getJSONObject("data").getString("reason"));
                }

            }, "SendThread-" + foreignHost + ":" + foreignPort).start();
        }
    }

    // ( occurs on WebServer vote submission )
    public void formulateOutgoingVote(Vote vote) {
        try {
            // Create Message
            logger.info("formulating outgoing vote");

            JSONObject data = BlockchainParser.VoteToJSON(vote);
            JSONObject message = formulateOutgoingMessage(data, "send_vote");

            distributeSendMessage(message);

        } catch (Exception e) {
            logger.error("Failed to distribute vote", e);
        }
    }

    // ( occurs on Blockchain block creation )
    public void formulateOutgoingBlock(Block block) {
        try {
            logger.info("formulating outgoing block");

            JSONObject data = BlockchainParser.BlockToJSON(block);
            JSONObject message = formulateOutgoingMessage(data, "send_block");

            distributeSendMessage(message);
        } catch (Exception e) {
            logger.error("Failed to distribute block", e);
        }
    }

    // ( occurs on WebServer voter registration )
    public void formulateOutgoingVoter(String voter) {
        try {
            logger.info("formulating outgoing voter");

            JSONObject data = new JSONObject();
            data.put("voter", voter);
            JSONObject message = formulateOutgoingMessage(data, "send_voter");

            distributeSendMessage(message);
        } catch (Exception e) {
            logger.error("Failed to distribute voter", e);
        }
    }


    /* <---------Message---Cycle---For---Request---------> */

    // ( occurs intermittedly ( every minute ) )
    public void formulateOutgoingBlockchainRequest() {
        for(RemotePeer peer : knownPeers.getRandomPeers(1)) {
            String foreignHost = peer.getHost();
            int foreignPort  = peer.getPort();

            try {
                JSONObject data = new JSONObject(); // No data
                JSONObject message = formulateOutgoingMessage(data, "request_blockchain");
                JSONObject response = MessageSender.send(foreignHost, foreignPort, message);

                if (response.getBoolean("accepted")) {
                    JSONObject JSONBlockchain = response.getJSONObject("data");
                    Blockchain newBlockchain = BlockchainParser.JSONToBlockchain(JSONBlockchain, localNode);
                    localNode.handleNetworkBlockchain(newBlockchain);
                    logger.info("{} accepted by {}:{}", message.getString("message_type"), foreignHost, foreignPort);
                } else {
                    logger.warn("{} rejected by {}:{} - {}", message.getString("message_type"), foreignHost, foreignPort, response.getJSONObject("data").getString("reason"));
                }

            } catch (Exception e) {
                logger.error("Failed to complete blockchain request", e);
            }
        }

    }

    // ( occurs on command from ControlServer )
    public void formulateOutgoingConnectionRequest(String foreignHost, int foreignPort) {
        try {
            JSONObject data = new JSONObject();
            JSONObject message = formulateOutgoingMessage(data, "request_connection");
            JSONObject response = MessageSender.send(foreignHost, foreignPort, message);

            if (response.getBoolean("accepted")) {
                JSONObject JSONPeer = response.getJSONObject("sender");
                RemotePeer newPeer = NetworkParser.JSONToRemotePeer(JSONPeer);
                knownPeers.addPeer(newPeer);
                logger.info("{} accepted by {}:{}", message.getString("message_type"), foreignHost, foreignPort);
            } else {
                logger.warn("{} rejected by {}:{} - {}", message.getString("message_type"), foreignHost, foreignPort, response.getJSONObject("data").getString("reason"));
            }

        } catch (Exception e) {
            logger.warn("Failed to complete connection request: " + e.getMessage(), e);
        }
    }

    public void sendPingMessage() {}


    /* <---------General---Message---Constructors---------> */

    // ( constructs message into recognised format )
    private JSONObject formulateOutgoingMessage(JSONObject data, String messageType) throws Exception {
        JSONObject message = new JSONObject();
        message.put("sender", formulatePeerID());
        message.put("message_type", messageType);
        message.put("data", data);

        String hash;
        try {
            hash = Cryptography.hash(data.toString());
        } catch (Exception e) {
            logger.error("Failed to hash message", e);
            throw new Exception("Failed to hash message");
        }
        message.put("hash", hash);
        messageCache.addHash(hash);


        String signature;
        try {
            signature = Cryptography.sign(data.toString(), privateKey);
        } catch (Exception e) {
            logger.error("Failed to sign message", e);
            throw new SignatureException("Failed to sign message");
        }
        message.put("signature", signature);

        return message;
    }

    // Helper for formulateOutgoingMessage(), constructResponse()
    private JSONObject formulatePeerID() {
        JSONObject peerID = new JSONObject();
        peerID.put("host", host);
        peerID.put("port", port);
        peerID.put("public_key", publicKey);
        return peerID;
    }


    /* <---------Receiving---Message---From---Peers---------> */

    // ( deciphers messages from recognised format )
    public JSONObject handleIncomingMessage(JSONObject message) {
        String messageType = message.getString("message_type");
        JSONObject received_data = message.getJSONObject("data");

        String response_messageType = "";
        JSONObject response_data = new JSONObject();

        // Validate message
        try {
            validateMessage(message);
        } catch (InvalidParameterException e) {
            response_data.put("reason", e.getMessage());
            return constructResponse(messageType, false, response_data);
        }

        // Handle send messages
        switch (messageType) {
            case "send_vote" -> {
                try {
                    Vote vote = BlockchainParser.JSONToVote(received_data);
                    localNode.handleNetworkVote(vote);
                    JSONObject newMessage = formulateOutgoingMessage(received_data,messageType);
                    distributeSendMessage(newMessage);
                    return constructResponse(messageType, true, response_data);
                } catch (Exception e) {
                    response_data.put("reason", e.getMessage());
                    return constructResponse(messageType, false, response_data);
                }
            }
            case "send_voter" -> {
                try {
                    String voter = received_data.getString("voter");
                    localNode.handleNetworkVoter(voter);
                    JSONObject newMessage = formulateOutgoingMessage(received_data,messageType);
                    distributeSendMessage(newMessage);
                    return constructResponse(messageType, true, response_data);
                } catch (Exception e) {
                    response_data.put("reason", e.getMessage());
                    return constructResponse(messageType, false, response_data);
                }
            }
            case "send_block" -> {
                try {
                    Block block = BlockchainParser.JSONToBlock(received_data);
                    localNode.handleNetworkBlock(block);
                    JSONObject newMessage = formulateOutgoingMessage(received_data,messageType);
                    distributeSendMessage(newMessage);
                    return constructResponse(messageType, true, response_data);
                } catch (Exception e) {
                    response_data.put("reason", e.getMessage());
                    return constructResponse(messageType, false, response_data);
                }
            }
            case "request_connection" -> {
                try {
                    RemotePeer peer = NetworkParser.JSONToRemotePeer(message.getJSONObject("sender"));
                    knownPeers.addPeer(peer);
                    return constructResponse(messageType, true, response_data);
                } catch (Exception e) {
                    response_data.put("reason", e.getMessage());
                    return constructResponse(messageType, false, response_data);
                }
            }
            case "request_blockchain" -> {
                try {
                    response_data = localNode.getBlockchainJSON();
                    return constructResponse(messageType, true, response_data);
                } catch (Exception e) {
                    response_data.put("reason", e.getMessage());
                    return constructResponse(messageType, false, response_data);
                }
            }
            case "ping" -> {
                return constructResponse(messageType, true, response_data);
            }
            default -> {
                response_data.put("reason", "Invalid message type");
                return constructResponse(messageType, false, response_data);
            }
        }
    }

    // Helpers for handleIncomingResponse()
    public void validateMessage(JSONObject message) throws InvalidParameterException {
        JSONObject sender;
        String message_type;
        JSONObject data;
        String hash;
        String signature;

        // All message fields exist
        try {
            sender = message.getJSONObject("sender");
            message_type = message.getString("message_type");
            data = message.getJSONObject("data");
            hash = message.getString("hash");
            signature = message.getString("signature");
        } catch (Exception e) {
            throw new InvalidParameterException("Invalid message format: " + e.getMessage());
        }

        // Message has valid sender
        RemotePeer senderPeer;
        try {
            senderPeer = NetworkParser.JSONToRemotePeer(sender);
        } catch (Exception e) {
            throw new InvalidParameterException("Malformed sender field");
        }

        // Message has valid signature
        try {
            Cryptography.verify(data.toString(), signature, senderPeer.getPublicKey());
        } catch (Exception e) {
            throw new InvalidParameterException("Invalid signature: " + e.getMessage());
        }

        if (messageCache.containsHash(hash)) {
            throw new InvalidParameterException("Message already seen");
        }


        // Message has valid message type
        List<String> message_types = new ArrayList<>();
        message_types.add("send_vote");
        message_types.add("send_voter");
        message_types.add("send_block");
        message_types.add("request_blockchain");
        message_types.add("request_connection");

        if (!message_types.contains(message_type)) {
            throw new InvalidParameterException("Invalid message_type " + message_type);
        }

        // Sender in knownPeers ( unless requesting connection )
        if (message_type.equals("request_connection")) {
            return;
        }

        if (!knownPeers.containsPeer(senderPeer)) {
            throw new InvalidParameterException("Sender not in known peer list");
        }
    }

    public JSONObject constructResponse(String messageType, boolean accepted, JSONObject data) {
        JSONObject response = new JSONObject();
        response.put("sender", formulatePeerID());
        response.put("message_type", messageType);
        response.put("accepted", accepted);
        response.put("data", data);

        String signature;
        try {
            signature = Cryptography.sign(data.toString(), privateKey);
        } catch (Exception e) {
            throw new InvalidParameterException("Invalid signature");
        }
        response.put("signature", signature);

        return response;

    }
}
