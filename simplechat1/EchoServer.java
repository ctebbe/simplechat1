// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

/**
 * CS 314
 * @author calebtebbe
 * @author zachkaplan
 *
 */

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import ocsf.server.*;

/**
 * This class overrides some of the methods in the abstract 
 * superclass in order to give more functionality to the server.
 *
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;re
 * @author Fran&ccedil;ois B&eacute;langer
 * @author Paul Holden
 * @version July 2000
 * 
 */
public class EchoServer extends AbstractServer 
{
	//Class variables *************************************************

	/**
	 * The default port to listen on.
	 */
	final public static int DEFAULT_PORT = 5555;
	private ArrayList<String> blockList; // an updated list of all server blocks
	private ArrayList<ConnectionToClient> clientList; // an updated list of all currently connected clients
	private HashMap<String, String> clientPasswordMap; // a hashmap to keep track of all existing users and their passwords (if any)
	private HashMap<String, ArrayList<ConnectionToClient>> channelMap; // channel name and arraylist of clients in that channel
	
	//status codes
	private final static int ONLINE = 0;
	private final static int IDLE = 1;
	private final static int UNAVAIL = 2;
	private final static int OFFLINE = 3;
	
	//Constructors ****************************************************

	/**
	 * Constructs an instance of the echo server.
	 *
	 * @param port The port number to connect on.
	 */
	public EchoServer(int port) 
	{
		super(port);
		blockList = new ArrayList<String>();
		clientList = new ArrayList<ConnectionToClient>();
		clientPasswordMap = new HashMap<String, String>(); 
		channelMap = new HashMap<String, ArrayList<ConnectionToClient>>();
	}


	//Instance methods ************************************************
	
	// returns the id of c or server if null
	public String getLoginID(ConnectionToClient c) {
		if(c == null) {
			return "SERVER";
		} else {
			return (String) c.getInfo("loginid");
		}
	}
	
	private ConnectionToClient getClient(String user) {
		for(ConnectionToClient c : clientList) {
			if(((String) c.getInfo("loginid")).equalsIgnoreCase(user)) {
				return c;
			}
		}
		return null;
	}
	
	private boolean checkClientMessagingStatus(ConnectionToClient c) {
		int status = (Integer) c.getInfo("status");
		switch(status) {
		case ONLINE:
			return true;
		case IDLE:
			return true;
		case UNAVAIL:
			return false;
		case OFFLINE:
			return false;
		default:
			return false;
		}
	}
	
	private String getStatusString(Integer statusCode) {
		switch(statusCode) {
		case ONLINE:
			return "ONLINE";
		case IDLE:
			return "IDLE";
		case UNAVAIL:
			return "UNAVAILABLE";
		case OFFLINE:
			return "OFFLINE";
		default:
			return "";
		}
	}
	
	private boolean setClientStatus(ConnectionToClient client, Integer clientStatus) throws IOException {
		
		//int currentStatus = (Integer) client.getInfo("status"); // dont send client status change if already that status
		switch(clientStatus) {
		case ONLINE:
			//if( !(currentStatus == ONLINE) ) {
				client.setInfo("status", ONLINE);
				client.sendToClient("> You are now Online.");
			//}
			return true;
		case IDLE:
			client.setInfo("status", IDLE);
			client.sendToClient("> You are now Idle.");
			return true;
		case UNAVAIL:
			client.setInfo("status", UNAVAIL);
			client.sendToClient("> You are now Unavailable.");
			return true;
		case OFFLINE:
			client.setInfo("status", OFFLINE);
			client.sendToClient("> You are now Offline.");
			return true;
		//default: // ignore an invalid status
			//client.sendToClient("> Invalid status");
		}
		return false;
	}
	
	public void sendToAllClients(ConnectionToClient sender, Object msg) throws IOException { // 
		
		if(sender != null && (Integer) sender.getInfo("status") == IDLE) {
			setClientStatus(sender, ONLINE);
		}
		for(ConnectionToClient c : clientList) {
			checkClientToClientMessage(sender, c, (String) msg);
		}
		//super.sendToAllClients(msg);
	}

