import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import common.ChatIF;

/**
 * This class starts a server with echoserver as a shell
 * @author calebtebbe
 * @author zachkaplan
 *
 */
public class ServerConsole implements ChatIF {

	private static final int DEFAULT_PORT = 5555;
	EchoServer server;
	
	public ServerConsole(EchoServer echoServer) {
		this.server = echoServer;
	}

	@Override
	public void display(String message) {
		String msg = "SERVER MSG> " + message;
		server.sendToAllClients(msg);
	}

	public void accept() {
		try {
			BufferedReader fromConsole = new BufferedReader(
					new InputStreamReader(System.in));
			String message;

			while (true) {
				message = fromConsole.readLine();
				if ((message.trim()).startsWith("#")) {
					handleServerCommand(message);
				} else {
					System.out.println("Message received: " + message + " from server console");
					display(message);
				}
			}
		} catch (Exception ex) {
			System.out.println("Unexpected error while reading from console!");
		}
	}

	private void handleServerCommand(String command) {
		// pull argument from command if there is any
		String arg = null;
		int argIndex = command.indexOf(' ');
		if(argIndex != -1) {
			arg = (command.substring(argIndex)).trim();
			command = (command.substring(command.indexOf('#'), argIndex)).trim();
		}
		try {
			// parse command
			if(command.equals("#quit")) {
				quit();
			} else if(command.equals("#stop")) {
				stop();
			}else if(command.equals("#close")) {
				close();
			}else if(command.equals("#setport")) {
				setPort(Integer.parseInt(arg));
			}else if(command.equals("#start")) {
				start();
			}else if(command.equals("#getport")) {
				getPort();
			} else {
				System.out.println("Illegal command. Use: #command <arg>");
			}
		} catch(Exception e) {
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
		}
	}

	private void close() throws IOException {
		server.close();
	}

	private void stop() {
		server.stopListening();
	}

	private void quit() throws IOException {
		display("Server exiting.");
		server.close();
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
	      System.out.println("ERROR - Could not listen for clients!");
	    }
	    server.accept();
	  }
}
