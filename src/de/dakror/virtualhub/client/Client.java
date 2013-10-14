package de.dakror.virtualhub.client;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Properties;

import javax.swing.JOptionPane;

import de.dakror.virtualhub.client.dialog.ChooseCatalogDialog;
import de.dakror.virtualhub.data.Catalog;
import de.dakror.virtualhub.net.NetHandler;
import de.dakror.virtualhub.net.PacketHandler;
import de.dakror.virtualhub.net.packet.Packet;
import de.dakror.virtualhub.net.packet.Packet0Katalogs;
import de.dakror.virtualhub.settings.CFG;
import de.dakror.virtualhub.util.Assistant;

/**
 * @author Dakror
 */
public class Client extends Thread implements PacketHandler
{
	public static Client currentClient;
	
	Socket socket;
	NetHandler netHandler;
	public ClientFrame frame;
	Catalog catalog;
	
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
				disconnect();
			}
		});
		
		Assistant.setJFrameComponentsEnabled(frame, false);
		frame.setVisible(true);
		try
		{
			socket = new Socket(InetAddress.getByName(properties.getProperty("server")), CFG.SERVER_PORT);
		}
		catch (ConnectException e)
		{
			JOptionPane.showMessageDialog(frame, "Kann Server unter " + properties.getProperty("server") + " nicht erreichen!", "Fehler!", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
			return;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		Assistant.setJFrameComponentsEnabled(frame, true);
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
				ChooseCatalogDialog.show(frame, p.getKatalogs());
				break;
		}
	}
	
	public void disconnect()
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
	
	@Override
	public void sendPacket(Packet p) throws IOException
	{
		netHandler.sendPacket(p);
	}
	
	public Catalog getCatalog()
	{
		return catalog;
	}
	
	public void setCatalog(Catalog catalog)
	{
		this.catalog = catalog;
	}
}
