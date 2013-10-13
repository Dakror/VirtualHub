package de.dakror.virtualhub.client;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Properties;

import javax.swing.JOptionPane;

import de.dakror.virtualhub.net.NetHandler;
import de.dakror.virtualhub.net.PacketHandler;
import de.dakror.virtualhub.net.packet.Packet;
import de.dakror.virtualhub.net.packet.Packet0Katalogs;
import de.dakror.virtualhub.settings.CFG;

/**
 * @author Dakror
 */
public class Client extends Thread implements PacketHandler
{
	public static Client currentClient;
	
	Socket socket;
	NetHandler netHandler;
	ClientFrame frame;
	public Properties properties;
	
	public static File dir;
	
	public Client()
	{
		currentClient = this;
		
		dir = new File(CFG.DIR, "Client");
		dir.mkdir();
		
		frame = new ClientFrame();
		frame.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				if (socket == null) return;
				try
				{
					socket.close();
				}
				catch (IOException e1)
				{
					e1.printStackTrace();
				}
			}
		});
		
		try
		{
			socket = new Socket(properties.getProperty("server"), CFG.SERVER_PORT);
		}
		catch (ConnectException e)
		{
			JOptionPane.showMessageDialog(frame, "Kann Server unter " + properties.getProperty("server") + " nicht erreichen!", "Fehler!", JOptionPane.ERROR_MESSAGE);
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
		netHandler = new NetHandler(this, socket);
		netHandler.run();
	}
	
	@Override
	public void parsePacket(byte[] data)
	{
		switch (Packet.lookupPacket(data[0]))
		{
			case INVALID:
				CFG.p("Received invalid packet");
				break;
			case KATALOGS:
				Packet0Katalogs p = new Packet0Katalogs(data);
				CFG.p(p.getKatalogs());
				break;
		}
	}
	
	@Override
	public void sendPacket(Packet p) throws IOException
	{}
}
