package de.dakror.virtualhub.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

import de.dakror.virtualhub.server.Server;

/**
 * Both Client- and Serversided
 * 
 * @author Dakror
 */
public class NetHandler extends Thread
{
	Socket socket;
	
	public NetHandler(Socket socket)
	{
		this.socket = socket;
	}
	
	@Override
	public void run()
	{
		DataInputStream dis = null;
		DataOutputStream dos = null;
		try
		{
			dis = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		while (!socket.isClosed())
		{
			try
			{
				int length = dis.readInt();
				byte[] data = new byte[length];
				dis.readFully(data, 0, length);
				
				// now do sth with this data ^^
			}
			catch (SocketException e)
			{
				try
				{
					dis.close();
					dos.close();
					if (isServerSided()) Server.currentServer.removeClient(socket, "Verbindung verloren");
					return;
				}
				catch (IOException e1)
				{
					e1.printStackTrace();
				}
			}
			catch (EOFException e)
			{
				try
				{
					dis.close();
					dos.close();
					if (isServerSided()) Server.currentServer.removeClient(socket, "Verbindung getrennt");
					return;
				}
				catch (IOException e1)
				{
					e1.printStackTrace();
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public boolean isServerSided()
	{
		return Server.currentServer != null;
	}
}
