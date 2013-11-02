package de.dakror.virtualhub.web;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Properties;

import javax.swing.JOptionPane;

import com.sun.net.httpserver.HttpServer;

import de.dakror.universion.UniVersion;
import de.dakror.virtualhub.VirtualHub;
import de.dakror.virtualhub.settings.CFG;

/**
 * @author Dakror
 */
public class Web
{
	public static final int VERSION = 2013102617;
	public static final int PHASE = 2;
	
	public static HttpServer server;
	
	public static File dir;
	
	public static Properties properties;
	
	public Web()
	{
		try
		{
			server = HttpServer.create(new InetSocketAddress(InetAddress.getLocalHost(), 8080), 0);
			server.createContext("/", new WebHandler());
			server.start();
			
			dir = new File(CFG.DIR, "Web");
			dir.mkdir();
			
			initProperties();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void initProperties() throws Exception
	{
		File prop = new File(Web.dir, "settings.properties");
		if (!prop.exists())
		{
			JOptionPane.showMessageDialog(null, "Bitte bearbeiten Sie die soeben erstelle Konfigurationsdatei:\r\n    " + prop.getPath().replace("\\", "/") + "\r\n\r\nStarten Sie bitte den VirtualHub-Webserver daraufhin erneut.", "VirtualHub-Webserver konfigurieren", JOptionPane.WARNING_MESSAGE);
			Properties p = new Properties();
			
			p.setProperty("theme", "virtualhub");
			p.setProperty("company", "Dakror Productions");
			
			p.store(new FileWriter(prop), "VirtualHub-Webserver Konfigurationsdatei");
			System.exit(0);
		}
		properties = new Properties();
		properties.load(new FileReader(prop));
	}
	
	public static void main(String[] args)
	{
		VirtualHub.init();
		
		UniVersion.offline = !CFG.INTERNET;
		UniVersion.init(Web.class, VERSION, PHASE);
		
		new Web();
	}
}
