package de.dakror.virtualhub.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;

import com.jtattoo.plaf.AbstractLookAndFeel;
import com.jtattoo.plaf.ColorHelper;

import de.dakror.virtualhub.settings.CFG;
import de.dakror.virtualhub.util.Assistant;
import de.dakror.virtualhub.util.ThumbnailAssistant;

/**
 * @author Dakror
 */
public class FileButton extends JButton
{
	private static final long serialVersionUID = 1L;
	
	File file;
	JLabel preview;
	
	Color eticet = Color.green;
	
	public FileButton(File file)
	{
		this.file = file;
		
		setContentAreaFilled(false);
		setFocusPainted(false);
		setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		setPreferredSize(new Dimension(250, 250));
		
		preview = new JLabel();
		preview.setPreferredSize(CFG.PREVIEWSIZE);
		preview.setHorizontalAlignment(JLabel.CENTER);
		preview.setVerticalAlignment(JLabel.CENTER);
		preview.setIcon(new ImageIcon(ThumbnailAssistant.scaleImage(((ImageIcon) ThumbnailAssistant.getFileSystemIcon(file)).getImage(), CFG.PREVIEWSIZE.width / 2f, CFG.PREVIEWSIZE.height / 2f)));
		add(preview);
		
		JLabel textLabel = new JLabel("<html><body style='text-align:center;'><br>" + Assistant.shortenFileName(file, 35, 2) + "</body></html>");
		add(textLabel);
	}
	
	public void setPreview()
	{	
		
	}
	
	@Override
	public void paint(Graphics g)
	{
		super.paint(g);
		
		if (eticet == null) return;
		
		Graphics2D g2d = (Graphics2D) g;
		Color oldColor = g2d.getColor();
		
		g2d.setColor(eticet);
		
		Polygon p = new Polygon();
		
		
		
		int size = 25;
		
		p.addPoint(0, 0);
		p.addPoint(size, 0);
		p.addPoint(0, size);
		
		g2d.fillPolygon(p);
		
		// very laf specific
		g2d.setColor(getModel().isRollover() ? AbstractLookAndFeel.getTheme().getRolloverColorDark() : ColorHelper.brighter(AbstractLookAndFeel.getTheme().getFrameColor(), 50));
		
		g2d.drawPolygon(p);
		
		g2d.setColor(oldColor);
	}
}