import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import ocsf.server.ObservableOriginatorServer;
import ocsf.server.ObservableServer;

import common.ChatIF;

/**
 * CS 314
 * This class starts a server with echoserver as a shell
 * @author calebtebbe
 * @author zachkaplan
 *
 */
public class ServerConsole implements ChatIF {

	private static final int DEFAULT_PORT = 5555;
	EchoServer server;
	
	public ServerConsole(int port) {
		ObservableOriginatorServer obsServer = new ObservableOriginatorServer(port);
		this.server = new EchoServer(this, obsServer);
		try {
			obsServer.listen();
		} catch(IOException ex) {
			System.out.println("Error - Couldnt listen for clients");
		}
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
			} else if(command.equals("#block")) {
				server.addBlock(null, arg);
			} else if(command.equals("#unblock")){
				server.removeFromBlockList(null, arg);
			} else if(command.equals("#whoiblock")){
				server.whoIBlock();
			} else if(command.equals("#whoblocksme")){
				server.whoBlocksServer();
			} else {
				server.serverCommand(command, arg);
			}
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println("Error reading from server console.");
		}
		
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
		
	    //EchoServer sv = new EchoServer(port);
	    ServerConsole server = new ServerConsole(port);
	    server.accept();
	  }
}
