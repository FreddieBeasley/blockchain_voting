// Responsible for sending ( postman )

/*

 */

import java.io.*;
import java.net.*;

// Driver Class
public class Client{

    public static void main(String[] args) {
        try{
            // Initialise client
            Socket socket = new Socket("localhost", 1000);
            System.out.println("Connected to the server");

            // Open input and output streams
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // Send data
            out.println("data");
            String response = in.readLine();
            System.out.println("Server Response: " + response);

            // Close connections
            in.close();
            out.close();
            socket.close();
        } catch (IOException e){
            System.out.println("Server Error: " + e.getMessage());
        }
    }
}