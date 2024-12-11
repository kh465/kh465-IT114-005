package Project.Server;

import Project.Common.ConnectionPayload;
import Project.Common.DMPayload;
import Project.Common.LoggerUtil;
import Project.Common.Payload;
import Project.Common.PayloadType;
import Project.Common.RollPayload;
import Project.Common.RoomResultsPayload;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader; //kh465 12/7/24
import java.io.FileWriter; //kh465 12/8/24
import java.io.IOException; //kh465 12/7/24
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A server-side representation of a single client.
 * This class is more about the data and abstracted communication
 */
public class ServerThread extends BaseServerThread {
    public static final long DEFAULT_CLIENT_ID = -1;
    private Room currentRoom;
    private long clientId;
    private String clientName;
    private ArrayList<String> muteList = new ArrayList<String>(); //kh465 12/04/24
    private boolean isMuted = false;
    private Consumer<ServerThread> onInitializationComplete; // callback to inform when this object is ready
    private String filePath;

    /**
     * Wraps the Socket connection and takes a Server reference and a callback
     * 
     * @param myClient
     * @param server
     * @param onInitializationComplete method to inform listener that this object is
     *                                 ready
     */
    protected ServerThread(Socket myClient, Consumer<ServerThread> onInitializationComplete) {
        Objects.requireNonNull(myClient, "Client socket cannot be null");
        Objects.requireNonNull(onInitializationComplete, "callback cannot be null");
        info("ServerThread created");
        // get communication channels to single client
        this.client = myClient;
        this.clientId = ServerThread.DEFAULT_CLIENT_ID;// this is updated later by the server
        this.onInitializationComplete = onInitializationComplete;

    }

    public void setClientName(String name) {
        if (name == null) {
            throw new NullPointerException("Client name can't be null");
        }
        this.clientName = name;
        onInitialized();
    }

    public String getClientName() {
        return clientName;
    }

    public long getClientId() {
        return this.clientId;
    }

    protected Room getCurrentRoom() {
        return this.currentRoom;
    }

    protected void setCurrentRoom(Room room) {
        if (room == null) {
            throw new NullPointerException("Room argument can't be null");
        }
        currentRoom = room;
    }

    @Override
    protected void onInitialized() {
        onInitializationComplete.accept(this); // Notify server that initialization is complete
    }

    @Override
    protected void info(String message) {
        LoggerUtil.INSTANCE.info(String.format("ServerThread[%s(%s)]: %s", getClientName(), getClientId(), message));
    }

    @Override
    protected void cleanup() {
        currentRoom = null;
        super.cleanup();
    }

    @Override
    protected void disconnect() {
        // sendDisconnect(clientId, clientName);
        super.disconnect();
    }

