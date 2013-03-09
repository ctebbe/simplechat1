// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

package client;

import ocsf.client.*;
import common.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class overrides some of the methods defined in the abstract
 * superclass in order to give more functionality to the client.
 *
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;
 * @author Fran&ccedil;ois B&eacute;langer
 * @version July 2000
 */
@SuppressWarnings("unused")
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
    this.quitOnClose = true;
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
			clientUI.display("Logged in as "+this.loginid);
		}
	} catch (IOException e) {}
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
	  loginid = (message.substring(0, loginidIndex)).toLowerCase();
	  
	  if(this.blockList.contains(loginid)) {
		  //System.out.println("Blocked user message");
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
		} else if(command.equals("#unblock") && arg != null) {
			sendToServer("#removeblock "+arg.toLowerCase());
			this.blockList.remove(arg);
		} else if(command.equals("#unblock") && arg == null) {
			this.blockList.clear();
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


private void addToBlockList(String arg) throws IOException {
	arg = arg.toLowerCase();
	sendToServer("#addblock "+arg);
	this.blockList.add(arg);
	clientUI.display("Added user to block list: "+arg);
}

private void setPort(String arg) {
	if(isConnected()) {
		clientUI.display("Must disconnect from server. Use #logoff and try again.");
	} else if(arg == null) {
		clientUI.display("No port argument. Use: #setport <port>");
	} else {
		setPort(Integer.parseInt(arg));
	}
}

private void logoff() throws IOException {
	this.setQuitOnClose(false); // disable client from quitting upon losing connection to server
	closeConnection();
}

private void changeHost(String arg) {
	if(isConnected()) {
		clientUI.display("Must disconnect from server. Use #logoff and try again.");
	} else if(arg == null) {
		clientUI.display("No host argument. Use: #sethost <host>");
	} else {
		setHost(arg);
	}
}

/**
   * (non-Javadoc)
   * @see ocsf.client.AbstractClient#connectionClosed()
   * closes the client when connection to the server is lost
   */
  public void connectionClosed() {
	  if(this.quitOnClose()) {
		  clientUI.display("Lost connection to Server.  Terminating client.");
		  quit();
	  }
	  this.setQuitOnClose(true); // re-enable quitting on close
  }

  /**
   * This method terminates the client.
   */
  public void quit()
  {
    try
    {
      closeConnection();
    }
    catch(IOException e) {}
    System.exit(0);
  }
  
  private void setQuitOnClose(boolean quit) {
	  this.quitOnClose = quit;
  }
  
  private boolean quitOnClose() {
	  return this.quitOnClose;
  }
}
//End of ChatClient class
