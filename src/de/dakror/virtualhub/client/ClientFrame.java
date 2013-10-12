package de.dakror.virtualhub.client;

import java.awt.Dimension;
import java.io.IOException;

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
		initMenu();
	}
	
	public void initMenu()
	{
		JMenuBar menu = new JMenuBar();
		
		setJMenuBar(menu);
	}
}
