package de.dakror.virtualhub.client;

import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import de.dakror.virtualhub.util.Assistant;
import de.dakror.virtualhub.util.FileComparator;

/**
 * @author Dakror
 */
public class DirectoryLoader extends Thread
{
	static DirectoryLoader loader;
	
	private ClientFrame frame;
	
	DefaultMutableTreeNode selectedNode;
	
	boolean synced;
	
	public DirectoryLoader()
	{
		frame = Client.currentClient.frame;
		setName("DirectoryLoader");
		
		synced = true;
		
		start();
	}
	
	@Override
	public void run()
	{
		while (true)
		{
			try
			{
				Thread.sleep(1);
			}
			catch (InterruptedException e2)
			{
				e2.printStackTrace();
			}
			
			if (frame.catalog.getSelectionPath() == null)
			{
				if (selectedNode == null)
				{
					continue;
				}
				
				frame.fileView.removeAll();
				frame.validate();
				frame.repaint();
				selectedNode = null;
				continue;
			}
			
			final DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) frame.catalog.getSelectionPath().getLastPathComponent();
			if (selectedNode == null || !dmtn.equals(selectedNode))
			{
				selectedNode = dmtn;
				synced = false;
			}
			else continue;
			
			frame.fileViewWrap.getVerticalScrollBar().setValue(0);
			frame.fileView.removeAll();
			
			File folder = new File(Assistant.getNodePath(dmtn));
			
			List<File> files = Arrays.asList(folder.listFiles());
			Collections.sort(files, new FileComparator());
			
			for (File f : files)
			{
				if (f.isHidden()) continue;
				
				if (!((DefaultMutableTreeNode) frame.catalog.getSelectionPath().getLastPathComponent()).equals(dmtn)) break;
				
				
				final FileButton fb = new FileButton(f);
				fb.addMouseListener(new MouseAdapter()
				{
					@Override
					public void mousePressed(MouseEvent e)
					{
						if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2)
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
										Desktop.getDesktop().browse(fb.file.toURI());
									}
									catch (IOException e1)
									{
										JOptionPane.showMessageDialog(frame, "Die Datei konnte nicht ge\u00f6ffnet werden!\nM\u00f6glicherweise fehlt Ihnen eine mit diesem Dateityp assozierte Software", "Datei konnte nicht ge\u00f6ffnet werden!", JOptionPane.ERROR_MESSAGE);
									}
								}
							}
						}
					}
				});
				frame.fileView.add(fb);
				
				frame.validate();
				frame.repaint();
			}
			
			frame.validate();
			frame.repaint();
			
			synced = true;
		}
	}
	
	public void fireUpdate()
	{
		synced = false;
		selectedNode = null;
	}
}
