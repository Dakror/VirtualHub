package de.dakror.virtualhub.server.dialog;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.json.JSONException;

import de.dakror.virtualhub.server.Server;
import de.dakror.virtualhub.util.SpringUtilities;

/**
 * @author Dakror
 */
public class BackupEditDialog {
	public static void show() throws JSONException {
		final JDialog dialog = new JDialog(Server.currentServer.frame, "Backup-Einstellungen", true);
		dialog.setSize(400, 250);
		dialog.setLocationRelativeTo(Server.currentServer.frame);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		JPanel cp = new JPanel(new SpringLayout());
		cp.add(new JLabel("Zielverzeichnis:"));
		JPanel panel = new JPanel();
		final JTextField path = new JTextField((Server.currentServer.settings.has("backup.path") ? Server.currentServer.settings.getString("backup.path") : ""), 10);
		panel.add(path);
		panel.add(new JButton(new AbstractAction("Wählen...") {
			private static final long serialVersionUID = 1L;
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc = new JFileChooser((path.getText().length() > 0 ? new File(path.getText()) : new File(System.getProperty("user.home"))));
				jfc.setFileHidingEnabled(false);
				jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				jfc.setDialogTitle("Backup-Zielverzeichnis wählen");
				
				if (jfc.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) path.setText(jfc.getSelectedFile().getPath().replace("\\", "/"));
			}
		}));
		
		cp.add(panel);
		
		cp.add(new JLabel(""));
		cp.add(new JLabel(""));
		
		cp.add(new JButton(new AbstractAction("Abbrechen") {
			private static final long serialVersionUID = 1L;
			
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.dispose();
			}
		}));
		cp.add(new JButton(new AbstractAction("Speichern") {
			private static final long serialVersionUID = 1L;
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if (path.getText().length() > 0) Server.currentServer.settings.put("backup.path", path.getText());
					dialog.dispose();
				} catch (JSONException e1) {
					e1.printStackTrace();
				}
			}
		}));
		
		SpringUtilities.makeCompactGrid(cp, 3, 2, 6, 6, 6, 6);
		dialog.setContentPane(cp);
		dialog.pack();
		dialog.setVisible(true);
	}
}