	/**
	 * This method handles any messages received from the client.
	 *
	 * @param msg The message received from the client.
	 * @param client The connection from which the message originated.
	 * @throws IOException 
	 */
	//Handles messages from client
	public void handleMessageFromClient(Object msg, ConnectionToClient client) {
		
		try{
			String loginid = (String) client.getInfo("loginid");
			if( (((msg.toString()).trim()).startsWith("#")) ) { // handle command
				handleClientCommand(msg.toString(), client);
				
			} else { // not a command
				if(loginid != null) { // user logged in
					if( !(blockList.contains( getLoginID(client).toLowerCase() )) ) { // check server blocklist
						System.out.println("Message received "+ msg + " from " + getLoginID(client));
					}
					if(!checkClientMessagingStatus(client)) {
						client.sendToClient("> You are currently unavailable. Try #available and resend.");
					} else {
						sendToAllClients(client, msg);
					}
				} else { // user not logged in
					System.out.println("Client sending message but not logged in.");
				}
			}
			
		} catch(IOException e) {}
	}

	//Handles client commands
	private void handleClientCommand(String command, ConnectionToClient client) {

		// pull argument from command if there is any
		command = command.trim();
		String arg = null;
		String arg2 = null;
		int argIndex = command.indexOf(' ');
		if(argIndex != -1) {
			arg = (command.substring(argIndex)).trim();
			command = (command.substring(command.indexOf('#'), argIndex)).trim();
			
			int argIndex2 = arg.indexOf(' ');
			if(argIndex2 != -1) {
				arg2 = (arg.substring(argIndex2)).trim();
				arg = (arg.substring(0, argIndex2)).trim();
			}
		}
		
		
		
		// print loginid if logged in or the hostname if not
		if(isUserConnected(client)) {
			System.out.println("Command received "+ command + " from " + getLoginID(client));
		} else {
			System.out.println("Command received "+ command + " from " + client);
		}
		/*
		System.out.println(command);
		System.out.println(arg);
		System.out.println(arg2);*/
		
		try {
			//if( (client.getInfo("loginid") == null) ) { // check if user has a loginid
			if( !(isUserConnected(client)) ) { // check if user is connected and chatting or a new user
				
				if(command.equals("#login")) { // client log in
					//clientLogin(client, arg);
					handleClientLoginAttempt(client, arg, arg2); // handle a new login
				
				} else if(command.equals("#password")) {
					handleClientPasswordAttempt(client, arg); // handle a client attempting to enter a password
					
				} else { // client requesting server use without logging in
					System.out.println("Command recieved from client but not logged in.");
					client.sendToClient("> Must be logged in. Try #login <loginid>.");
					//client.close();
				}
				
			// parse commands that can be sent from client to server 	
			} else if(command.equals("#whoblocksme")) {
				whoBlocksClient(client);
			} else if(command.equals("#block")) {
				addBlock(client, arg);
			} else if(command.equals("#unblock")) {
				removeFromBlockList(client, arg);
			} else if(command.equals("#whoiblock")) {
				whoClientBlocks(client);
			} else if(command.equals("#setpassword")) {
				setClientPassword(client, arg);
			} else if(command.equals("#getpassword")) {
				getClientPassword(client);
			} else if(command.equals("#status")) {
				handleStatusCommand(client, arg);
			} else if(command.equals("#channel")) {
				handleChannelCommand(client, arg, arg2);
			} else if(command.equals("#forward")) {
				setClientForward(client, arg);
			} else if(command.equals("#private")) {
				sendPrivateMessage(client, arg, arg2);
			}
			if((Integer) client.getInfo("status") == IDLE) {
				setClientStatus(client, ONLINE);
			}
		} catch (IOException e) {
			System.out.println("IOException thrown");
		}
	}

