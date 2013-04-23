package client;
import drawpad.OpenDrawPad;

import java.io.IOException;
import java.util.*;

import ocsf.client.ObservableClient;

/**
 * This class is used to start the DrawPad application without any client or
 * server.  It provides no facilities for quitting the application and is meant
 * to be used to demonstrate the use of DrawPad.
 *
 * @author Fran&ccedil;ois B&eacute;langer
 * @version August 2001
 * @modified Caleb Tebbe & Zach Kaplan (April 2013)
 */
public class ClientDrawPad extends Observable implements Observer 
{

	private ObservableClient client; 

	public ClientDrawPad(ObservableClient observableClient)	{
		client = observableClient;
	}

	public void update(Observable obs, Object obj)
	{
		if (!(obj instanceof String))
			return;

		String msg = (String)obj;

		//If the message to draw needs to be sent to other clients
		if (msg.startsWith("#send"))
		{	  
			try {
				client.sendToServer(obj);
			} catch (IOException e) {
				e.printStackTrace();
			}

			setChanged();
			notifyObservers(msg.substring(6));
		}

		//Else we need to display a message we have received on our drawpad
		else{
			setChanged();
			notifyObservers(msg.substring(msg.indexOf("#linedraw")));
		}
	}
} 
