// Handles the sending of messages  to other peers on the network

package network;

import java.io.*;
import java.net.*;
import java.security.PrivateKey;
import java.security.PublicKey;

// Driver Class
public class Client{
    private String host;
    private int port;
    private PublicKey publicKey;
    private PrivateKey privateKey;
}