    // handle received message from the Client
    @Override
    protected void processPayload(Payload payload) {
        try {
            switch (payload.getPayloadType()) {
                case CLIENT_CONNECT:
                    ConnectionPayload cp = (ConnectionPayload) payload;
                    setClientName(cp.getClientName());
                    //kh465 12/7/24
                    filePath = "Project/" + cp.getClientName() + ".txt";
                    try (BufferedReader br = new BufferedReader(new FileReader(filePath)))
                    {
                        LoggerUtil.INSTANCE.info(String.format("Loading %s's mutelist: %s.txt", cp.getClientName(), cp.getClientName()));
                        String user;
                        while ((user = br.readLine()) != null)
                            muteList.add(user);
                    }
                    catch (IOException e)
                    {
                        LoggerUtil.INSTANCE.severe(filePath + " either does not exist or can't be found!");
                        e.printStackTrace();
                    }
                    break;
                case MESSAGE:
                    currentRoom.sendMessage(this, payload.getMessage());
                    break;
                case ROOM_CREATE:
                    currentRoom.handleCreateRoom(this, payload.getMessage());
                    break;
                case ROOM_JOIN:
                    currentRoom.handleJoinRoom(this, payload.getMessage());
                    break;
                case ROOM_LIST:
                    currentRoom.handleListRooms(this, payload.getMessage());
                    break;
                case DISCONNECT:
                    //kh465 12/8/24
                    filePath = "Project/" + getClientName() + ".txt";
                    try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath)))
                    {
                        for (String line : muteList)
                        {
                            bw.write(line);
                            bw.newLine();
                        }
                        LoggerUtil.INSTANCE.info(String.format("Successfully wrote %s.txt to %s", getClientName(), filePath));
                    }
                    catch (IOException e)
                    {
                        LoggerUtil.INSTANCE.severe(String.format("Could not write to %s!", filePath));
                        e.printStackTrace();
                    }
                    currentRoom.disconnect(this);
                    break;
                case ROLL: //kh465 11/22/24
                    RollPayload rollPayload = (RollPayload) payload;
                    currentRoom.handleRoll(this, rollPayload.getArg1(), rollPayload.getArg2());
                    break;
                case FLIP: //kh465 11/22/24
                    currentRoom.handleFlip(this);
                    break;
                case DM: //kh465 11/26/24
                    DMPayload dmPayload = (DMPayload) payload;
                    currentRoom.sendDM(this, dmPayload.getRecClientId(), dmPayload.getMessage());
                    break;
                case MUTE: //kh465 11/26/24
                    DMPayload mutePayload = (DMPayload) payload;
                    currentRoom.handleMute(this, mutePayload.getRecClientName());
                    break;
                case UNMUTE: //kh465 11/26/24
                    DMPayload unmutePayload = (DMPayload) payload;
                    currentRoom.handleUnmute(this, unmutePayload.getRecClientName());
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            LoggerUtil.INSTANCE.severe("Could not process Payload: " + payload,e);
        
        }
    }

    // send methods to pass data back to the Client

    public void addToMuteList(String clientIdToMute) //kh465 11/26/24
    {
        if (!muteList.contains(clientIdToMute))
            muteList.add(clientIdToMute);
    }

    public void removeFromMuteList(String clientIdToUnmute) //kh465 11/26/24
    {
        if (muteList.contains(clientIdToUnmute))
            muteList.remove(clientIdToUnmute);
    }

    public boolean isMuted(String mutedClientId) //kh465 11/26/24
    {
        return muteList.contains(mutedClientId);
    }
    
    public boolean sendMute(long clientId, long mutedClientId) //kh465 11/26/24
    {
        DMPayload mp = new DMPayload();
        mp.setRecClientId(mutedClientId);
        mp.setClientId(clientId);
        return send(mp);
    }

    public boolean sendUnmute(long clientId, long unmutedClientId) //kh465 11/26/24
    {
        DMPayload mp = new DMPayload();
        mp.setRecClientId(unmutedClientId);
        mp.setClientId(clientId);
        return send(mp);
    }
    
    public boolean sendRoll(long clientId, int arg1, int arg2) //kh465 11/22/24
    {
        RollPayload rp = new RollPayload(arg1, arg2);
        rp.setPayloadType(PayloadType.ROLL);
        rp.setClientId(clientId);
        return send(rp);
    }
    
    public boolean sendRooms(List<String> rooms) {
        RoomResultsPayload rrp = new RoomResultsPayload();
        rrp.setRooms(rooms);
        return send(rrp);
    }

    public boolean sendClientSync(long clientId, String clientName) {
        ConnectionPayload cp = new ConnectionPayload();
        cp.setClientId(clientId);
        cp.setClientName(clientName);
        cp.setConnect(true);
        cp.setPayloadType(PayloadType.SYNC_CLIENT);
        return send(cp);
    }

    /**
     * Overload of sendMessage used for server-side generated messages
     * 
     * @param message
     * @return @see {@link #send(Payload)}
     */
    public boolean sendMessage(String message) {
        return sendMessage(ServerThread.DEFAULT_CLIENT_ID, message);
    }

    /**
     * Sends a message with the author/source identifier
     * 
     * @param senderId
     * @param message
     * @return @see {@link #send(Payload)}
     */
    public boolean sendMessage(long senderId, String message) {
        Payload p = new Payload();
        p.setClientId(senderId);
        p.setMessage(message);
        p.setPayloadType(PayloadType.MESSAGE);
        return send(p);
    }


    public boolean sendDM(long senderId, String recipientName, String message) //kh465 11/26/24
    {
        DMPayload dmp = new DMPayload();
        dmp.setClientId(clientId);
        //dmp.setRecClientId(recipientId);
        dmp.setMessage(message);
        dmp.setPayloadType(PayloadType.DM);
        return send(dmp);
    }

    /**
     * Tells the client information about a client joining/leaving a room
     * 
     * @param clientId   their unique identifier
     * @param clientName their name
     * @param room       the room
     * @param isJoin     true for join, false for leaivng
     * @return success of sending the payload
     */
    public boolean sendRoomAction(long clientId, String clientName, String room, boolean isJoin) {
        ConnectionPayload cp = new ConnectionPayload();
        cp.setPayloadType(PayloadType.ROOM_JOIN);
        cp.setConnect(isJoin); // <-- determine if join or leave
        cp.setMessage(room);
        cp.setClientId(clientId);
        cp.setClientName(clientName);
        return send(cp);
    }

    /**
     * Tells the client information about a disconnect (similar to leaving a room)
     * 
     * @param clientId   their unique identifier
     * @param clientName their name
     * @return success of sending the payload
     */
    public boolean sendDisconnect(long clientId, String clientName) {
        ConnectionPayload cp = new ConnectionPayload();
        cp.setPayloadType(PayloadType.DISCONNECT);
        cp.setConnect(false);
        cp.setClientId(clientId);
        cp.setClientName(clientName);
        return send(cp);
    }

    /**
     * Sends (and sets) this client their id (typically when they first connect)
     * 
     * @param clientId
     * @return success of sending the payload
     */
    public boolean sendClientId(long clientId) {
        this.clientId = clientId;
        ConnectionPayload cp = new ConnectionPayload();
        cp.setPayloadType(PayloadType.CLIENT_ID);
        cp.setConnect(true);
        cp.setClientId(clientId);
        cp.setClientName(clientName);
        return send(cp);
    }

    // end send methods
}
