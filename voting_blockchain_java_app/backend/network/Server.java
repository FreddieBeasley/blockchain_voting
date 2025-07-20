import java.io.*;
import java.net.*;

// Driver class
public class Server {
    public static void main(String[] args) {
        try{
            // Initialise server
            ServerSocket serverSocket = new ServerSocket(1000);
            System.out.println("Server Started");

            // Wait for client connection
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client Connected");

            // Open input and output streams
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            // Read client message
            String message = in.readLine();
            System.out.println("client:  " + message);

            // Reply message
            out.println("Message Received");

            // Close connections
            in.close();
            out.close();
            clientSocket.close();
            serverSocket.close();
        } catch (IOException e) {
            System.out.println("Server Error: " + e.getMessage());
        }
    }
}