package app.resources.network;

import app.resources.NetworkManager;
import org.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class MessageSender {
    private static final Logger logger =  LoggerFactory.getLogger(MessageSender.class);
    private final NetworkManager networkManager;

    public MessageSender(NetworkManager networkManager) {
        this.networkManager = networkManager;
    }

        public void send(String host, int port, JSONObject message){
            try(
                    Socket socket = new Socket(host, port);
                    OutputStream out = socket.getOutputStream();
                    PrintWriter writer = new PrintWriter(out, true);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            ) {
                String messageString = message.toString();
                writer.println(messageString);
                logger.info("Message sent to " + host + ":" + port);

                JSONObject response = new JSONObject(reader.readLine());

                networkManager.handleIncomingResponse(response);


            } catch (Exception e) {
                logger.error("Failed to send message to " + host + ":" + port, e);
        }

    }
}