	private void sendPrivateMessage(ConnectionToClient sender, String user, String msg) throws IOException {
		
		ConnectionToClient sendee = getClient(user);
		ConnectionToClient forwardClient = getClient( (String) sendee.getInfo("forward") );
		
		if(!isUserConnected(sendee)) {
			sender.sendToClient("> Client "+user+" is not connected");
		}
		
		// check blocking
		if(!(checkBlock(sendee, sender))) { // sendee not blocking the sender
			if(checkClientMessagingStatus(sendee)) { // online or idle
				sendToClient(sender, sendee, "[PVT:"+getLoginID(sender)+"] "+msg);
			} else {
				sendClientUserStatus(sender, sendee);
			}
		} else {
			sender.sendToClient("> Messages to user "+getLoginID(sender)+" are currently blocked");
		}
		
		// check forwarding...dont care if fowarding client is blocking the message
		if(forwardClient != null) { // sendee is forwarding messages to another client
			if(checkClientMessagingStatus(forwardClient)) { // online or idle
				sendToClient(sender, forwardClient, "[PVT:"+getLoginID(sender)+"] "+"[FWD:"+getLoginID(sendee)+"] "+msg);
			}
		}
	}


	private void setClientForward(ConnectionToClient client, String arg) throws IOException {
		
		ConnectionToClient forwardClient = getClient(arg);
		if(checkBlock(client, forwardClient)) {
			client.sendToClient("> Cannot add forward due to block");
		} else if(!doesUserExist(forwardClient)) {
			client.sendToClient("> Cannot forward to client that does not exist");
		} else if(!isUserConnected(forwardClient)) {
			client.sendToClient("> Cannot forward to user currently disconnected from the server");
		} else {
			client.setInfo("forward", arg);
			client.sendToClient("> Set forwarding to:"+arg);
		}
	}


	// parses a channel command from client
	public void handleChannelCommand(ConnectionToClient client, String channelName, String msg) throws IOException {
			
		String loginid = getLoginID(client);
		if(msg == null) { // creating a new channel or requesting to join existing channel
			if(!this.checkClientMessagingStatus(client)) { // check if client is available
				client.sendToClient("> You are currently unavailable. Try #available and resend");
			} else if(channelMap.containsKey(channelName)) { // join existing channel
				channelMap.get(channelName).add(client);
				client.sendToClient("> Joined existing channel "+channelName);
			} else { // create a new channel
				channelMap.put(channelName, new ArrayList<ConnectionToClient>());
				channelMap.get(channelName).add(client);
				client.sendToClient("> Created new channel "+channelName);
			}
		} else if(channelMap.containsKey(channelName) && channelMap.get(channelName).contains(client) || loginid.equalsIgnoreCase("server")) { // sending message to channel
			for(ConnectionToClient c : channelMap.get(channelName)) {
				checkClientToClientMessage(client, c, "[CHNL:"+channelName+"] "+msg);
			}
		} else {
			client.sendToClient("> No access in channel");
		}
	}

	@SuppressWarnings("unchecked")
	private boolean checkBlock(ConnectionToClient blocker, ConnectionToClient blockee) {
		if( ((ArrayList<String>) blocker.getInfo("blocklist")).contains(getLoginID(blockee).toLowerCase()) ) {
			return true;
		}
		return false;
	}


	// checks all aspects of a client sending a general message to another client including blocklist, forwarding list ect
	// a null value for either client represents the server
	private void checkClientToClientMessage(ConnectionToClient sender, ConnectionToClient sendee, String msg) throws IOException {
		
		ConnectionToClient forwardClient = getClient( (String) sendee.getInfo("forward") );
		
		// check blocking
		if(!(checkBlock(sendee, sender))) { // sendee not blocking the sender
			if(checkClientMessagingStatus(sendee) || sender == null) { // send client a server message even if unavailable
				sendToClient(sender, sendee, msg);
			} else {
				//sendClientUserStatus(sender, sendee);
			}
		} else {
			System.out.println("blocked...");
		}
		
		// check forwarding...dont care if fowarding client is blocking the message
		if(forwardClient != null) { // sendee is forwarding messages to another client
			if(!forwardClient.equals(sender) && checkClientMessagingStatus(forwardClient)) { // dont forward a message from the same client / check status
				sendToClient(sender, forwardClient, "[FWD:"+getLoginID(sendee)+"] "+msg);
			}
		} 
	}

