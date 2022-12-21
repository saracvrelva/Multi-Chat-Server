import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebServer {


    public static void main(String[] args) {
        WebServer webServer = new WebServer();
        webServer.connection();

    }

    private final ServerSocket serverSocket;

    private final int port = 8002;

    //list of clients connected to the chat
    private static Vector<ClientHandler> clients;

    public WebServer() {
        clients = new Vector<>();
        try {
            //Creates a server socket, bound to the specified port.
            this.serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //this method is responsible for maintaining the server waiting for new connections
    public void connection() {
        while (!serverSocket.isClosed()) {
            try {

                System.out.println("Chat Server is listening on port " + port);

                //Listens for a connection to be made to the server socket and accepts it. The method blocks until a connection is made.
                // we will set this property to a client socket trying to connect with this server
                Socket clientSocket = serverSocket.accept();

                //Reads the message/name sent by the client; saves it and close the stream
                BufferedReader inputName = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String clientName = inputName.readLine();

                // we instantiate a client handler that implements runnable
                // we give it a socket and a name
                ClientHandler clientHandler = new ClientHandler(clientSocket, clientName);

                //add the new client to the list of clients
                clients.add(clientHandler);

                System.out.println("New user connected! It has de port number: " + clientSocket.getPort() + ". Client's name: " + clientName + "\n");

                //Executor Service --> An Executor that provides methods to manage termination and methods that can produce a Future for tracking progress of one or more asynchronous tasks.
                // new Cached Thread Pool --> Creates a thread pool that creates new threads as needed, but will reuse previously constructed threads when they are available.
                ExecutorService cachedPool = Executors.newCachedThreadPool();

                // submit --> receives a runnable; Submits a Runnable task for execution and returns a Future representing that task.
                // makes the runnable that is passed to it invoke its method run
                cachedPool.submit(clientHandler);


            } catch (IOException ex) {
                System.out.println("The server went down: " + ex.getMessage());
            }
        }
    }
    //The Runnable interface should be implemented by any class whose instances are intended to be executed by a thread.
    private static class ClientHandler implements Runnable {

        //it will be passed a client's socket when a client connects to the server
        private final Socket clientSocket;

        // the client's name
        private final String name;

        public ClientHandler(Socket socket, String name) {
            this.clientSocket = socket;
            this.name = name;
        }


        public void run() {
            // The server will receive all the messages being sent
            // inside the method we will call se sendToAll() method to send the received messages to all the clients connected to the server
            receiveAll();

        }

        public void receiveAll() {
            while (!clientSocket.isClosed()) {
                try {
                    //Reads the message sent by the client
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                    // Reads the first line of text from the bufferedReader
                    String clientMessage = in.readLine();

                    sendToAll(clientMessage);

                } catch (IOException ex) {
                    System.out.println("Error: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }


        public void sendToAll(String clientMessage) {
            try {

                PrintWriter output;

                //evaluates if the client exists or not, when trying to private message
                boolean clientFound = false;

                //if the clients writes /pm <client's name> <message> , it will send the message to the other client if the client's name exists
                //if not, it will inform the current client that that client it's not in the chat
                if (clientMessage.split(" ")[0].equals("/pm")) {
                    String dmClient = clientMessage.split(" ")[1];
                    for (ClientHandler client : clients) {
                        if (client.name.equals(dmClient)) {
                            output = new PrintWriter(client.clientSocket.getOutputStream(), true);
                            output.println(this.name + ": " + clientMessage.split(dmClient, 2)[1]);
                            output = new PrintWriter(clientSocket.getOutputStream(), true);
                            output.println(this.name + ": " + clientMessage.split(dmClient, 2)[1]);
                            clientFound = true;
                        }
                    }
                     if (!clientFound){
                        output = new PrintWriter(clientSocket.getOutputStream(), true);
                        output.println(dmClient + " it's not in the chat!");
                    }
                }
                    //if the clients writes /quit his socket is closed, and he goes out of the system
                    // a message informing this client is out of the chat is sent to all the other clients in the chat
                else if (clientMessage.equals("/quit")) {
                    for (ClientHandler client : clients) {
                        output = new PrintWriter(client.clientSocket.getOutputStream(), true);
                        output.println(this.name + " left the chat!");
                    }
                        this.clientSocket.close();
                        //removes this client from the list of clients in the chat
                        clients.remove(this);
                }
                // if the client doesn't use /pm or /quit , just send a message to all the clients present in the chat
                else {
                    for (ClientHandler client : clients) {
                        output = new PrintWriter(client.clientSocket.getOutputStream(), true);
                        output.println(this.name + ": " + clientMessage);
                    }
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }


        }

    }
}
