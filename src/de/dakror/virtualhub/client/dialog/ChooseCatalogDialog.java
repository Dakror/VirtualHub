package de.dakror.virtualhub.client.dialog;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ListSelectionModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.dakror.virtualhub.client.Client;
import de.dakror.virtualhub.client.ClientFrame;
import de.dakror.virtualhub.data.Catalog;
import de.dakror.virtualhub.net.packet.Packet0Katalogs;

/**
 * @author Dakror
 */
public class ChooseCatalogDialog
{
	public static void show(ClientFrame frame, final JSONArray data)
	{
		final JDialog dialog = new JDialog(frame, true);
		dialog.setTitle("Katalog w\u00e4hlen");
		dialog.setSize(400, 300);
		dialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		dialog.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				Client.currentClient.disconnect();
				System.exit(0);
			}
		});
		
		JPanel contentPane = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
		dialog.setContentPane(contentPane);
		DefaultListModel dlm = new DefaultListModel();
		for (int i = 0; i < data.length(); i++)
		{
			try
			{
				dlm.addElement(data.getJSONObject(i).getString("name"));
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
		}
		
		final JList catalogs = new JList(dlm);
		catalogs.setDragEnabled(false);
		catalogs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		JScrollPane jsp = new JScrollPane(catalogs, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jsp.setPreferredSize(new Dimension(396, 200));
		contentPane.add(jsp);
		
		JPanel mods = new JPanel(new GridLayout(1, 2));
		mods.setPreferredSize(new Dimension(50, 22));
		mods.add(new JButton(new AbstractAction("+")
		{
			private static final long serialVersionUID = 1L;
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				String name = JOptionPane.showInputDialog(dialog, "Bitte geben Sie den Namen des neuen Katalogs ein", "Katalog hinzuf\u00fcgen", JOptionPane.PLAIN_MESSAGE);
				if (name != null && name.length() > 0)
				{
					DefaultListModel dlm = (DefaultListModel) catalogs.getModel();
					for (int i = 0; i < dlm.getSize(); i++)
					{
						if (dlm.get(i).toString().equals(name))
						{
							JOptionPane.showMessageDialog(dialog, "Es existert bereits ein Katalog mit diesem Namen!", "Katalog berets vorhanden!", JOptionPane.ERROR_MESSAGE);
							actionPerformed(e);
							return;
						}
					}
					
					try
					{
						dlm.addElement(name);
						JSONObject o = new JSONObject();
						o.put("name", name);
						o.put("sources", new JSONArray());
						o.put("tags", new JSONArray());
						data.put(o);
						Client.currentClient.sendPacket(new Packet0Katalogs(data));
					}
					catch (Exception e1)
					{
						e1.printStackTrace();
					}
				}
			}
		}));
		mods.add(new JButton(new AbstractAction("-")
		{
			private static final long serialVersionUID = 1L;
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (catalogs.getSelectedIndex() != -1)
				{
					if (JOptionPane.showConfirmDialog(dialog, "Sind Sie sicher, dass Sie diesen\r\nKatalog unwiderruflich l\u00f6schen wollen?", "Katalog l\u00f6schen", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
					{
						DefaultListModel dlm = (DefaultListModel) catalogs.getModel();
						data.remove(catalogs.getSelectedIndex());
						dlm.remove(catalogs.getSelectedIndex());
						try
						{
							Client.currentClient.sendPacket(new Packet0Katalogs(data));
						}
						catch (IOException e1)
						{
							e1.printStackTrace();
						}
					}
				}
			}
		}));
		
		contentPane.add(mods);
		
		JLabel l = new JLabel("");
		l.setPreferredSize(new Dimension(396, 14));
		contentPane.add(l);
		
		JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
		sep.setPreferredSize(new Dimension(396, 10));
		contentPane.add(sep);
		
		JPanel buttons = new JPanel(new GridLayout(1, 2));
		buttons.setPreferredSize(new Dimension(396, 22));
		buttons.add(new JButton(new AbstractAction("Abbrechen")
		{
			private static final long serialVersionUID = 1L;
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Client.currentClient.disconnect();
				System.exit(0);
			}
		}));
		buttons.add(new JButton(new AbstractAction("Katalog w\u00e4hlen")
		{
			private static final long serialVersionUID = 1L;
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (catalogs.getSelectedIndex() != -1)
				{
					try
					{
						Client.currentClient.setCatalog(new Catalog(data.getJSONObject(catalogs.getSelectedIndex())));
						Client.currentClient.frame.setTitle("- " + Client.currentClient.getCatalog().getName());
						dialog.dispose();
					}
					catch (JSONException e1)
					{
						e1.printStackTrace();
					}
				}
			}
		}));
		
		dialog.add(buttons);
		
		dialog.setLocationRelativeTo(frame);
		dialog.setResizable(false);
		dialog.setVisible(true);
	}
}