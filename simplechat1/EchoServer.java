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
	}


	//Instance methods ************************************************
	
	@SuppressWarnings("unchecked")
	public void sendToAllClients(ConnectionToClient sender, Object msg) throws IOException { // eventually do all client block filtering here
		
		String senderID = null;
		if(sender == null) {
			senderID = "server";
		} else { // coming from a connected client
			senderID = ((String) sender.getInfo("loginid")).toLowerCase();
		}
		
		for(ConnectionToClient c : clientList) {
			if( !(((ArrayList<String>)c.getInfo("blocklist")).contains(senderID)) ) {
				c.sendToClient(msg);
			}
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
					if( !(blockList.contains( ((String)client.getInfo("loginid")).toLowerCase() )) ) { // check server blocklist
						System.out.println("Message received "+ msg + " from " + client.getInfo("loginid"));
					}
					sendToAllClients(client, loginid+"> "+msg);
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
		int argIndex = command.indexOf(' ');
		if(argIndex != -1) {
			arg = (command.substring(argIndex)).trim();
			command = (command.substring(command.indexOf('#'), argIndex)).trim();
		}
		
		// print loginid if logged in or the hostname if not
		if(isUserConnected((String) client.getInfo("loginid"))) {
			System.out.println("Command received "+ command + " from " + client.getInfo("loginid"));
		} else {
			System.out.println("Command received "+ command + " from " + client);
		}
		
		
		try {
			//if( (client.getInfo("loginid") == null) ) { // check if user has a loginid
			if( !(isUserConnected((String) client.getInfo("loginid"))) ) { // check if user is connected and chatting or a new user
				
				if(command.equals("#login")) { // client log in
					handleClientLoginAttempt(client, arg, null); // handle a new login
				
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
				addClientBlock(client, arg);
			} else if(command.equals("#unblock")) {
				removeFromClientBlockList(client, arg);
			} else if(command.equals("#whoiblock")) {
				whoClientBlocks(client);
			} else if(command.equals("#setpassword")) {
				setClientPassword(client, arg);
			} else if(command.equals("#getpassword")) {
				getClientPassword(client);
			} else if(command.equals("#status")) {
				handleClientStatus(client, arg);
			}
		} catch (IOException e) {}
	}

	private void handleClientStatus(ConnectionToClient client, String arg) throws IOException {
		
		if(clientPasswordMap.containsKey(arg)) { // client asking about a user's status
			sendClientUserStatus(client, arg);
		} else {
			setClientStatus(client, arg);
		}
	}


	private void sendClientUserStatus(ConnectionToClient client, String user) throws IOException {
		ConnectionToClient c = getClient(user);
		client.sendToClient("> User "+user+" is "+getStatusString((Integer) c.getInfo("status")));
	}


	private String getStatusString(Integer info) {
		// TODO Auto-generated method stub
		return null;
	}


	private ConnectionToClient getClient(String user) {
		for(ConnectionToClient c : clientList) {
			if(((String) c.getInfo("loginid")).equalsIgnoreCase(user)) {
				return c;
			}
		}
		return null;
	}


	private void setClientStatus(ConnectionToClient client, String arg) throws IOException {
		
		int clientStatus = Integer.parseInt(arg);
		
		switch(clientStatus) {
		case ONLINE:
			client.setInfo("status", ONLINE);
			client.sendToClient("> You are now Online.");
			break;
		case IDLE:
			client.setInfo("status", IDLE);
			client.sendToClient("> You are now Idle.");
			break;
		case UNAVAIL:
			client.setInfo("status", UNAVAIL);
			client.sendToClient("> You are now Unavailable.");
			break;
		case OFFLINE:
			client.setInfo("status", OFFLINE);
			client.sendToClient("> You are now Offline.");
			break;
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
		
		if(isUserConnected(loginid)) { // user online with id already
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
		setClientStatus(client, Integer.toString(ONLINE));
		clientList.add(client);
		if(!clientPasswordMap.containsKey(loginid)) { // dont override an existing password
			clientPasswordMap.put(loginid, null);
		}
			
		System.out.println(client.getInfo("loginid") +" has logged on");
		sendToAllClients("> "+client.getInfo("loginid")+ " has logged on");
	}


	private void setClientPassword(ConnectionToClient client, String arg) throws IOException {
		if(clientPasswordMap.containsKey(client.getInfo("loginid"))) {
			clientPasswordMap.remove(client.getInfo("loginid")); // temporarily remove user
			clientPasswordMap.put((String) client.getInfo("loginid"), arg); // re-add user with new password
			client.sendToClient("> New password successfully set.");
		}
	}

	//Adds a block that a client requests
	@SuppressWarnings("unchecked")
	private void addClientBlock(ConnectionToClient client, String blockee) throws IOException {
		if(doesUserExist(blockee)) {
			if( ((ArrayList<String>)client.getInfo("blocklist")).contains(blockee.toLowerCase()) ) {
				client.sendToClient("> Messages from user "+blockee+" already blocked.");
			} else {
				((ArrayList<String>)client.getInfo("blocklist")).add(blockee.toLowerCase());
				client.sendToClient("> Messages from user "+blockee+" will now be blocked.");
			}
		} else {
			client.sendToClient("> Cannot block user that does not exist.");
		}
	}

	// check if a certain loginid is actively chatting through the server
	private boolean isUserConnected(String loginid) {
		if(loginid == null) return false;
		for(ConnectionToClient c : this.clientList) {
			if( ((String)c.getInfo("loginid")).equalsIgnoreCase(loginid) ) {
				return true;
			}
		}
		if(loginid.equalsIgnoreCase("server")) return true;
		return false;
	}
	
	// check if a certain loginid is currently chatting or has ever logged in before...server too
	private boolean doesUserExist(String loginid) {
		if(clientPasswordMap.containsKey(loginid) || loginid.equalsIgnoreCase("server")) {
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


	//Used by the server to block non-command messages from clients
	public void addToServerBlockList(String arg) {
		if(arg != null && doesUserExist(arg)) {
			this.blockList.add(arg.toLowerCase());
			System.out.println("Messages from "+arg+" will now be blocked.");
			
		} else { // no command or illegal user
			System.out.println("User does not exist.");
		}
	}

	//Server side unblock method
	public void removeFromServerBlockList(String blockee){
		if(this.blockList.isEmpty()) { // no blocks
			System.out.println("No blocking is in effect");
			return;
		}
		
		if(blockee == null) { // remove everyone from block list
			for(String user : this.blockList) {
				this.blockList.remove(user.toLowerCase());
				System.out.println("Messages from "+user+" will now be displayed.");
			}
			
		} else if( !(this.blockList.contains(blockee.toLowerCase())) ) { // asking to unblock a user that was not blocked
			System.out.println("Messages from "+blockee+" are already displayed.");
			
		} else {
			this.blockList.remove(blockee.toLowerCase());
			System.out.println("Messages from "+blockee+" will now be displayed.");
		}
	}
	
	// client remove block
	@SuppressWarnings("unchecked")
	private void removeFromClientBlockList(ConnectionToClient client, String blockee) throws IOException{
		
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
		if(isUserConnected((String) client.getInfo("loginid"))) {
				sendToAllClients("> "+client.getInfo("loginid") + " has disconnected");
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
