package de.dakror.virtualhub.client;

import java.io.File;

import javax.swing.JFrame;

import de.dakror.virtualhub.client.dialog.ProgressDialog;

/**
 * @author Dakror
 */
public class FileMover extends Thread
{
	File targetParent;
	File[] files;
	
	boolean copy;
	
	ProgressDialog dialog;
	
	public FileMover(final JFrame frame, final boolean copy, File targetParent, File... files)
	{
		this.targetParent = targetParent;
		this.files = files;
		this.copy = copy;
		
		dialog = new ProgressDialog(frame, (copy ? "Kopiere" : "Verschiebe") + " Dateien...", "", files.length);
		dialog.setTitle((copy ? "Kopiere" : "Verschiebe") + "...");
		
		start();
	}
	
	@Override
	public void run()
	{
		for (int i = 0; i < files.length; i++)
		{
			File target = new File(targetParent, files[i].getName());
			dialog.setNote("Datei: " + files[i].getName());
			files[i].renameTo(target);
			dialog.setProgress(i + 1);
		}
		
		dialog.dispose();
	}
}
