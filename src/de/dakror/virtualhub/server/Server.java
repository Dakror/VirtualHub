package de.dakror.virtualhub.server;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import de.dakror.virtualhub.net.NetHandler;
import de.dakror.virtualhub.settings.CFG;

/**
 * @author Dakror
 */
public class Server extends Thread
{
	public static final int PACKETSIZE = 65536;
	
	public static Server currentServer;
	
	public ServerFrame frame;
	
	ArrayList<Socket> clients = new ArrayList<Socket>();
	
	ServerSocket socket;
	
	public Server()
	{
		currentServer = this;
		
		frame = new ServerFrame();
		frame.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				shutdown();
			}
		});
		
		setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler()
		{
			@Override
			public void uncaughtException(Thread t, Throwable e)
			{
				frame.log("ERROR: " + e.toString());
			}
		});
		
		try
		{
			socket = new ServerSocket(CFG.SERVER_PORT, 0, InetAddress.getLocalHost());
			frame.log("Starte Server unter " + socket.getInetAddress().getHostAddress() + ":" + socket.getLocalPort());
		}
		catch (BindException e)
		{
			frame.log("Es läuft bereits ein Server auf diesem Port!");
			shutdown();
			return;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		start();
	}
	
	@Override
	public void run()
	{
		while (!socket.isClosed())
		{
			Socket s = null;
			try
			{
				s = socket.accept();
				handleConnection(s);
			}
			catch (IOException e)
			{}
		}
	}
	
	public void handleConnection(Socket s)
	{
		frame.log("Client verbunden: " + s.getInetAddress().getHostAddress() + ":" + s.getPort());
		clients.add(s);
		
		new NetHandler(s).start();
	}
	
	public void removeClient(Socket s, String msg)
	{
		clients.remove(s);
		frame.log("Client getrennt: " + s.getInetAddress().getHostAddress() + ":" + s.getPort() + ((!msg.equals("Verbindung getrennt")) ? " (" + msg + ")" : ""));
	}
	
	public void shutdown()
	{
		try
		{
			if (socket != null) socket.close();
			frame.log("Server geschlossen\r\n");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