	// this does not check anything, instead it contructs the proper senderID> msg format and sends it to client sendee
	private void sendToClient(ConnectionToClient sender, ConnectionToClient sendee, Object msg) throws IOException {
		sendee.sendToClient("> "+getLoginID(sender)+"> "+msg);
	}


	private void handleStatusCommand(ConnectionToClient client, String arg) throws IOException {
		
		if(clientPasswordMap.containsKey(arg)) { // client asking about a user's status
			sendClientUserStatus(client, getClient(arg));
			
		} else if(channelMap.containsKey(arg)) {
			sendClientChannelStatus(client, arg);
			
		} else { // setting own status
			if(!setClientStatus(client, Integer.parseInt(arg))) {
				client.sendToClient("> Not a valid user or channel");
			}
		}
	}


	private void sendClientChannelStatus(ConnectionToClient client, String channelName) throws IOException {
		if( channelMap.get(channelName).contains(client) ) { // client not in specified channel
			for(ConnectionToClient c : channelMap.get(channelName)) {
				sendClientUserStatus(client, c);
			}
		} else { // client not in channel or channel doesnt exist
			client.sendToClient("> You do not have access to channel "+channelName);
		}
	}


	private void sendClientUserStatus(ConnectionToClient client, ConnectionToClient statusToGet) throws IOException {

		if(statusToGet == null) { // non-existant or offline user
			client.sendToClient("> User "+statusToGet+" is OFFLINE.");
		} else {
			client.sendToClient("> User "+getLoginID(statusToGet)+" is "+getStatusString((Integer) statusToGet.getInfo("status")));
		}
	}

	private void getClientPassword(ConnectionToClient client) throws IOException {
		
		String password = clientPasswordMap.get((String) client.getInfo("loginid"));
		if(password != null) {
			client.sendToClient("> Password: "+password);
		} else {
			client.sendToClient("> No password set.");
		}
	}


	private void handleClientPasswordAttempt(ConnectionToClient client, String pw) throws IOException {
		
		System.out.println("Checking password attempt..."+pw);
		if(pw == null) {
			client.sendToClient("> Enter Password: #password <password>");
			return;
		} 
		
		String loginid = (String) client.getInfo("loginid");
		if(clientPasswordMap.containsKey(loginid)) {
			//System.out.println("Checking password attempt:"+pw+" with "+clientPasswordMap.get(loginid));
			if(clientPasswordMap.get(loginid).equals(pw)) {
				clientLogin(client, loginid); // allow the client to log in
			} else { // bad password...let them keep trying
				client.sendToClient("> Enter Password: #password <password>");
			}
		} 
	}


	private void handleClientLoginAttempt(ConnectionToClient client, String loginid, String password) throws IOException {
		
		if(loginid == null) {
			loginid = (String) client.getInfo("loginid");
		}
		
		if(isUserConnected(client)) { // user online with id already
			client.sendToClient("> Loginid "+loginid+" already in use. Try #login <loginid> with a new loginid.");
			//client.close();
			
		} else if(clientPasswordMap.containsKey(loginid)) { // new client connection of existing client
			if(clientPasswordMap.get(loginid).equals(password)) { // password hit
				clientLogin(client, loginid);
			} else {
				client.setInfo("loginid", loginid); // put this in the client reference temporarily to use when getting the password back
				client.sendToClient("> Enter Password: #password <password>");
			}
			
		} else { // brand new user to the server
			clientLogin(client, loginid);
		}
	}

	// logs in a new or existing client
	private void clientLogin(ConnectionToClient client, String loginid) throws IOException {
		
		client.setInfo("loginid", loginid); // store loginid
		client.setInfo("blocklist", new ArrayList<String>()); // init a new blocklist
		
		//setClientStatus(client, ONLINE);
		clientList.add(client);
		if(!clientPasswordMap.containsKey(loginid)) { // dont override an existing password
			clientPasswordMap.put(loginid, null);
		}
			
		System.out.println(getLoginID(client) +" has logged on");
		sendToAllClients("> "+getLoginID(client)+ " has logged on");
	}


