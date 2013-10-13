package de.dakror.virtualhub.client;

import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JMenuBar;

import de.dakror.universion.UniVersion;

/**
 * @author Dakror
 */
public class ClientFrame extends JFrame
{
	private static final long serialVersionUID = 1L;
	
	public ClientFrame()
	{
		super("VirtualHub Client (" + UniVersion.prettyVersion() + ")");
		setSize(1080, 675);
		setMinimumSize(new Dimension(1080, 675));
		try
		{
			setIconImage(ImageIO.read(getClass().getResource("/img/icon.png")));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		init();
		
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
	}
	
	public void init()
	{
		initFiles();
		initMenu();
	}
	
	public void initFiles()
	{
		try
		{
			if (!new File(Client.dir, "settings.properties").exists())
			{
				// properties file
				Properties properties = new Properties();
				properties.put("server", InetAddress.getLocalHost().getHostAddress());
				Client.currentClient.properties = properties;
				properties.store(new FileOutputStream(new File(Client.dir, "settings.properties")), "VirtualHub Client Einstellungen\r\n\r\n  server = Die IP des Servers, auf dem die VirtualHub Server Software läuft\r\n");
			}
			else
			{
				Client.currentClient.properties = new Properties();
				Client.currentClient.properties.load(new FileInputStream(new File(Client.dir, "settings.properties")));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void initMenu()
	{
		JMenuBar menu = new JMenuBar();
		
		setJMenuBar(menu);
	}
}
