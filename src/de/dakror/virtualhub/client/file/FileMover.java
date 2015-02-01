package de.dakror.virtualhub.client.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;

import de.dakror.virtualhub.client.Client;
import de.dakror.virtualhub.client.ClientFrame;
import de.dakror.virtualhub.util.Assistant;

/**
 * @author Dakror
 */
public class FileMover extends Thread {
	File targetParent;
	File[] files;
	
	boolean copy;
	boolean canceled;
	boolean fromTree;
	
	int value;
	
	ProgressMonitor monitor;
	
	ClientFrame frame;
	
	
	public FileMover(final ClientFrame frame, boolean fromTree, final boolean copy, File targetParent, File... files) {
		this.targetParent = targetParent;
		this.files = files;
		this.fromTree = fromTree;
		this.copy = copy;
		this.frame = frame;
		
		canceled = false;
		
		
		JDialog dialog = new JDialog(frame, "");
		dialog.setContentPane(new JLabel("<html><body>Ermittle Gesamtgröße<br>Bitte warten...</body></html>", JLabel.CENTER));
		dialog.setSize(160, 80);
		dialog.setLocationRelativeTo(frame);
		dialog.setAlwaysOnTop(true);
		dialog.setVisible(true);
		
		int count = 0;
		for (File f : files) {
			if (f.isDirectory()) count += Assistant.getFileCount(f);
			else count++;
		}
		
		dialog.dispose();
		
		value = 0;
		
		monitor = new ProgressMonitor(frame, (copy ? "Kopiere" : "Verschiebe") + " Dateien...", "", 0, count);
		monitor.setMillisToDecideToPopup(0);
		monitor.setMillisToPopup(0);
		
		start();
	}
	
	@Override
	public void run() {
		for (int i = 0; i < files.length; i++) {
			File target = new File(targetParent, files[i].getName());
			monitor.setNote("Datei: " + files[i].getName());
			
			if (Assistant.isDeepChild(files[i], target)) {
				JOptionPane.showMessageDialog(Client.currentClient.frame, "Hierhin kann nicht verschoben werden.", (files[i].isDirectory() ? "Verzeichnis" : "Datei")
						+ (copy ? " kopieren" : " verschieben"), JOptionPane.ERROR_MESSAGE);
				canceled = true;
				break;
			}
			
			if (files[i].equals(target.getParentFile())) {
				if (JOptionPane.showConfirmDialog(Client.currentClient.frame,
																					(files[i].isDirectory() ? "Das Quell- und Zielverzeichnis" : "Die Quell- und Zieldatei") + " sind identisch.", (files[i].isDirectory()
																							? "Verzeichnis" : "Datei") + (copy ? " kopieren" : " verschieben"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.CANCEL_OPTION)
					break;
				continue;
			}
			
			if (target.exists()) {
				int response = JOptionPane.showConfirmDialog(Client.currentClient.frame, (files[i].isDirectory() ? "Das Zielverzeichnis" : "Die Zieldatei")
						+ " existiert bereits. Überschreiben?\r\nBei \"Nein\" wird " + (files[i].isDirectory() ? "das Verzeichnis" : "die Datei") + " übersprungen.", (files[i].isDirectory()
						? "Verzeichnis" : "Datei") + (copy ? " kopieren" : " verschieben"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
				if (response == JOptionPane.NO_OPTION) continue;
				if (response == JOptionPane.CANCEL_OPTION) {
					canceled = true;
					break;
				}
				if (!target.isDirectory()) target.delete();
			}
			
			if (!copy) {
				if (files[i].isDirectory()) moveOrCopyFolder(files[i], target, copy);
				else files[i].renameTo(target);
			} else {
				if (files[i].isDirectory()) moveOrCopyFolder(files[i], target, copy);
				else {
					try {
						target.createNewFile();
						Assistant.copyInputStream(new FileInputStream(files[i]), new FileOutputStream(target));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		if (fromTree) frame.catalog.handleDrop();
	}
	
	public void updateProgress(String note) {
		value++;
		monitor.setNote(note);
		monitor.setProgress(value);
	}
	
	public void moveOrCopyFolder(File folder, File targetParent, boolean copy) {
		for (File f : folder.listFiles()) {
			// if (f.isHidden()) continue;
			
			if (f.isDirectory()) {
				File newFolder = new File(targetParent, f.getName());
				newFolder.mkdir();
				moveOrCopyFolder(f, newFolder, copy);
				if (!copy) f.delete();
			} else if (f.isFile()) {
				File newFile = new File(targetParent, f.getName());
				try {
					newFile.getParentFile().mkdirs();
					if (copy) {
						newFile.createNewFile();
						Assistant.copyInputStream(new FileInputStream(f), new FileOutputStream(newFile));
					} else {
						newFile.delete();
						f.renameTo(newFile);
					}
					
					String path = f.getPath();
					
					int length = 70;
					
					updateProgress(f.getPath().length() > length ? path.substring(0, length - 3) + "..." : path);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		if (!copy) folder.delete();
	}
}
