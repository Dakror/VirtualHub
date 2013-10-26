package de.dakror.virtualhub.server;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
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
	
	JCheckBoxMenuItem packetLogEnabled;
	JCheckBoxMenuItem logEnabled;
	
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
		
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("Aktionen");
		menu.add(new JMenuItem(new AbstractAction("Protokoll leeren", new ImageIcon(getClass().getResource("/img/trash.png")))
		{
			private static final long serialVersionUID = 1L;
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				logArea.setText("");
				log("Protokoll geleert.");
			}
		}));
		menu.add(logEnabled = new JCheckBoxMenuItem("Protokoll aktiviert", new ImageIcon(getClass().getResource("/img/log.png")), true));
		menu.add(packetLogEnabled = new JCheckBoxMenuItem("Paketverkehr protokollieren", new ImageIcon(getClass().getResource("/img/traffic.png")), false));
		menuBar.add(menu);
		setJMenuBar(menuBar);
	}
	
	public void initFiles()
	{
		try
		{
			// katalogs file
			File catalogs = new File(Server.dir, "catalogs.json");
			
			if (!catalogs.exists())
			{
				Assistant.setFileContent(catalogs, new JSONArray().toString());
				Server.currentServer.catalogs = new JSONArray();
			}
			else Server.currentServer.catalogs = new JSONArray(Assistant.getFileContent(catalogs));
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}
	
	public void save()
	{
		File catalogs = new File(Server.dir, "catalogs.json");
		Assistant.setFileContent(catalogs, Server.currentServer.catalogs.toString());
	}
	
	public void plog(String line)
	{
		if (packetLogEnabled.isSelected()) log(line);
	}
	
	public void log(String line)
	{
		if (!logEnabled.isSelected()) return;
		
		logArea.append("[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "]: " + line + "\r\n");
	}
}
