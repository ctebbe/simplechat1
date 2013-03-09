// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

import java.io.*;
import java.util.ArrayList;

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
  private ArrayList<ConnectionToClient> clientList;
  private ArrayList<String> blockList;
  
  //Constructors ****************************************************
  
  /**
   * Constructs an instance of the echo server.
   *
   * @param port The port number to connect on.
   */
  public EchoServer(int port) 
  {
    super(port);
    clientList = new ArrayList<ConnectionToClient>();
    blockList = new ArrayList<String>();
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
		  if(loginid != null) {
			  System.out.println("Message recieved from: "+loginid);
			  sendToAllClients(loginid+"> "+msg);
		  } else {
			  System.out.println("Client sending message but not logged in.");
		  }
	  }
  }
  
  @SuppressWarnings("unchecked")
private void handleClientCommand(String command, ConnectionToClient client) {
	  
	// pull argument from command if there is any
	command = command.trim();
	String arg = null;
	int argIndex = command.indexOf(' ');
	if(argIndex != -1) {
		arg = (command.substring(argIndex)).trim();
		command = (command.substring(command.indexOf('#'), argIndex)).trim();
	}
	//System.out.println("command:"+command);
	//System.out.println("arg:"+arg);
	if( (client.getInfo("loginid") == null) ) { // check user has a loginid and if doesnt then make sure its calling #login
		if(command.equals("#login")) {
			client.setInfo("loginid", arg);
			client.setInfo("blocklist", new ArrayList<String>());
			this.clientList.add(client);
		} else {
			System.out.println("Command recieved from client but not logged in.");
			try {
				client.close();
			} catch (IOException e) {}
		}
	} else if(command.equals("#whoblocksme")) {
		sendClientWhoBlocksThem(client);
	} else if(command.equals("#addblock")) {
		System.out.println("server adding block:"+arg+" from "+client.getInfo("loginid"));
		((ArrayList<String>) client.getInfo("blocklist")).add(arg.toLowerCase());
	} else if(command.equals("#removeblock")) {
		((ArrayList<String>) client.getInfo("blocklist")).remove(arg);
	} else {
		
	}
	
  }

@SuppressWarnings("unchecked")
private void sendClientWhoBlocksThem(ConnectionToClient client) {
	String idToSearch = ((String) client.getInfo("loginid")).toLowerCase();
	String clientBlockString = "";
	System.out.println("id to search:"+idToSearch);
	for(ConnectionToClient c : this.clientList) {
		ArrayList<String> cBlockList = (ArrayList<String>) c.getInfo("blocklist");
		if(cBlockList.contains(idToSearch)) {
			System.out.println(c.getInfo("loginid")+" blocking "+client.getInfo("loginid"));
			clientBlockString += (String) c.getInfo("loginid")+" ";
		}
	}
	try {
		Object msg = (Object) "Who blocks you:"+clientBlockString.trim();
		System.out.println("Sending message to client");
		((ConnectionToClient) client).sendToClient(msg);
	} catch (IOException e) {System.out.println("Cannot send to client");}
}

public void addToBlockList(String arg) {
	if(arg != null) {
		this.blockList.add(arg);
	}
}

/**
   * called when a client connects
   */
  protected void clientConnected(ConnectionToClient client) {
	clientList.add(client);
	System.out.println("Client connected.");
  }
  
  /**
   * called when a client disconnected
   */
  synchronized protected void clientDisconnected(ConnectionToClient client) {
	  clientList.remove(client);
	  System.out.println("Client disconnected.");
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
      System.out.println("ERROR - Could not listen for clients!");
    }
    server.accept();
  }
}
//End of EchoServer class
