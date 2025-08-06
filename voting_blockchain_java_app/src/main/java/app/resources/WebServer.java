package app.resources;

import app.LocalPeer;
import app.resources.blockchain.resources.Vote;
import app.resources.util.JSONParsers.BlockchainParser;

import com.sun.net.httpserver.HttpServer; // Allow creation and start of HTTP server
import com.sun.net.httpserver.HttpHandler; // Interface for handling HTTP requests
import com.sun.net.httpserver.HttpExchange; // Represents the request and response

import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.concurrent.Executors;

public class WebServer {
    // Fields
    private final Logger logger;


    private final LocalPeer localPeer;

    private final String host;
    private final int port;

    // Initialisation
    public WebServer(String host, int port, LocalPeer localPeer) {
        this.logger = LoggerFactory.getLogger(WebServer.class);

        this.localPeer = localPeer;

        this.host = host;
        this.port = port;

        logger.info("WebServer Initialized");
    }

    // Getter
    public int getPort() {
        return port;
    }

    // Methods
    public void start() {
        HttpServer server;

        try {
            server = HttpServer.create(new InetSocketAddress(host, port), 0); // Create HTTP sever bound to host:port
        } catch (IOException e) {
            logger.error("Error starting http server", e);
            throw new RuntimeException(e);
        }

        // L:KJ:LKLKFALKDLDFJLKFLK:J
        /*
        server.createContext("/", exchange -> {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            logger.error("Incoming request: " + method + " " + path);

           if(path.equals("/") || path.equals("/vote")) {
               new rootHandler().handle(exchange);
           } else {
               String response = "Unhandled path" + path;
               exchange.sendResponseHeaders(404, response.length());
               try (OutputStream os = exchange.getResponseBody()) {
                   os.write(response.getBytes());
               }
           }
        });
         */

        // L:JL:KJSD:LKJLFKJDSLKJFLKJDK

        server.createContext("/", new rootHandler());
        server.createContext("/vote", new VoteHandler()); // Create a new route at "/vote", POST requests to "/vote" will be handled by "VoteHandler()"
        server.setExecutor(null); // Uses default single threaded executor
        server.start(); // Starts server listening

        logger.info("Web server started running on http://" + host + ":" + port);
    }

    // Handles incoming vote, called on POST to "/vote"
    public class VoteHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("POST")) {
                exchange.sendResponseHeaders(400, 0);
                return;
            }

            // Reads raw body of HTTP request
            InputStream in = exchange.getRequestBody();

            // Converts from InputStream to String
            String body = new BufferedReader(new InputStreamReader(in)).lines().reduce("", (a, b) -> a + b);

            // Close InputStream
            in.close();

            try {
                logger.info("New vote received: " + body);

                // Convert String to JSON to Vote
                Vote vote = BlockchainParser.JSONToVote(new JSONObject(body));

                // Send vote to LocalPeer
                logger.info("New vote send for handling: " + vote.serialise());
                localPeer.handleNewVote(vote);

                // Send back success response
                String response = "Vote accepted.";
                exchange.sendResponseHeaders(200, response.length());

                try (OutputStream out = exchange.getResponseBody()) {
                    out.write(response.getBytes());
                }

            } catch (Exception e) {
                // Send back reject response
                logger.info("New vote rejected: " + body);

                String response = "Vote rejected: " + e.getMessage();
                exchange.sendResponseHeaders(400, response.length());
                try (OutputStream out = exchange.getResponseBody()) {
                    out.write(response.getBytes());
                }
            }


        }
    }

    public class rootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            logger.info("Root request received");
            if(!exchange.getRequestMethod().equals("GET")){
                logger.warn("Invalid method: "  + exchange.getRequestMethod());
                exchange.sendResponseHeaders(400,0);
                try (OutputStream out = exchange.getResponseBody()) {
                    out.write("Bad request".getBytes());
                }
                return;
            }

            try {
                byte[] response = Files.readAllBytes(Path.of("src/main/frontend/vote.html"));
                exchange.sendResponseHeaders(200, response.length);

                // try with resources automatically closes output stream
                try(OutputStream out = exchange.getResponseBody()){
                    out.write(response);
                }

                logger.info("Successfully served vote.html");
            } catch (IOException e) {
                logger.error("Failed serving vote.html", e);
                String msg = "My 404 Not Found";
                exchange.sendResponseHeaders(404, msg.length());

                try(OutputStream out = exchange.getResponseBody()) {
                    out.write(msg.getBytes());
                }
            }
        }
    }

}
