// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

package client;

import ocsf.client.*;
import common.*;
import java.io.*;
import java.util.ArrayList;


/**
 * This class overrides some of the methods defined in the abstract
 * superclass in order to give more functionality to the client.
 *
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;
 * @author Fran&ccedil;ois B&eacute;langer
 * @version July 2000
 */
public class ChatClient extends AbstractClient
{
	//Instance variables **********************************************

	/**
	 * The interface type variable.  It allows the implementation of 
	 * the display method in the client.
	 */
	ChatIF clientUI; 
	private boolean quitOnClose; // use this to "logoff" and not terminate the client
	private String loginid;
	private ArrayList<String> blockList;

	//Constructors ****************************************************

	/**
	 * Constructs an instance of the chat client.
	 *
	 * @param host The server to connect to.
	 * @param port The port number to connect on.
	 * @param clientUI The interface type variable.
	 */

	public ChatClient(String host, int port, ChatIF clientUI, String loginid) throws IOException 
	{
		super(host, port); //Call the superclass constructor
		this.clientUI = clientUI;
		this.quitOnClose = false;
		this.loginid = loginid;
		this.blockList = new ArrayList<String>();
		login();
		//openConnection();
	}


	//Instance methods ************************************************

	private void login() {
		try {
			if(isConnected()) {
				System.out.println("Currently connected to the server. #logoff and try again.");
			} else {
				openConnection();
				sendToServer("#login "+this.loginid);
			}
		} catch (IOException e) {
			System.out.println("Cannot open connection. Awaiting command.");
		}
	}

	/**
	 * This method handles all data that comes in from the server.
	 *
	 * @param msg The message from the server.
	 */
	public void handleMessageFromServer(Object msg) 
	{
		if( !(isMessageFromBlockedClient( (msg.toString()) )) ) {
			clientUI.display(msg.toString());
		}    
	}

	private boolean isMessageFromBlockedClient(String message) {
		// server format: loginid> message
		int loginidIndex = message.indexOf('>');
		String loginid = "";
		loginid = (message.substring(0, loginidIndex));

		if(blockList.contains(loginid)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * This method handles all data coming from the UI            
	 *
	 * @param message The message from the UI.    
	 */
	public void handleMessageFromClientUI(String message)
	{
		message = message.trim();
		if((message).startsWith("#")) { // handle special commands
			handleCommandFromClientUI(message);
		} else { // if not a command send message to server
			try
			{
				sendToServer(message);
			}
			catch(IOException e)
			{
				clientUI.display("Could not send message to server.  Terminating client.");
				quit();
			}
		}
	}

	private void handleCommandFromClientUI(String command) {
		// pull argument from command if there is any
		String arg = null;
		int argIndex = command.indexOf(' ');
		if(argIndex != -1) {
			arg = (command.substring(argIndex)).trim();
			command = (command.substring(command.indexOf('#'), argIndex)).trim();
		}

		// parse command
		try {
			if(command.equals("#quit")) {
				quit();
			} else if(command.equals("#logoff")) {
				setQuitOnClose(true);
				logoff();

			} else if(command.equals("#sethost")) {
				changeHost(arg);

			} else if(command.equals("#setport")) {
				setPort(arg);

			} else if(command.equals("#login")) {
				login();

			} else if(command.equals("#gethost")) {
				clientUI.display("Current host: "+getHost());

			} else if(command.equals("#getport")) {
				clientUI.display("Current port: "+getPort());


			} else if(command.equals("#block")) {
				addToBlockList(arg);

			} else if(command.equals("#whoiblock")) {
				clientUI.display("Block list:"+ getWhoIBlockString());

			} else if(command.equals("#unblock")) {
		
				if(arg == null){
					arg = "";
				}
				removeFromBlockList(arg);

			} else if(command.equals("#whoblocksme")) {
				sendToServer(command);

			} else {
				System.out.println("Illegal command. Use: #command <arg>");
			}
		} catch(IOException e) {

		}
	}

	public String getWhoIBlockString() {
		String blockList = "";
		for(String s:this.blockList) {
			blockList += s+" ";
		}
		return blockList.trim();
	}

	//Adds a specified user to the block list
	private void addToBlockList(String arg) throws IOException {
		if(arg.equalsIgnoreCase(loginid)) {
			clientUI.display("You cannot block the sending of messages to yourself.");
		}
		else if(arg.equalsIgnoreCase("server")){
			sendToServer("#addblock "+arg);
			if(!blockList.contains("SERVER")){
				blockList.add("SERVER");
			}
		}
		else {
			sendToServer("#addblock "+arg);
			if(!blockList.contains(arg)){
				blockList.add(arg);
			}		
		}
	}

	//Removes specific user from block list. If no user is specified, remove everyone.
	private void removeFromBlockList(String arg) throws IOException{

		if(arg.equals(loginid)){
			clientUI.display("Cannot unblock yourself because you can't block yourself!");	
		}
		else if(arg == ""){
			if(blockList.isEmpty()){
				clientUI.display("No blocking is in effect.");
			}
			else{
				System.out.println("oh hi");
				if(!blockList.isEmpty()){
					while(!blockList.isEmpty()){
						int position = blockList.size() - 1;
						sendToServer("#removeblock "+blockList.get(position));
						blockList.remove(position);
						clientUI.display("Messages from " +blockList.get(position)+ "will now be displayed");
					}
				}
				else{
					clientUI.display("No blocking is in effect.");
				}

			}
		}
		else {
			if(arg.equalsIgnoreCase("server") && blockList.contains("SERVER"))
			{
				sendToServer("#removeblock " + "SERVER");
				blockList.remove("SERVER");
				clientUI.display("Messages from " + arg + " will now be displayed");
			}
		else if(blockList.contains(arg)){
				sendToServer("#removeblock " +arg);
				blockList.remove(arg);
				clientUI.display("Messages from " + arg + " will now be displayed");
			}
			else{
				clientUI.display("Messages from " + arg + " were not blocked.");
			}
		}

	}

	private void setPort(String arg) {
		if(isConnected()) {
			clientUI.display("Must disconnect from server. Use #logoff and try again.");
		} else if(arg == null) {
			clientUI.display("No port argument. Use: #setport <port>");
		} else {
			setPort(Integer.parseInt(arg));
			clientUI.display("Port set to: " + arg);
		}
	}

	private void logoff() throws IOException {
		closeConnection();
		clientUI.display("Connectioned closed.");
	}

	private void changeHost(String arg) {
		if(isConnected()) {
			clientUI.display("Must disconnect from server. Use #logoff and try again.");
		} else if(arg == null) {
			clientUI.display("No host argument. Use: #sethost <host>");
		} else {
			setHost(arg);
			clientUI.display("Host set to: " + arg);
		}
	}

	/**
	 * (non-Javadoc)
	 * @see ocsf.client.AbstractClient#connectionClosed()
	 * closes the client when connection to the server is lost
	 */
	public void connectionClosed() {
		if(!quitOnClose()) {
			clientUI.display("Abnormal termination of connection");
		}
	}

	/**
	 * This method terminates the client.
	 */
	public void quit()
	{
		System.out.println("Terminating client...");
		try
		{
			closeConnection();
		}
		catch(IOException e) {
			System.exit(0);
		}
		System.exit(0);
	}

	private void setQuitOnClose(boolean quit) {
		quitOnClose = quit;
	}

	private boolean quitOnClose() {
		return quitOnClose;
	}
}
//End of ChatClient class
