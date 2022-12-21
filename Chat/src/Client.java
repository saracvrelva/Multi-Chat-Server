import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {

        if (args.length != 1 ){
            System.out.println("What's your name?");
            Scanner scn = new Scanner(System.in);
            String name = scn.nextLine();
            Client client = new Client(name);
            client.sendMessage.start();
            client.readMessage.start();

        }else {
            Client client = new Client(args[0]);
            client.sendMessage.start();
            client.readMessage.start();
        }

    }

    private final Socket clientSocket;

    public Client(String name) {
        try {
            //IP address
            InetAddress hostname = InetAddress.getLoopbackAddress();
            // it's the server port
            int port = 8002;
            //Creates a stream socket and connects it to the specified port number at the specified IP address of the server
            this.clientSocket = new Socket(hostname, port);
            //send the client's name to the server
            PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true);
            output.println(name);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //send message to the server that will send the message to all the connected clients
    Thread sendMessage = new Thread() {
        @Override
        public void run() {
            while (!clientSocket.isClosed()) {

                Scanner scn = new Scanner(System.in);

                // read the message
                String msg = scn.nextLine();

                try {
                    PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true);

                    // write the message on the output stream
                    // sends it to the server
                    output.println(msg);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    Thread readMessage = new Thread() {
        public void run() {
            while (!clientSocket.isClosed()) {
                try {
                    BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                    // read the message sent to this client
                    //readUTF() --> reads in a string that has been encoded using a modified UTF-8 format. The string of character is decoded from the UTF and returned as String.
                    String msg = input.readLine();

                    if(msg != null) {
                        System.out.println(msg);
                    } else {
                        clientSocket.close();
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };



}


