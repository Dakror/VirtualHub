package de.dakror.virtualhub.client;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

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
					final DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) frame.catalog.getSelectionPath().getLastPathComponent();
					
					File folder = new File(Assistant.getNodePath(dmtn));
					
					List<File> files = Arrays.asList(folder.listFiles());
					Collections.sort(files, new FileComparator());
					
					for (File f : files)
					{
						final FileButton fb = new FileButton(f);
						fb.addActionListener(new ActionListener()
						{
							@Override
							public void actionPerformed(ActionEvent e)
							{
								if (fb.file.isDirectory())
								{
									frame.catalog.expandPath(new TreePath(dmtn.getPath()));
									for (int i = 0; i < dmtn.getChildCount(); i++)
									{
										if (((DefaultMutableTreeNode) dmtn.getChildAt(i)).getUserObject().equals(fb.file.getName()))
										{
											frame.catalog.setSelectionPath(new TreePath(((DefaultMutableTreeNode) dmtn.getChildAt(i)).getPath()));
										}
									}
								}
								else
								{
									if (Desktop.isDesktopSupported())
									{
										try
										{
											Desktop.getDesktop().open(fb.file);
										}
										catch (IOException e1)
										{
											e1.printStackTrace();
										}
									}
								}
							}
						});
						frame.fileView.add(fb);
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
