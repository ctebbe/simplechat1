// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

import java.io.*;

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
  
  //Constructors ****************************************************
  
  /**
   * Constructs an instance of the echo server.
   *
   * @param port The port number to connect on.
   */
  public EchoServer(int port) 
  {
    super(port);
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
		  handleClientCommand(msg.toString().trim(), client);
	  } else { // not a command
		  if(loginid != null) {
			  sendToAllClients(loginid+"> "+msg);
		  } else {
			  System.out.println("Client sending message but not logged in.");
		  }
	  }
  }
  
  private void handleClientCommand(String command, ConnectionToClient client) {
	  
	// pull argument from command if there is any
	String arg = null;
	int argIndex = command.indexOf(' ');
	if(argIndex != -1) {
		arg = (command.substring(argIndex)).trim();
		command = (command.substring(command.indexOf('#'), argIndex)).trim();
	}
	
	try{
		if( (client.getInfo("loginid") == null) && !(command.equals("#login")) ) { // check user has a loginid and if doesnt then make sure its calling #login
			client.close();
		} else if(command.equals("#login")) { // start adding commands here
			client.setInfo("loginid", arg);
		} else {
			
		}
	} catch(Exception e) {}
	
  }


/**
   * called when a client connects
   */
  protected void clientConnected(ConnectionToClient client) {
	System.out.println("Client connected.");
  }
  
  /**
   * called when a client disconnected
   */
  synchronized protected void clientDisconnected(ConnectionToClient client) {
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
