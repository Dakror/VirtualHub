package de.dakror.virtualhub.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;

import de.dakror.virtualhub.settings.CFG;
import de.dakror.virtualhub.util.Assistant;

/**
 * @author Dakror
 */
public class FileMover extends Thread
{
	File targetParent;
	File[] files;
	
	boolean copy;
	
	int value;
	
	ProgressMonitor monitor;
	
	ClientFrame frame;
	
	boolean canceled;
	
	public FileMover(final ClientFrame frame, final boolean copy, File targetParent, File... files)
	{
		this.targetParent = targetParent;
		this.files = files;
		this.copy = copy;
		this.frame = frame;
		
		canceled = false;
		
		int count = 0;
		for (File f : files)
			count += Assistant.getFileCount(f);
		
		value = 0;
		
		monitor = new ProgressMonitor(frame, (copy ? "Kopiere" : "Verschiebe") + " Dateien...", "", 0, count);
		monitor.setMillisToDecideToPopup(0);
		monitor.setMillisToPopup(0);
		
		start();
	}
	
	@Override
	public void run()
	{
		for (int i = 0; i < files.length; i++)
		{
			File target = new File(targetParent, files[i].getName());
			monitor.setNote("Datei: " + files[i].getName());
			
			if (target.getPath().replace("\\", "/").startsWith(files[i].getPath().replace("\\", "/")))
			{
				JOptionPane.showMessageDialog(Client.currentClient.frame, "Hierhin kann nicht verschoben werden.", (files[i].isDirectory() ? "Verzeichnis" : "Datei") + (copy ? " kopieren" : " verschieben"), JOptionPane.ERROR_MESSAGE);
				canceled = true;
				break;
			}
			
			if (files[i].equals(target.getParentFile()))
			{
				if (JOptionPane.showConfirmDialog(Client.currentClient.frame, (files[i].isDirectory() ? "Das Quell- und Zielverzeichnis" : "Die Quell- und Zieldatei") + " sind identisch.", (files[i].isDirectory() ? "Verzeichnis" : "Datei") + (copy ? " kopieren" : " verschieben"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.CANCEL_OPTION) break;
				continue;
			}
			
			if (target.exists())
			{
				int response = JOptionPane.showConfirmDialog(Client.currentClient.frame, (files[i].isDirectory() ? "Das Zielverzeichnis" : "Die Zieldatei") + " existiert bereits. \u00dcberschreiben?\r\nBei \"Nein\" wird " + (files[i].isDirectory() ? "das Verzeichnis" : "die Datei") + " \u00fcbersprungen.", (files[i].isDirectory() ? "Verzeichnis" : "Datei") + (copy ? " kopieren" : " verschieben"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
				if (response == JOptionPane.NO_OPTION) continue;
				if (response == JOptionPane.CANCEL_OPTION)
				{
					canceled = true;
					break;
				}
			}
			
			if (!copy)
			{
				if (files[i].isDirectory()) moveOrCopyFolder(files[i], target, copy);
				else files[i].renameTo(target);
			}
			else
			{
				if (files[i].isDirectory()) moveOrCopyFolder(files[i], target, copy);
				else
				{
					try
					{
						target.createNewFile();
						Assistant.copyInputStream(new FileInputStream(files[i]), new FileOutputStream(target));
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		}
		
		frame.catalog.handleDrop();
		frame.directoryLoader.fireUpdate();
	}
	
	public void updateProgress(String note)
	{
		value++;
		monitor.setNote(note);
		monitor.setProgress(value);
	}
	
	public void moveOrCopyFolder(File folder, File targetParent, boolean copy)
	{
		for (File f : folder.listFiles())
		{
			if (f.isHidden()) continue;
			
			if (f.isDirectory())
			{
				File newFolder = new File(targetParent, f.getName());
				newFolder.mkdir();
				moveOrCopyFolder(f, newFolder, copy);
				if (!copy) f.delete();
			}
			else if (f.isFile())
			{
				File newFile = new File(targetParent, f.getName());
				try
				{
					newFile.getParentFile().mkdirs();
					if (copy)
					{
						newFile.createNewFile();
						Assistant.copyInputStream(new FileInputStream(f), new FileOutputStream(newFile));
					}
					else
					{
						newFile.delete();
						f.renameTo(newFile);
					}
					
					updateProgress(f.getPath());
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		
		if (!copy) CFG.p(folder, folder.delete());
	}
}
