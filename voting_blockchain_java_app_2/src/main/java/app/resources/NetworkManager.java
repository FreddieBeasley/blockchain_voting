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

    private final MessageReceiver messageReceiver;

    public NetworkManager(String host, int port, LocalNode localNode, KnownPeers knownPeers) throws NoSuchAlgorithmException {
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
    public String getPublicKey() {
        return publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public KnownPeers getKnownPeers() {
        return knownPeers;
    }


    /* <---------Begin---Listening---For---Messages---------> */
    public void start() {
        Thread serverThread = new Thread(messageReceiver);
        serverThread.start();
    }


    /* <---------Message---Cycle---For---Send---------> */

    // ( distributes send messages to 3 random known peers )
    public void distributeSendMessage(JSONObject message){
        for (RemotePeer peer : knownPeers.getRandomPeers(3)) {
            String foreignHost = peer.getHost();
            int foreignPort = peer.getPort();

            new Thread(() -> {
                JSONObject response;
                try {
                    response = MessageSender.send(foreignHost, foreignPort, message);
                } catch (Exception e) {
                    logger.warn("Unable to send message to peer " + foreignHost + ":" + foreignPort + " : " + e.getMessage());
                    return;
                }

                if (response.getString("message_type").equals("accepted")) {
                    logger.info(foreignHost + ":" + foreignPort + " accepted vote");
                } else {
                    logger.warn(foreignHost + ":" + foreignPort + " rejected vote: " + response.getJSONObject("data").getString("reason"));
                }
            }).start();
        }
    }

    // ( occurs on WebServer vote submission )
    public void formulateOutgoingVote(Vote vote) {
        try {
            // Create Message
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

                if (response.getString("message_type").equals("accepted")) {
                    JSONObject JSONBlockchain = response.getJSONObject("data");
                    Blockchain newBlockchain = BlockchainParser.JSONToBlockchain(JSONBlockchain, localNode);
                    localNode.handleNetworkBlockchain(newBlockchain);
                } else {
                    logger.warn("Blockchain request rejected by " + foreignHost + ":" + foreignPort + " : " + response.getJSONObject("data").getString("reason"));
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

            if (response.getString("message_type").equals("accepted")) {
                JSONObject JSONPeer = response.getJSONObject("data");
                RemotePeer newPeer = NetworkParser.JSONToRemotePeer(JSONPeer);
                knownPeers.addPeer(newPeer);
                logger.info(foreignHost + ":" + foreignPort + " accepted connection request");
            } else {
                logger.warn("Connection request rejected by " + foreignHost + ":" + foreignPort + " : " + response.getJSONObject("data").getString("reason"));
            }

        } catch (Exception e) {
            logger.warn("Failed to complete connection request", e);
        }
    }


    /* <---------General---Message---Constructors---------> */

    // ( constructs message into recognised format )
    private JSONObject formulateOutgoingMessage(JSONObject data, String messageType) throws SignatureException {
        JSONObject message = new JSONObject();
        message.put("sender", formulatePeerID());
        message.put("message_type", messageType);
        message.put("data", data);

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
        peerID.put("publicKey", publicKey);
        return peerID;
    }


    /* <---------Receiving---Message---From---Peers---------> */

    // ( deciphers messages from recognised format )
    public JSONObject handleIncomingMessage(JSONObject message) {
        String received_messageType = message.getString("message_type");
        JSONObject received_data = message.getJSONObject("data");

        String response_messageType = "";
        JSONObject response_data = new JSONObject();

        // Validate message
        try {
            validateMessage(message);
        } catch (InvalidParameterException e) {
            response_data.put("reason", e.getMessage());
            return constructResponse("rejected", response_data);
        }

        // Handle send messages
        if (received_messageType.startsWith("send")) {
            response_messageType = "accepted_data";
            if (received_messageType.endsWith("vote")) {
                response_data.put("type", "vote");
                Vote vote;

                try {
                    vote = BlockchainParser.JSONToVote(received_data);
                    localNode.handleNetworkVote(vote);
                } catch (Exception e) {
                    response_data.put("reason", e.getMessage());
                    return constructResponse("rejected", response_data);
                }

                //redistribute
                formulateOutgoingVote(vote);

            } else if (received_messageType.endsWith("voter")) {
                response_data.put("type", "voter");

                String voter =  received_data.getString("voter");
                localNode.handleNetworkVoter(voter);

                //redistribute
                formulateOutgoingVoter(voter);

            } else if (received_messageType.endsWith("block")) {
                response_data.put("type", "block");
                Block block;

                try {
                    block = BlockchainParser.JSONToBlock(received_data);
                    localNode.handleNetworkBlock(block);
                } catch (Exception e) {
                    response_data.put("reason", e.getMessage());
                    return constructResponse("rejected", response_data);
                }

                //redistribute
                formulateOutgoingBlock(block);
            }

        } else if (received_messageType.equals("request_blockchain")) {
            JSONObject JSONBlockchain = localNode.getBlockchainJSON();
            response_messageType = "accepted_request";
            response_data = JSONBlockchain;

        } else if (received_messageType.equals("request_connection")) {
            try {
                JSONObject JSONPeer = message.getJSONObject("sender");
                RemotePeer newPeer = NetworkParser.JSONToRemotePeer(JSONPeer);
                knownPeers.addPeer(newPeer);

                response_messageType = "accepted_request";
                response_data = JSONPeer;
            } catch (Exception e) {
                response_messageType = "rejected_request";
                response_data.put("reason", e.getMessage());
            }
        }

        return constructResponse(response_messageType, response_data);
    }

    // Helpers for handleIncomingResponse()
    public void validateMessage(JSONObject message) throws InvalidParameterException {
        JSONObject sender;
        String message_type;
        JSONObject data;
        String signature;

        // All message fields exist
        try {
            sender = message.getJSONObject("sender");
            message_type = message.getString("message_type");
            data = message.getJSONObject("data");
            signature = message.getString("signature");
        } catch (Exception e) {
            throw new InvalidParameterException("Invalid message format: " + e.getMessage());
        }

        // Message has valid signature
        try {
            Cryptography.verify(data.toString(), publicKey, signature);
        } catch (Exception e) {
            throw new InvalidParameterException("Invalid signature");
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

        // Message sender is a peer ( not for request_connection )
        try {
            RemotePeer senderPeer = NetworkParser.JSONToRemotePeer(sender);
            if (!(!message_type.equals("request_connection") && knownPeers.containsPeer(senderPeer))) {
                throw new InvalidParameterException("Sender not in known peer list");
            }
        } catch (Exception e) {
            throw new InvalidParameterException("Invalid sender: " + e.getMessage());
        }
    }

    public JSONObject constructResponse(String messageType, JSONObject data) {
        JSONObject response = new JSONObject();
        response.put("sender", formulatePeerID());
        response.put("message_type", messageType);
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
