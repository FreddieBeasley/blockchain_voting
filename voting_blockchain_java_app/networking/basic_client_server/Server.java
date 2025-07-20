import java.net.*;
import java.io.*;

public class Server {
    public static void main(String[] args) throws IOException {
        // Initialise Server
        ServerSocket serverSocket = new ServerSocket(1000);
        System.out.println("Server started on port: " + serverSocket.getLocalPort());

        // Waiting for client
        System.out.println("Waiting for connection...");
        Socket clientSocket = serverSocket.accept();
        System.out.println("Connection accepted: " + clientSocket.getInetAddress());

        // Preparing server input
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        /*
        clientSocket.getInputStream() returns bytes steam of the clients input
        InputStreamReader(...) converts the bytes into characters
        BufferReader(...) allows for efficient line by line reading
         */

        // Preparing server output
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        /*
        clientSocket.getOutputStream() returns byte stream to send to the client
        PrintWriter(...) converts byte stream to text based writer
        true enable autoflush after a line of the output stream is send
         */

        // Echoing input
        String message = in.readLine();
        while (!message.equals("exit")) {
            System.out.println("Message received from: " + message);
            out.println("Echo: " + message);
            message = in.readLine();
        }

        // Closing socket connection
        in.close();
        out.close();
        clientSocket.close();
        serverSocket.close();

    }
}