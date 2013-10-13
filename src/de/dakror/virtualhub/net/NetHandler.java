package de.dakror.virtualhub.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

import de.dakror.virtualhub.net.packet.Packet;
import de.dakror.virtualhub.net.packet.Packet.PacketTypes;
import de.dakror.virtualhub.net.packet.Packet0Katalogs;
import de.dakror.virtualhub.server.Server;
import de.dakror.virtualhub.util.Assistant;

/**
 * Both Client- and Serversided
 * 
 * @author Dakror
 */
public class NetHandler extends Thread implements PacketHandler
{
	Socket socket;
	PacketHandler handler;
	
	public DataInputStream dis;
	public DataOutputStream dos;
	
	public NetHandler(PacketHandler handler, Socket socket)
	{
		this.socket = socket;
		this.handler = handler;
	}
	
	@Override
	public void run()
	{
		try
		{
			dis = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());
			
			if (handler == null) sendPacket(new Packet0Katalogs(Server.currentServer.catalogs));
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
				
				if (handler != null) handler.parsePacket(data);
				else parsePacket(data);
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
	
	/**
	 * Only called Server sided
	 */
	@Override
	public void parsePacket(byte[] data)
	{
		PacketTypes t = Packet.lookupPacket(data[0]);
		Server.currentServer.frame.log("< " + Assistant.getSocketAddress(socket) + " : " + t.name());
		switch (t)
		{
			case INVALID:
				Server.currentServer.frame.log("Empfing ung\u00fcltiges Paket");
				break;
			case KATALOGS:
				Packet0Katalogs p = new Packet0Katalogs(data);
				Server.currentServer.catalogs = p.getKatalogs();
				Server.currentServer.frame.log("Kataloge ge\u00e4ndert von: " + Assistant.getSocketAddress(socket));
				break;
		}
	}
	
	@Override
	public void sendPacket(Packet p) throws IOException
	{
		if (isServerSided()) Server.currentServer.frame.log("> " + Assistant.getSocketAddress(socket) + " : " + p.getType().name());
		dos.write(p.getData());
	}
	
	public boolean isServerSided()
	{
		return Server.currentServer != null;
	}
	
}
