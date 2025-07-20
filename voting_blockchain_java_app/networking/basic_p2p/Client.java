import java.net.*;
import java.io.*;

public class Client {
    int targetPort;
    String targetHost;

    public void initClient(int targetPort, String targetHost) {
        this.targetPort = targetPort;
        this.targetHost = targetHost;
    }

    public void sendMessage(Str outgoingMessage) throws IOException {
        // Connect to port
        Socket socket = new Socket(targetHost, targetPort);
        System.out.println("Connected to " + socket.getInetAddress().getHostName());

        // Prepare input and output
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // Send message
        out.println(outgoingMessage);

        // Recieve reply
        String incommingMessage = in.readLine();
        out.println(incommingMessage);

    }

}