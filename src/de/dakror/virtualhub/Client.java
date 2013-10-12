package de.dakror.virtualhub;

import javax.swing.JFrame;

import de.dakror.universion.UniVersion;

/**
 * @author Dakror
 */
public class Client extends JFrame
{
	private static final long serialVersionUID = 1L;
	
	public Client()
	{
		super("VirtualHub " + UniVersion.prettyVersion() + " Client");
		setSize(720, 480);
		
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
	}
	
	public void init()
	{}
}
