// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

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
	HashMap<String, ArrayList<String>> clientBlockList;
	private ArrayList<String> serverBlockList;
	private ArrayList<String> clientList;
	//Constructors ****************************************************

	/**
	 * Constructs an instance of the echo server.
	 *
	 * @param port The port number to connect on.
	 */
	public EchoServer(int port) 
	{
		super(port);
		//clientList = new ArrayList<ConnectionToClient>();
		clientBlockList = new HashMap<String, ArrayList<String>>();
		serverBlockList = new ArrayList<String>();
		clientList = new ArrayList<String>();
		clientList.add("server"); //Add server to client list so it can be blocked by clients
	}


	//Instance methods ************************************************

	/**
	 * This method handles any messages received from the client.
	 *
	 * @param msg The message received from the client.
	 * @param client The connection from which the message originated.
	 */
	public void handleMessageFromClient(Object msg, ConnectionToClient client) {

		String loginid = (String) client.getInfo("loginid");
		if( (((msg.toString()).trim()).startsWith("#")) ) { // handle command
			handleClientCommand(msg.toString(), client);
		} else { // not a command
			if(loginid != null && serverBlockList.contains(client.getInfo("loginid"))) {
				sendToAllClients(loginid+"> "+msg);
			}
			else if(loginid != null && !serverBlockList.contains(client.getInfo("loginid"))){
				System.out.println("Message received "+ msg + " from " + client.getInfo("loginid"));
				sendToAllClients(loginid+"> "+msg);
			} else {

				System.out.println("Client sending message but not logged in.");
			}
		}
	}

	private void handleClientCommand(String command, ConnectionToClient client) {

		// pull argument from command if there is any
		command = command.trim();
		String arg = null;
		int argIndex = command.indexOf(' ');
		if(argIndex != -1) {
			arg = (command.substring(argIndex)).trim();
			command = (command.substring(command.indexOf('#'), argIndex)).trim();
		}
		System.out.println("Message received "+ command + " from " + client.getInfo("loginid"));
		//System.out.println("arg:"+arg);
		if( (client.getInfo("loginid") == null) ) { // check user has a loginid and if doesnt then make sure its calling #login
			if(command.equals("#login")) {
				client.setInfo("loginid", arg);
				clientList.add(arg);
				System.out.println(client.getInfo("loginid") + " has logged on");
				sendToAllClients("> "+client.getInfo("loginid") + " has logged on");
				sendToAllClients(client.getInfo("loginid") + " has logged on");
			} else {
				System.out.println("Command recieved from client but not logged in.");
				try {
					client.close();
				} catch (IOException e) {}
			}
		} else if(command.equals("#whoblocksme")) {
			sendClientWhoBlocksThem((String)client.getInfo("loginid"));
		} else if(command.equals("#addblock")) {
			addClientBlock(client,(String)client.getInfo("loginid"), arg);
		} else if(command.equals("#removeblock")) {
			removeClientBlock((String)client.getInfo("loginid"), arg);
		}
		else {

		}

	}

	private void removeClientBlock(String blocker, String blockee) {
		if(blockee.equalsIgnoreCase("SERVER")){
			clientBlockList.get(blocker).remove("server");
		}
		else{
			clientBlockList.get(blocker).remove(blockee);
		}
	}


	private void addClientBlock(ConnectionToClient client, String blocker, String blockee) {
		try {
			System.out.println(clientBlockList.get(blocker));
			if (clientList.contains(blockee)){
				ArrayList<String> blockList = clientBlockList.get(blocker);
				if (blockList == null) {
					blockList = new ArrayList<String>();
					clientBlockList.put(blocker, blockList);
				}
				if(blockList.contains(blockee)){
					client.sendToClient("> " + "Messages from " + blockee + " were already blocked.");
				}
				else{
					blockList.add(blockee);
					client.sendToClient("> " + "Messages from " + blockee + " will be blocked.");
				}
			} 
			else{
				client.sendToClient("> " + "User " + blockee + " does not exist.");
			}
		} catch (IOException e) {
			System.out.println("ERROR: Connection to client lost. Could not modify block list.");
		}
	}


	private void sendClientWhoBlocksThem(String client) {
		// client.sendToClient() doesnt work for some reason..
		System.out.println("looking for users who block:"+client);
		for(String user : this.clientBlockList.keySet()) {
			System.out.println("checking in block list belonging to:"+user);
			for(String blocked : clientBlockList.get(user)) {
				System.out.println("blocks: "+blocked+" "+client.equals(blocked));
				if( client.equals(blocked) ) {
					sendToAllClients("blocked by "+client+" "+blocked);
				}
			}
		}

	}

	//Used by the server the block non-command messages from clients
	public void addToServerBlockList(String arg) {

		if(clientList.contains(arg)){
			if(arg.equalsIgnoreCase("server")) {
				System.out.println("You cannot block the sending of messages to yourself.");
			}
			else {
				if(!serverBlockList.contains(arg)){
					serverBlockList.add(arg);
					System.out.println("User " + arg + " added to block list.");
				}
				else{
					System.out.println("Messages from " + arg + " were already blocked");
				}
			}
		}
		else{
			System.out.println("User " + arg + " does not exist");
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
