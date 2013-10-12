package de.dakror.virtualhub.client;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;

import javax.swing.JOptionPane;

import de.dakror.virtualhub.settings.CFG;

/**
 * @author Dakror
 */
public class Client
{
	Socket socket;
	
	ClientFrame frame;
	
	public Client(InetAddress serverIP)
	{
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
			socket = new Socket(serverIP, CFG.SERVER_PORT);
		}
		catch (ConnectException e)
		{
			JOptionPane.showMessageDialog(frame, "Kann Server unter " + serverIP.getHostAddress() + " nicht erreichen!", "Fehler!", JOptionPane.ERROR_MESSAGE);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
