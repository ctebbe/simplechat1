// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

package client;

import ocsf.client.*;
import common.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


/**
 * This class overrides some of the methods defined in the abstract
 * superclass in order to give more functionality to the client.
 *
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;
 * @author Fran&ccedil;ois B&eacute;langer
 * @version July 2000
 */

/**
 * CS 314 implementation
 * @author calebtebbe
 * @author zachkaplan
 * @version March 2013
 *
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
	Timer timer; // used to schedule a reminder to change status after 5 mins
	boolean setTimer; // use this to control the setting of timer depending if idle or not
	private String userLoginID;
	// user status codes
	private final static int ONLINE = 0;
	private final static int IDLE = 1;
	private final static int UNAVAIL = 2;
	private final static int OFFLINE = 3;

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
		this.timer = new Timer();
		userLoginID = loginid;
		setTimer = true;
		login(loginid);
		//openConnection();
	}


	//Instance methods ************************************************

	private void login(String loginid) {
		try {
			if(isConnected()) {
				System.out.println("Currently connected to the server. #logoff and try again.");
			} else {
				openConnection();
				sendToServer("#login "+userLoginID);
				sendStatusToServer("online");
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
		clientUI.display(msg.toString());
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
				login(arg);

			} else if(command.equals("#gethost")) {
				clientUI.display("Current host: "+getHost());

			} else if(command.equals("#getport")) {
				clientUI.display("Current port: "+getPort());

			} else if(command.equals("#block")) {
				sendToServer(command+" "+arg);

			} else if(command.equals("#whoiblock")) {
				sendToServer(command);
				
			} else if(command.equals("#unblock")) {
				sendToServer(command+" "+arg);
				
			} else if(command.equals("#whoblocksme")) {
				sendToServer(command);

			} else if(command.equals("#setpassword")) {
				sendToServer(command+" "+arg);

			} else if(command.equals("#getpassword")) {
				sendToServer(command);

			} else if(command.equals("#password")) {
				sendToServer(command+" "+arg);

			// #channel <channelname> <- create a new or join an existing channel
			// #channel <channelname> <msg> <- send a message to the specified channel
			} else if(command.equals("#channel")) {
				if(arg2 == null) {
					sendToServer(command+" "+arg);
				} else {
					sendToServer(command+" "+arg+" "+arg2);
				}

			// #status <user> <- check current status of a user
			// #status <channelname> <- check status of a channel you are in
			// #status <validstatus> <- change your current status
			} else if(command.equals("#status")) {
				if(!(sendStatusToServer(arg))) { // check if changing a status or inquiring about a channel/user
					sendToServer(command+" "+arg);
				}

			} else if(command.equals("#available")) {
				sendStatusToServer("online");

			} else if(command.equals("#notavailable")) {
				sendStatusToServer("unavailable");
				setTimer = false;
				resetTimer();

			} else if(command.equals("#private")) {
				sendToServer(command+" "+arg+" "+arg2);

			} else if(command.equals("#forward")) {
				sendToServer(command+" "+arg);

			} else {
				System.out.println("Illegal command. Use: #command <arg>");
			}
		} catch(IOException e) {

		}
	}
	
	// changes status, if not valid status return false
	private boolean sendStatusToServer(String statusString) throws IOException {
		
		if(statusString.equalsIgnoreCase("online")) {
			sendToServer("#status "+ONLINE);
			return true;
		} else if(statusString.equalsIgnoreCase("idle")) {
			sendToServer("#status "+IDLE);
			return true;
		} else if(statusString.equalsIgnoreCase("unavailable")) {
			sendToServer("#status "+UNAVAIL);
			return true;
		} else if(statusString.equalsIgnoreCase("offline")) {
			sendToServer("#status "+OFFLINE);
			return true;
		}
		return false;
	}

	public void sendToServer(Object msg) throws IOException {
		
		super.sendToServer(msg);
		
		if(!setTimer) { // coming back from being idle
			//super.sendToServer("#status "+ONLINE);
			setTimer = true;
		}
		resetTimer();
	}
	
	// cancels current timer and sets a new one
	private void resetTimer() throws IOException {
		timer.cancel(); // stop the current timer thread
		timer = new Timer(); // cancel clears all schedule threads...restarting the timer to be scheduled
		if(setTimer) { // check if need to reset the timer
			timer.schedule(new IdleTimer(), 300*1000); // set a new reminder for 300 seconds or 5 mins if not canceled before then
		} 
	}
	
	private void idleTimerActivated() throws IOException {
		setTimer = false;
		resetTimer(); // stop the current timer
		super.sendToServer("#status "+IDLE);
	}

	class IdleTimer extends TimerTask {
		public void run() {
			try {
				idleTimerActivated();
			} catch (IOException e) {}
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
		sendStatusToServer("offline");
		clientUI.display("Logged off.");
		closeConnection();
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
			clientUI.display("Terminating connection");
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
