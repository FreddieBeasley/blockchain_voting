import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) throws IOException {
        // Connects to server on the same machine at port 1000
        Socket socket = new Socket("127.0.0.1", 1000);
        System.out.println("Connected to " + socket.getInetAddress().getHostName());

        // Take input from user
        BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
        /*
        System.in takes a user input from keyboard as bytes
        InputStreamReader() converts the input into characters
        BufferedReader() allows for efficient line by line reading
         */

        // Preparing client input
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        /*
        socket.getInputStream() returns bytes steam of the clients input
        InputStreamReader(...) converts the bytes into characters
        BufferReader(...) allows for efficient line by line reading
         */

        // Preparing client output
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        /*
        clientSocket.getOutputStream() returns byte stream to send to the client
        PrintWriter(...) converts byte stream to text based writer
        true enable autoflush after a line of the output stream is send
         */

        // Communicating
        String message = userInput.readLine();
        while (!message.equals("exit")) {
            out.println(message); // sends message
            String serverMessage = in.readLine(); // recieves response
            System.out.println("Server response: " + serverMessage);
            System.out.println("Enter new message: ");
            message = userInput.readLine();
        }

        // Closing socket connection
        in.close();
        out.close();
        socket.close();
    }
}