	private void setClientPassword(ConnectionToClient client, String arg) throws IOException {
		if(clientPasswordMap.containsKey(client.getInfo("loginid"))) {
			clientPasswordMap.remove(client.getInfo("loginid")); // temporarily remove user
			clientPasswordMap.put(getLoginID(client), arg); // re-add user with new password
			client.sendToClient("> New password successfully set.");
		}
	}

	//Adds a block that a client requests or server block if client is null
	@SuppressWarnings("unchecked")
	public void addBlock(ConnectionToClient client, String blockee) throws IOException {
		
		if(client == null) { // server block
			if(blockee.equalsIgnoreCase("server")) {
				System.out.println("Cannot block yourself");
			} else if(doesUserExist(getClient(blockee))) {
				this.blockList.add(blockee.toLowerCase());
				System.out.println("Messages from "+blockee+" will now be blocked.");
				
			} else { // no command or illegal user
				System.out.println("User does not exist.");
			}
			
		} else { // client block
			if(getLoginID(client).equalsIgnoreCase(blockee)) {
				client.sendToClient("> Cannot block yourself");
				
			} else if(doesUserExist(getClient(blockee)) || blockee.equalsIgnoreCase("server")) {
				if( ((ArrayList<String>)client.getInfo("blocklist")).contains(blockee.toLowerCase()) ) {
					client.sendToClient("> Messages from user "+blockee+" already blocked.");
				} else {
					((ArrayList<String>)client.getInfo("blocklist")).add(blockee.toLowerCase());
					client.sendToClient("> Messages from user "+blockee+" will now be blocked.");
					
					// check if this changes any previous forwarding
					if(((String) getClient(blockee).getInfo("forward")).equalsIgnoreCase(getLoginID(client))) {
						setClientForward(getClient(blockee), null);
						getClient(blockee).sendToClient("> Forwarding removed due to block");
					}
				}
			} else {
				client.sendToClient("> Cannot block user that does not exist.");
			}
		}
	}

	// check if a certain loginid is actively chatting through the server
	private boolean isUserConnected(ConnectionToClient client) {
		return clientList.contains(client);
	}
	
	// check if a certain loginid is currently chatting or has ever logged in before...server too
	// return false if not in the password map or the server
	private boolean doesUserExist(ConnectionToClient c) {
		if(clientPasswordMap.containsKey(getLoginID(c))) {
			return true;
		}
		return false;
	}


	//Used to send clients messages about who blocks them
	@SuppressWarnings("unchecked")
	private void whoBlocksClient(ConnectionToClient client) throws IOException {

		String id = ((String) client.getInfo("loginid")).toLowerCase();
		boolean blocked = false;
		// check clients
		for(ConnectionToClient c : this.clientList) {
			if( ((ArrayList<String>)c.getInfo("blocklist")).contains(id) ) {
				client.sendToClient("> Messages to "+ (String) c.getInfo("loginid") +" are blocked.");
				blocked = true;
			}
		}
		
		// check server
		if( blockList.contains(id) ) {
			client.sendToClient("> Messages to server are blocked.");
			blocked = true;
		}
		
		if(!blocked) {
			client.sendToClient("> No users are blocking you.");
		}
	}

	//Used to found out which clients are blocking the server
	@SuppressWarnings("unchecked")
	public void whoBlocksServer() {
		boolean blocked = false;
		for(ConnectionToClient c : this.clientList) {
			if( ((ArrayList<String>)c.getInfo("blocklist")).contains("server") ) {
				System.out.println("Messages to "+ (String)c.getInfo("loginid") + " are currently being blocked.");
				blocked = true;
			}
		}
		if(!blocked) {
			System.out.println("No user is blocking you.");
		}
	}


	
	
