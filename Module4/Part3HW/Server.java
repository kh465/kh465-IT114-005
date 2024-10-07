package Module4.Part3HW;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays; //kh465 10/6/24
import java.util.Random; //kh465 10/6/24
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private int port = 3000;
    // connected clients
    // Use ConcurrentHashMap for thread-safe client management
    private final ConcurrentHashMap<Long, ServerThread> connectedClients = new ConcurrentHashMap<>();
    private boolean isRunning = true;

    //kh465 10/6/24
    private void coinToss(ServerThread user) //takes one argument (the user who initiated the command)
    {
        Random rng = new Random(); //create a new Random. this util was imported on line 6.
        int randNum = rng.nextInt(10) + 1; //set randNum to whatever rng.nextInt is. this generates a number between 0-9, 1 is added to make it 1-10
        String flipHeads = ("<- has flipped a coin and got Heads!");
        String flipTails = ("<- has flipped a coin and got Tails!");
        if (randNum % 2 == 0) //performing modulo on result. evens return 0 (heads), anything else is assumed tails. there is probably a better way of doing this.
            relay(flipHeads, user); //uses the relay method to send the message to all users, and also passes the user as an argument.
        else
            relay(flipTails, user);
    }
    
    //kh465 10/6/24
    private void diceRoll(int diceAmount, int diceSides, ServerThread user) //takes 3 arguments, the amount of dice, the sides on the dice, and the user who initiated the command
    {
        Random rng = new Random(); //create a new Random. this util was imported on line 6.
        int[] diceVal = new int[diceAmount]; //create an array of int named diceVal that is the length of the amount of dice (set by diceAmount)
        for (int i = 0; i < diceAmount; i++) //loops through the length of diceAmount
        {
            diceVal[i] += rng.nextInt((diceSides) + 1); //sets the index i to whatever rng.nextInt is. rng.nextInt is bound by diceSides + 1 so that it rolls between 1 and the user-set dice sides inclusive
        }
        String diceRoll = ("<- has rolled a " + diceAmount + "d" + diceSides + " and got: " + Arrays.toString(diceVal));
        relay(diceRoll, user); //uses the relay method to send the message to all users, and passes the user as an argument.
    }

    private void start(int port) {
        this.port = port;
        // server listening
        System.out.println("Listening on port " + this.port);
        // Simplified client connection loop
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (isRunning) {
                System.out.println("Waiting for next client");
                Socket incomingClient = serverSocket.accept(); // blocking action, waits for a client connection
                System.out.println("Client connected");
                // wrap socket in a ServerThread, pass a callback to notify the Server they're initialized
                ServerThread sClient = new ServerThread(incomingClient, this, this::onClientInitialized);
                // start the thread (typically an external entity manages the lifecycle and we
                // don't have the thread start itself)
                sClient.start();
            }
        } catch (IOException e) {
            System.err.println("Error accepting connection");
            e.printStackTrace();
        } finally {
            System.out.println("Closing server socket");
        }
    }
    /**
     * Callback passed to ServerThread to inform Server they're ready to receive data
     * @param sClient
     */
    private void onClientInitialized(ServerThread sClient) {
        // add to connected clients list
        connectedClients.put(sClient.getClientId(), sClient);
        relay(String.format("*User[%s] connected*", sClient.getClientId()), null);
    }
    /**
     * Takes a ServerThread and removes them from the Server
     * Adding the synchronized keyword ensures that only one thread can execute
     * these methods at a time,
     * preventing concurrent modification issues and ensuring thread safety
     * 
     * @param client
     */
    protected synchronized void disconnect(ServerThread client) {
        long id = client.getClientId();
        client.disconnect();
        connectedClients.remove(id);
        // Improved logging with user ID
        relay("User[" + id + "] disconnected", null);
    }

    /**
     * Relays the message from the sender to all connectedClients
     * Internally calls processCommand and evaluates as necessary.
     * Note: Clients that fail to receive a message get removed from
     * connectedClients.
     * Adding the synchronized keyword ensures that only one thread can execute
     * these methods at a time,
     * preventing concurrent modification issues and ensuring thread safety
     * 
     * @param message
     * @param sender ServerThread (client) sending the message or null if it's a server-generated message
     */
    protected synchronized void relay(String message, ServerThread sender) {
        if (sender != null && processCommand(message, sender)) {

            return;
        }
        // let's temporarily use the thread id as the client identifier to
        // show in all client's chat. This isn't good practice since it's subject to
        // change as clients connect/disconnect
        // Note: any desired changes to the message must be done before this line
        String senderString = sender == null ? "Server" : String.format("User[%s]", sender.getClientId());
        final String formattedMessage = String.format("%s: %s", senderString, message);
        // end temp identifier

        // loop over clients and send out the message; remove client if message failed
        // to be sent
        // Note: this uses a lambda expression for each item in the values() collection,
        // it's one way we can safely remove items during iteration
        
        connectedClients.values().removeIf(client -> {
            boolean failedToSend = !client.send(formattedMessage);
            if (failedToSend) {
                System.out.println(String.format("Removing disconnected client[%s] from list", client.getClientId()));
                disconnect(client);
            }
            return failedToSend;
        });
    }

    /**
     * Attempts to see if the message is a command and process its action
     * 
     * @param message
     * @param sender
     * @return true if it was a command, false otherwise
     */
    private boolean processCommand(String message, ServerThread sender) {
        if(sender == null){
            return false;
        }
        System.out.println("Checking command: " + message);
        // disconnect
        if ("/disconnect".equalsIgnoreCase(message)) {
            ServerThread removedClient = connectedClients.get(sender.getClientId());
            if (removedClient != null) {
                disconnect(removedClient);
            }
            return true;
        }
        else if ("/coin".equalsIgnoreCase(message)) //kh465 10/6/24
            coinToss(sender); //invokes method coinToss and passes along the sender as an argument
        else if (message.matches("/[1-99][d][1-99]")) //using regex to see if the message matches the typical connotation of die (#d#)
        {
            String dieAmtAndVal = (message.substring(1)); //removes the slash from the message and sets the String dieAmtAndVal to this string [this is probably a very scuffed way of doing this]
            String[] values = (dieAmtAndVal.split("[d]")); //creates an array of String from dieAmtAndVal that is split from the letter 'd' [probably very scuffed but i couldn't figure out another way]
            int dieAmt = Integer.parseInt(values[0]); //sets dieAmt to the first value of String[] values (presumably anything before the letter 'd')
            int dieVal = Integer.parseInt(values[1]); //sets dieVal to the second value of String[] values (presumably anything past the letter 'd')
            diceRoll(dieAmt, dieVal, sender); //calls the method diceRoll and passes along 3 arguments, dieAmt, dieVal and the sender
        }
        // add more "else if" as needed
        return false;
    }

    public static void main(String[] args) {
        System.out.println("Server Starting");
        Server server = new Server();
        int port = 3000;
        try {
            port = Integer.parseInt(args[0]);
        } catch (Exception e) {
            // can ignore, will either be index out of bounds or type mismatch
            // will default to the defined value prior to the try/catch
        }
        server.start(port);
        System.out.println("Server Stopped");
    }
}
