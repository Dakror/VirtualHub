package de.dakror.virtualhub.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.swing.ProgressMonitor;

import de.dakror.virtualhub.util.Assistant;

/**
 * @author Dakror
 */
public class FileMover extends Thread
{
	File targetParent;
	File[] files;
	
	boolean copy;
	
	ProgressMonitor monitor;
	
	ClientFrame frame;
	
	public FileMover(final ClientFrame frame, final boolean copy, File targetParent, File... files)
	{
		this.targetParent = targetParent;
		this.files = files;
		this.copy = copy;
		this.frame = frame;
		
		monitor = new ProgressMonitor(frame, (copy ? "Kopiere" : "Verschiebe") + " Dateien...", "", 0, 50);
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
			
			if (!copy) files[i].renameTo(target);
			else
			{
				if (files[i].isDirectory()) moveOrCopyFolder(files[i], target, copy);
				else
				{
					try
					{
						target.mkdir();
						Assistant.copyInputStream(new FileInputStream(files[i]), new FileOutputStream(target));
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
			monitor.setProgress(i + 1);
		}
		
		monitor.close();
		frame.directoryLoader.fireUpdate();
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
			}
			else if (f.isFile())
			{
				File newFile = new File(targetParent, f.getName());
				try
				{
					newFile.getParentFile().mkdirs();
					newFile.createNewFile();
					Assistant.copyInputStream(new FileInputStream(f), new FileOutputStream(newFile));
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
}
