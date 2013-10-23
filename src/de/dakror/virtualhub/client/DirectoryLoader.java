package de.dakror.virtualhub.client;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultTreeModel;
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
	
	EticetableTreeNode selectedNode;
	
	boolean synced;
	
	public DirectoryLoader()
	{
		frame = Client.currentClient.frame;
		setName("DirectoryLoader");
		
		setPriority(MAX_PRIORITY);
		
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
				
				if (frame.catalog.getSelectionPath() == null)
				{
					if (selectedNode == null)
					{
						continue;
					}
					frame.fileView.dropTarget.setActive(false);
					frame.fileView.removeAll();
					frame.fileView.validate();
					frame.repaint();
					selectedNode = null;
					continue;
				}
				
				final EticetableTreeNode dmtn = (EticetableTreeNode) frame.catalog.getSelectionPath().getLastPathComponent();
				if (selectedNode == null || !dmtn.equals(selectedNode))
				{
					selectedNode = dmtn;
					synced = false;
				}
				else continue;
				
				frame.fileView.dropTarget.setActive(true);
				frame.fileViewWrap.getVerticalScrollBar().setValue(0);
				frame.fileView.removeAll();
				
				File folder = new File(Assistant.getNodePath(dmtn));
				if (!folder.exists()) continue;
				
				List<File> files = Arrays.asList(folder.listFiles());
				Collections.sort(files, new FileComparator());
				
				for (File f : files)
				{
					if (f.isHidden()) continue;
					
					if (frame.catalog.getSelectionPath() == null || !((EticetableTreeNode) frame.catalog.getSelectionPath().getLastPathComponent()).equals(dmtn)) break;
					
					final FileButton fb = new FileButton(f);
					
					final JPopupMenu popup = new JPopupMenu();
					popup.add(new JMenuItem(new AbstractAction("L\u00f6schen")
					{
						private static final long serialVersionUID = 1L;
						
						@Override
						public void actionPerformed(ActionEvent e)
						{
							if (frame.getSelectedFiles().length < 2)
							{
								if (JOptionPane.showConfirmDialog(frame, "Sind Sie sicher, dass Sie diese" + (fb.file.isDirectory() ? "s Verzeichnis und alle enthaltenen Dateien\n" : " Datei") + " unwiderruflich l\u00f6schen wollen?\nSie sollten von wichtigen Daten Backups machen, bevor Sie sie l\u00f6schen.", (fb.file.isDirectory() ? "Verzeichnis" : "Datei") + " l\u00f6schen", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
								{
									boolean success = true;
									boolean directory = fb.file.isDirectory();
									
									if (directory)
									{
										success = Assistant.deleteDirectory(fb.file);
										
										EticetableTreeNode dmtn = (EticetableTreeNode) frame.catalog.getSelectionPath().getLastPathComponent();
										frame.loadSubTree(dmtn);
										
									}
									else success = fb.file.delete();
									
									if (!success) JOptionPane.showMessageDialog(frame, (directory ? "Das Verzeichnis" : "Die Datei") + " konnte nicht gel\u00f6scht werden, da sie in einem anderen Programm ge\u00f6ffnet ist.", "L\u00f6schen nicht m\u00f6glich", JOptionPane.ERROR_MESSAGE);
									else fireUpdate();
								}
							}
							else if (JOptionPane.showConfirmDialog(frame, "Sind Sie sicher, dass Sie die Daten unwiderruflich l\u00f6schen wollen?\nSie sollten von wichtigen Daten Backups machen, bevor Sie sie l\u00f6schen.", "Daten l\u00f6schen", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
							{
								File[] files = frame.getSelectedFiles();
								for (File file : files)
								{
									boolean success = true;
									boolean directory = file.isDirectory();
									
									if (directory)
									{
										success = Assistant.deleteDirectory(file);
										
										if (success)
										{
											EticetableTreeNode dmtn = (EticetableTreeNode) frame.catalog.getSelectionPath().getLastPathComponent();
											for (int i = 0; i < dmtn.getChildCount(); i++)
												if (((EticetableTreeNode) dmtn.getChildAt(i)).getUserObject().toString().equals(fb.file.getName())) dmtn.remove(i);
											
											((DefaultTreeModel) frame.catalog.getModel()).reload(dmtn);
										}
									}
									else success = file.delete();
									
									if (!success) JOptionPane.showMessageDialog(frame, (directory ? "Das Verzeichnis" : "Die Datei") + " konnte nicht gel\u00f6scht werden, da sie in einem anderen Programm ge\u00f6ffnet ist.", "L\u00f6schen nicht m\u00f6glich", JOptionPane.ERROR_MESSAGE);
								}
								fireUpdate();
							}
						}
					}));
					
					fb.addActionListener(new ActionListener()
					{
						@Override
						public void actionPerformed(ActionEvent e)
						{
							if (e.getModifiers() == 16)
							{
								for (Component c : frame.fileView.getComponents())
								{
									if (c instanceof FileButton)
									{
										if (!c.equals(fb)) ((FileButton) c).setSelected(false);
									}
								}
								fb.setSelected(fb.isSelected());
							}
							else if (e.getModifiers() == 18) fb.setSelected(fb.isSelected());
							frame.setFileInfo(fb.file);
						}
					});
					
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
										if (((EticetableTreeNode) dmtn.getChildAt(i)).getUserObject().equals(fb.file.getName()))
										{
											frame.catalog.setSelectionPath(new TreePath(((EticetableTreeNode) dmtn.getChildAt(i)).getPath()));
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
											JOptionPane.showMessageDialog(frame, "Die Datei konnte nicht ge\u00f6ffnet werden!\nM\u00f6glicherweise fehlt Ihnen eine mit diesem Dateityp assozierte Software.", "Datei konnte nicht ge\u00f6ffnet werden!", JOptionPane.ERROR_MESSAGE);
										}
									}
								}
							}
							else if (e.getButton() == MouseEvent.BUTTON3) popup.show(e.getComponent(), e.getX(), e.getY());
						}
					});
					frame.fileView.add(fb);
					
					frame.fileView.revalidate();
					frame.repaint();
				}
				
				frame.fileView.revalidate();
				frame.repaint();
				
				System.gc();
				
				synced = true;
			}
			catch (InterruptedException e)
			{}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public void fireUpdate()
	{
		frame.fileView.removeAll();
		frame.fileView.validate();
		frame.fileView.repaint();
		
		synced = false;
		selectedNode = null;
	}
}
