import java.net.*;
import java.io.*;

public class Server {
    int serverPort;

    public void initServer(int serverPort) {
        this.serverPort = serverPort;
    }

    public void startServer() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
            System.out.println("Server started on port " + serverPort);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted from " + clientSocket.getInetAddress().getHostName());

                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                String incommingMessage = in.readLine();
                System.out.println("Recieved: " + incommingMessage);

                out.println("Ack: " + incommingMessage);

            }
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        }
    }
}