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
	private ArrayList<String> serverBlockList;
	private ArrayList<ConnectionToClient> clientList;
	
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
		serverBlockList = new ArrayList<String>();
		clientList = new ArrayList<ConnectionToClient>();
	}


	//Instance methods ************************************************

	/**
	 * This method handles any messages received from the client.
	 *
	 * @param msg The message received from the client.
	 * @param client The connection from which the message originated.
	 * @throws IOException 
	 */
	//Handles messages from client
	public void handleMessageFromClient(Object msg, ConnectionToClient client) {

		String loginid = (String) client.getInfo("loginid");
		if( (((msg.toString()).trim()).startsWith("#")) ) { // handle command
			handleClientCommand(msg.toString(), client);
			
		} else { // not a command
			if(loginid != null) { // user logged in
				if( !(serverBlockList.contains(client.getInfo("loginid"))) ) { // check server blocklist
					System.out.println("Message received "+ msg + " from " + client.getInfo("loginid"));
				}
				sendToAllClients(loginid+"> "+msg);
			} else { // user not logged in
				System.out.println("Client sending message but not logged in.");
			}
		}
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
		
		System.out.println("Command received "+ command + " from " + client.getInfo("loginid"));
		
		try {
			if( (client.getInfo("loginid") == null) ) { // check user has a loginid and if doesnt then make sure its calling #login
				
				if(command.equals("#login")) { // client log in
					
					client.setInfo("loginid", arg);
					client.setInfo("blocklist", new ArrayList<String>()); // init a new blocklist
					client.setInfo("status", ONLINE); // no assigned status yet
					clientList.add(client);
					
					System.out.println(client.getInfo("loginid") + " has logged on");
					sendToAllClients("> "+client.getInfo("loginid") + " has logged on");
					
				} else { // client requesting server use without logging in
					System.out.println("Command recieved from client but not logged in.");
					client.close();
				}
				
			// parse commands that can be sent from client to server 	
			} else if(command.equals("#whoblocksme")) {
				whoBlocksClient(client);
			} else if(command.equals("#addblock")) {
				addClientBlock(client, arg);
			} else if(command.equals("#removeblock")) {
				removeClientBlock(client, arg);
			}
			else {

			}
		} catch (IOException e) {}
	}

	//Removes a block that a client requests
	@SuppressWarnings("unchecked")
	private void removeClientBlock(ConnectionToClient client, String blockee) {
		((ArrayList<String>)client.getInfo("blocklist")).remove(blockee);
	}

	//Adds a block that a client requests
	@SuppressWarnings("unchecked")
	private void addClientBlock(ConnectionToClient client, String blockee) throws IOException {
		if(isConnectedUser(blockee)) {
			if( ((ArrayList<String>)client.getInfo("blocklist")).contains(blockee.toLowerCase()) ) {
				client.sendToClient("> Messages from user "+blockee+" already blocked.");
			} else {
				((ArrayList<String>)client.getInfo("blocklist")).add(blockee.toLowerCase());
				client.sendToClient("> Mesages from user "+blockee+" will now be blocked.");
			}
		} else {
			client.sendToClient("> Cannot block user not logged in.");
		}
	}


	private boolean isConnectedUser(String loginid) {
		if(loginid.equalsIgnoreCase("server")) return true;
		for(ConnectionToClient c : this.clientList) {
			if( ((String)c.getInfo("loginid")).equalsIgnoreCase(loginid) ) {
				return true;
			}
		}
		return false;
	}


	//Used to send clients messages about who blocks them
	@SuppressWarnings("unchecked")
	private void whoBlocksClient(ConnectionToClient client) throws IOException {

		String id = (String) client.getInfo("loginid");
		boolean blocked = false;
		for(ConnectionToClient c : this.clientList) {
			if( ((ArrayList<String>)c.getInfo("blocklist")).contains(id) ) {
				client.sendToClient("> Messages to "+ (String) c.getInfo("loginid") +" are blocked.");
				blocked = true;
			}
		}
		if(!blocked) {
			client.sendToClient("> No one is blocking you.");
		}
	}

	//Used to found out which clients are blocking the server
	@SuppressWarnings("unchecked")
	public void whoBlocksServer(){
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
		if(arg != null) {
			this.serverBlockList.add(arg);
			System.out.println("Messages from "+arg+" will now be blocked.");
		}
	}

	//Server side unblock method
	public void removeFromServerBlockList(String arg){
		if(clientList.contains(arg) || arg == "" || arg == null){
			if(arg == "" || arg == null){
				if(clientBlockList.get("server").isEmpty()){
					System.out.println("No blocking is in effect.");
				}
				else{
					if(!clientBlockList.get("server").isEmpty()){
						while(!clientBlockList.get("server").isEmpty()){
							int position = clientBlockList.get("server").size() - 1;
							String id = clientBlockList.get("server").get(position);
							clientBlockList.get("server").remove(position);
							System.out.println("Messages from " +id+ " will now be displayed");
						}
					}
					else{
						System.out.println("No blocking is in effect.");
					}

				}
			}
			else if(arg.equalsIgnoreCase("server")){
				System.out.println("Cannot unblock yourself because you can't block yourself!");	
			}
			else{
				if(clientList.contains(arg)){
					clientBlockList.get("server").remove(arg);
					System.out.println("Messages from " +arg+ " will now be displayed");
				}
			}

		}
		else{
			System.out.println("User " + arg + " does not exist");
		}
	}

	//Checks to see which clients the server is blocking
	public void whoIBlock(){
		if(clientBlockList.get("server").isEmpty()){
			System.out.println("No blocking is in effect.");
		}
		else{
			for(int i = 0; i < clientBlockList.get("server").size(); i++){
				System.out.println("Messages from " + clientBlockList.get("server").get(i) + " are blocked");
			}
		}
	}

	/**
	 * called when a client connects
	 */
	protected void clientConnected(ConnectionToClient client) {
		System.out.println("A new client is attempting to connect to the server.");
	}

	/**
	 * called when a client disconnected
	 */
	synchronized protected void clientDisconnected(ConnectionToClient client) {
		sendToAllClients("> "+client.getInfo("loginid") + " has disconnected");
		clientList.remove(client.getInfo("loginid"));
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
