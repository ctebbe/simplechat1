import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import ocsf.server.ConnectionToClient;

import common.ChatIF;

/**
 * CS 314
 * This class starts a server with echoserver as a shell
 * @author calebtebbe
 * @author zachkaplan
 *
 */
public class ServerConsole implements ChatIF {

	private static final int DEFAULT_PORT = 5556;
	EchoServer server;
	
	public ServerConsole(EchoServer echoServer) {
		this.server = echoServer;
	}


	public void display(String message) {
		System.out.println("SERVER> " +message);
		try {
			server.sendToAllClients(null, message);
		} catch (IOException e) {
		}
	}

	public void accept() {
		try {
			BufferedReader fromConsole = new BufferedReader(new InputStreamReader(System.in));
			String message;

			while (true) {
				message = fromConsole.readLine();
				if ((message.trim()).startsWith("#")) {
					handleServerCommand(message);
				} else {
					display(message);
				}
			}
		} catch (Exception ex) {
			//System.out.println("Unexpected error while reading from console!");
		}
	}

	@SuppressWarnings("unused")
	private void handleServerCommand(String command) {
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
		try {
			// parse command
			if(command.equals("#quit")) {
				quit();
			} else if(command.equals("#stop")) {
				display("WARNING - The server has stopped listening for connection");
				stop();
			} else if(command.equals("#close")) {
				display("WARNING - Server is shutting down!");
				close();
			} else if(command.equals("#setport")) {
				setPort(Integer.parseInt(arg));
			} else if(command.equals("#start")) {
				start();
			} else if(command.equals("#getport")) {
				getPort();
			} else if(command.equals("#block")) {
				server.addBlock(null, arg);
			} else if(command.equals("#unblock")){
				server.removeFromBlockList(null, arg);
			} else if(command.equals("#whoiblock")){
				server.whoIBlock();
			} else if(command.equals("#whoblocksme")){
				server.whoBlocksServer();
			} else {
				System.out.println("Illegal command. Use: #command <arg>");
			}
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println("Error reading from server console.");
		}
		
	}

	private void getPort() {
		System.out.println("Current port: "+server.getPort());
	}

	private void start() throws IOException {
		if(server.isListening()) {
			System.out.println("Server must be closed. Use #close and try again.");
		} else {
			server.listen();
		}
	}

	private void setPort(int port) {
		if(server.isListening()) {
			System.out.println("Server must be closed. Use #close and try again.");
		} else {
			server.setPort(port);
			System.out.println("Port set to: " + port);
		}
	}

	private void close() throws IOException {
		server.close();
	}

	private void stop() {
		server.stopListening();
	}

	private void quit() throws IOException {
		display("WARNING - Server is shutting down.");
		System.exit(0);
	}
	
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
	      System.out.println("ERROR - Could not listen for clients on specified port, shutting down!");
	      System.exit(1);
	    }
	    server.accept();
	  }
}
