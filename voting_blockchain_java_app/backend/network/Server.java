package network;

import java.io.*;
import java.net.*;

import network.nodes.LocalNode;


public class Server {

    // Fields
    private final Socket socket;
    private final LocalNode localNode;
    private final BufferedReader reader;
    private final PrintWriter writer;

    // Initialisation
    public Server(Socket socket, LocalNode localNode) throws IOException {
        this.socket = socket;
        this.localNode = localNode;
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new PrintWriter(socket.getOutputStream(), true);
    }

}