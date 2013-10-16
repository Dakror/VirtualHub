package de.dakror.virtualhub.client;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import de.dakror.virtualhub.util.Assistant;
import de.dakror.virtualhub.util.FileComparator;

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
				frame.fileViewWrap.getVerticalScrollBar().setValue(0);
				frame.fileView.removeAll();
				if (frame.catalog.getSelectionPath() != null)
				{
					DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) frame.catalog.getSelectionPath().getLastPathComponent();
					
					File folder = new File(Assistant.getNodePath(dmtn));
					
					List<File> files = Arrays.asList(folder.listFiles());
					Collections.sort(files, new FileComparator());
					
					for (File f : files)
					{
						frame.fileView.add(new FileButton(f));
					}
				}
				
				updateFired = false;
				frame.validate();
				frame.repaint();
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
