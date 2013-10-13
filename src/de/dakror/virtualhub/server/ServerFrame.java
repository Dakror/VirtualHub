package de.dakror.virtualhub.server;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.json.JSONArray;
import org.json.JSONException;

import de.dakror.universion.UniVersion;
import de.dakror.virtualhub.util.Assistant;

/**
 * @author Dakror
 */
public class ServerFrame extends JFrame
{
	private static final long serialVersionUID = 1L;
	
	private JTextArea logArea;
	
	public ServerFrame()
	{
		super("VirtualHub Server (" + UniVersion.prettyVersion() + ")");
		setSize(800, 600);
		setMinimumSize(new Dimension(800, 600));
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
		
		logArea = new JTextArea();
		logArea.setWrapStyleWord(true);
		logArea.setEditable(false);
		logArea.setLineWrap(true);
		
		JScrollPane jsp = new JScrollPane(logArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		setContentPane(jsp);
	}
	
	public void initFiles()
	{
		try
		{
			// katalogs file
			File katalogs = new File(Server.dir, "katalogs.json");
			
			if (!katalogs.exists()) Assistant.setFileContent(katalogs, new JSONArray().toString());
			else Server.currentServer.katalogs = new JSONArray(Assistant.getFileContent(katalogs));
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}
	
	public void log(String line)
	{
		logArea.append("[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "]: " + line + "\r\n");
	}
	
	public void showSettingsDialog()
	{	
		
	}
}
