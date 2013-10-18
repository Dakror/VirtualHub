package de.dakror.virtualhub.client.dialog;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

/**
 * @author Dakror
 */
public class ProgressDialog extends JDialog
{
	private static final long serialVersionUID = 1L;
	
	JProgressBar bar;
	JLabel msg, note;
	
	public ProgressDialog(final JFrame parent, String message, String note, int maximum)
	{
		super(parent);
		
		setSize(350, 120);
		setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setLocationRelativeTo(parent);
		
		msg = new JLabel(message, JLabel.CENTER);
		msg.setPreferredSize(new Dimension(330, 30));
		add(msg);
		
		this.note = new JLabel(note, JLabel.CENTER);
		this.note.setPreferredSize(new Dimension(330, 22));
		add(this.note);
		
		bar = new JProgressBar(0, maximum);
		bar.setValue(0);
		bar.setStringPainted(true);
		bar.setPreferredSize(new Dimension(330, 22));
		add(bar);
		
		setVisible(true);
	}
	
	public void setNote(String note)
	{
		this.note.setText(note);
	}
	
	public void setProgress(int progress)
	{
		bar.setValue(progress);
	}
}
