package de.dakror.virtualhub.client;

import java.awt.Dimension;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.tree.DefaultMutableTreeNode;

import de.dakror.virtualhub.util.Assistant;

/**
 * @author Dakror
 */
public class DirectoryLoader extends Thread
{
	ClientFrame frame;
	boolean updateFired;
	
	public DirectoryLoader()
	{
		frame = Client.currentClient.frame;
		setName("DirectoryLoader");
		
		start();
	}
	
	@Override
	public void run()
	{
		while (true)
		{
			if (updateFired)
			{
				if (frame.catalog.getSelectionPath() != null)
				{
					frame.fileViewWrap.getVerticalScrollBar().setValue(0);
					frame.fileView.removeAll();
					DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) frame.catalog.getSelectionPath().getLastPathComponent();
					
					File folder = new File(Assistant.getNodePath(dmtn));
					
					for (File f : folder.listFiles())
					{
						JLabel file = new JLabel(f.getName(), JLabel.CENTER);
						file.setBorder(BorderFactory.createLineBorder(frame.borderColor));
						file.setPreferredSize(new Dimension(250, 250));
						frame.fileView.add(file);
					}
					
					frame.validate();
					
					updateFired = false;
				}
			}
			try
			{
				Thread.sleep(100);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}
}