	// removes block from client, if null remove block from server
	@SuppressWarnings("unchecked")
	public void removeFromBlockList(ConnectionToClient client, String blockee) throws IOException{
		
		if(client == null) { // removing from server block list
			if(this.blockList.isEmpty()) { // no blocks
				System.out.println("No blocking is in effect");
				return;
			}
			
			if(blockee == null) { // remove everyone from block list
				for(String user : this.blockList) {
					this.blockList.remove(user);
					System.out.println("Messages from "+user+" will now be displayed.");
				}
				
			} else if( !(this.blockList.contains(blockee.toLowerCase())) ) { // asking to unblock a user that was not blocked
				System.out.println("Messages from "+blockee+" are already displayed.");
				
			} else {
				this.blockList.remove(blockee.toLowerCase());
				System.out.println("Messages from "+blockee+" will now be displayed.");
			}
			
		} else { // from user blocklist
			ArrayList<String> blockList = (ArrayList<String>) client.getInfo("blocklist");
			if(blockList.isEmpty()) { // no blocks
				client.sendToClient("> No blocking is in effect");
				return;
			}
			
			if(blockee == null) { // remove everyone from block list
				for(String user : blockList) {
					blockList.remove(user.toLowerCase());
					client.sendToClient("> Messages from "+user+" will now be displayed.");
				}
				
			} else if( !(blockList.contains(blockee.toLowerCase())) ) { // asking to unblock a user that was not blocked
				client.sendToClient("> Messages from "+blockee+" are already displayed.");
				
			} else {
				blockList.remove(blockee.toLowerCase());
				client.sendToClient("> Messages from "+blockee+" will now be displayed.");
			}
		}
	}
	

	//Checks to see which clients the server is blocking
	public void whoIBlock(){
		if(blockList.isEmpty()){
			System.out.println("No blocking is in effect.");
		}
		else{
			for(String user : this.blockList){
				System.out.println("Messages from " +user+ " are blocked.");
			}
		}
	}
	
	//Checks to see which clients the server is blocking
	@SuppressWarnings("unchecked")
	public void whoClientBlocks(ConnectionToClient client) throws IOException{
		
		ArrayList<String> blockList = (ArrayList<String>) client.getInfo("blocklist");
		if(blockList.isEmpty()){
			client.sendToClient("> No blocking is in effect.");
		}
		else{
			for(String user : blockList){
				client.sendToClient("> Messages from " +user+ " are blocked.");
			}
		}
	}

	/**
	 * called when a client connects
	 */
	protected void clientConnected(ConnectionToClient client) {
		System.out.println("A new client is attempting to connect to the server.");
		//clientList.add(client);
	}

	/**
	 * called when a client disconnected
	 */
	synchronized protected void clientDisconnected(ConnectionToClient client) {
		// remove any forwarding to client disconnecting
		String id = getLoginID(client);
		for(ConnectionToClient c : clientList) {
			if( ((String) c.getInfo("forward")).equalsIgnoreCase(id) ) {
				try {
					c.sendToClient("> Fowarding stopped due to disconnection");
				} catch (IOException e) {}
			}
		}
		if(isUserConnected(client)) {
				sendToAllClients("> "+getLoginID(client)+ " has disconnected");
		}
		clientList.remove(client);
	}

	/**
	 * This method overrides the one in the superclass.  Called
	 * when the server starts listening for connections.
	 */
	protected void serverStarted()
	{
		System.out.println
		("Server listening for connections on port " + getPort());
	}

	/**
	 * This method overrides the one in the superclass.  Called
	 * when the server stops listening for connections.
	 */
	protected void serverStopped()
	{
		System.out.println
		("Server has stopped listening for connections.");
	}



	//Class methods ***************************************************

	/**
	 * This method is responsible for the creation of 
	 * the server instance (there is no UI in this phase).
	 *
	 * @param args[0] The port number to listen on.  Defaults to 5555 
	 *          if no argument is entered.
	 */
	public static void main(String[] args) 
	{
		int port = 0; //Port to listen on

		try
		{
			port = Integer.parseInt(args[0]); //Get port from command line
		}
		catch(Throwable t)
		{
			port = DEFAULT_PORT; //Set port to 5555
		}

		EchoServer sv = new EchoServer(port);
		ServerConsole server = new ServerConsole(sv);

		try 
		{
			sv.listen(); //Start listening for connections
		} 
		catch (Exception ex) 
		{
			System.out.println("ERROR: Port occupied! Could not listen for clients! Terminating...");
			System.exit(1);
		}
		server.accept();
	}
}
//End of EchoServer class
