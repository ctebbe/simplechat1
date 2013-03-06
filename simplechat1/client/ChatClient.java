// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

package client;

import ocsf.client.*;
import common.*;
import java.io.*;

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
  
  //Constructors ****************************************************
  
  /**
   * Constructs an instance of the chat client.
   *
   * @param host The server to connect to.
   * @param port The port number to connect on.
   * @param clientUI The interface type variable.
   */
  
  public ChatClient(String host, int port, ChatIF clientUI) throws IOException 
  {
    super(host, port); //Call the superclass constructor
    this.clientUI = clientUI;
    this.quitOnClose = true;
    this.loginid = null;
    //openConnection();
  }
  
  public ChatClient(String host, int port, ChatIF clientUI, String loginid) throws IOException 
  {
    super(host, port); //Call the superclass constructor
    this.clientUI = clientUI;
    this.quitOnClose = true;
    this.loginid = loginid;
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
	} catch (IOException e) {}
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
	if((message.trim()).startsWith("#")) { // handle special commands
		handleCommandFromClientUI(message.trim());
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
			System.out.println("Current host: "+getHost());
		} else if(command.equals("#getport")) {
			System.out.println("Current port: "+getPort());
		} else {
			System.out.println("Illegal command. Use: #command <arg>");
		}
	} catch(Exception e) {
		
	}
  }

private void setPort(String arg) {
	if(isConnected()) {
		System.out.println("Must disconnect from server. Use #logoff and try again.");
	} else if(arg == null) {
		System.out.println("No port argument. Use: #setport <port>");
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
		System.out.println("Must disconnect from server. Use #logoff and try again.");
	} else if(arg == null) {
		System.out.println("No host argument. Use: #sethost <host>");
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
