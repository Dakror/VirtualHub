package de.dakror.virtualhub.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

import de.dakror.virtualhub.server.Server;

/**
 * @author Dakror
 */
public class ClientHandler extends Thread
{
	Socket client;
	
	public ClientHandler(Socket client)
	{
		this.client = client;
		
		start();
	}
	
	@Override
	public void run()
	{
		DataInputStream dis = null;
		DataOutputStream dos = null;
		try
		{
			dis = new DataInputStream(client.getInputStream());
			dos = new DataOutputStream(client.getOutputStream());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		while (!client.isClosed())
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
					Server.currentServer.removeClient(client, "Verbindung verloren");
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
					Server.currentServer.removeClient(client, "Verbindung getrennt");
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
}
