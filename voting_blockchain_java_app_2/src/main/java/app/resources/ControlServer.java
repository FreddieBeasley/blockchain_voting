package app.resources;

import app.LocalNode;
import app.resources.logging.LogAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ControlServer {
    private final Logger logger;

    private final String host;
    private final int port;
    private final LocalNode localNode;

    public ControlServer(String host, int port, LocalNode localNode) {
        this.logger = LoggerFactory.getLogger(ControlServer.class);

        this.host = host;
        this.port = port;
        this.localNode = localNode;

        logger.info("Control server initialized");
    }

    public void start(){
        HttpServer server;

        LogAppender logAppender = new LogAppender();

        try {
            server = HttpServer.create(new InetSocketAddress(host, port), 0); // Create HTTP sever bound to host:port
        } catch (IOException e) {
            logger.error("Error starting http server", e);
            throw new RuntimeException(e);
        }

        // Pages
        server.createContext("/log-page", new StaticFileHandler("src/main/frontend/owner/root.html", "text/html")); // Displays logs
        server.createContext("/blockchain-page",  new StaticFileHandler("src/main/frontend/owner/blockchain.html", "text/html")); // Displays blockchain
        server.createContext("/network-page",  new StaticFileHandler("src/main/frontend/owner/network.html", "text/html")); // Displays network peers

        // Actions
        server.createContext("/logs", new LogHandler(logAppender));
        server.createContext("/blockchain", new blockchainHandler());
        server.createContext("/network-peers", new networkPeersHandler());
        server.createContext("/connect", new connectHandler());


        //server.createContext("/persist", new persistHandler()); // overrides auto-persist

        server.setExecutor(null); // default executor
        server.start();
        logger.info("Control server running at http://" + host + ":" + port);
    }

    // GET Files
    public class StaticFileHandler implements HttpHandler {
        private final Path filePath;
        private final String contentType;

        public StaticFileHandler(String filePath, String contentType) {
            this.filePath = Path.of(filePath);
            this.contentType = contentType;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            logger.info("Request received for '" + filePath + "'");

            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                logger.warn("Invalid request method " + exchange.getRequestMethod());
                exchange.sendResponseHeaders(400, 0);
                try (OutputStream out = exchange.getResponseBody()) {
                    out.write("Bad Request".getBytes());
                }
                return;
            }

            try {
                byte[] response = Files.readAllBytes(filePath);

                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.sendResponseHeaders(200, response.length);

                try (OutputStream out = exchange.getResponseBody()) {
                    out.write(response);
                }

                logger.info("Successfully served '" + filePath + "'");
            }  catch (IOException e) {
                logger.error("Failed to serve '" + filePath + "'", e);
                exchange.sendResponseHeaders(404, 0);

                try (OutputStream out = exchange.getResponseBody()) {
                    out.write("404 Not Found".getBytes());
                }
            }
        }
    }

    // Get Logs
    public class LogHandler implements HttpHandler {
        private final LogAppender logAppender;

        public LogHandler(LogAppender logAppender) {
            this.logAppender = logAppender;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1); // 405 Method Not Allowed
                return;
            }

            List<ILoggingEvent> logs = logAppender.getLogs();
            JSONArray jsonArray = new JSONArray();

            for (ILoggingEvent log : logs) {
                JSONObject jsonLog = new JSONObject();
                jsonLog.put("timestamp", log.getTimeStamp());
                jsonLog.put("level", log.getLevel().toString());
                jsonLog.put("logger", log.getLoggerName());
                jsonLog.put("thread", log.getThreadName());
                jsonLog.put("message", log.getFormattedMessage());
                jsonArray.put(jsonLog);
            }

            byte[] responseBytes = jsonArray.toString().getBytes();

            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(200, responseBytes.length);

            try (OutputStream out = exchange.getResponseBody()) {
                out.write(responseBytes);
            }
        }
    }

    // Get Blockchain
    public class blockchainHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            JSONObject blockchain = localNode.getBlockchainJSON();

            byte[] responseBytes = blockchain.toString().getBytes();
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(200, responseBytes.length);

            try (OutputStream out = exchange.getResponseBody()) {
                out.write(responseBytes);
            }
        }
    }

    // Get Network
    public class networkPeersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            JSONObject networkPeers = localNode.getNetworkPeersJSON();

            byte[] responseBytes = networkPeers.toString().getBytes();
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(200, responseBytes.length);

            try (OutputStream out = exchange.getResponseBody()) {
                out.write(responseBytes);
            }

        }
    }

    public class connectHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("POST")) {
                exchange.sendResponseHeaders(400, 0);
                return;
            }

            // Reads raw body of HTTP request
            InputStream in =  exchange.getRequestBody();

            // Convert from InputStream to String
            String body = new BufferedReader(new InputStreamReader(in)).lines().reduce("", (a, b) -> a + b);

            // Close InputStream
            in.close();

            try {
                JSONObject JSONbody = new JSONObject(body);
                String Host =  JSONbody.getString("host");
                int Port =  JSONbody.getInt("port");

                localNode.connectToNewPeer(Host, Port);

                String response = "Connected to peer '" + Host + ":" + Port + "'";
                exchange.sendResponseHeaders(200, response.length());

                try (OutputStream out = exchange.getResponseBody()) {
                    out.write(response.getBytes());
                }
            } catch (IOException e) {
                logger.warn("Unable to connect to peer");

                String response = "Connection failed: "  + e.getMessage();
                exchange.sendResponseHeaders(400, response.length());

                try (OutputStream out = exchange.getResponseBody()) {
                    out.write(response.getBytes());
                }
            }
        }
   }
}



