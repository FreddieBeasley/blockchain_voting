package app.resources.network;

import app.resources.NetworkManager;

import org.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class MessageReceiver implements Runnable {
    private final Logger logger;
    private final int port;
    private final NetworkManager networkManager;

    public MessageReceiver(int port, NetworkManager networkManager) {
        this.logger = LoggerFactory.getLogger(MessageReceiver.class);

        this.port = port;
        this.networkManager = networkManager;
    }

    @Override
    public void run() {
        try(ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("Message receiver listening on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void handleClient(Socket clientSocket) {
        try(
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
        ){
            String messageString;
            JSONObject message;

            try {
                messageString = reader.readLine();
                message = new JSONObject(messageString);
            } catch (IOException e) {
                return;
            }
            logger.info("New message received");

            JSONObject response;

            try {
                response = networkManager.handleIncomingMessage(message);
            } catch (Exception e) {
                return;
            }

            writer.println(response.toString());
            logger.info("Response sent");


        } catch (Exception e) {
            logger.warn("Failed to accept client message");
        }
    }

}