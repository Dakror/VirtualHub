package de.dakror.virtualhub.client;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import com.jtattoo.plaf.AbstractLookAndFeel;
import com.jtattoo.plaf.ColorHelper;
import com.jtattoo.plaf.JTattooUtilities;
import com.jtattoo.plaf.LazyImageIcon;

import de.dakror.virtualhub.settings.CFG;
import de.dakror.virtualhub.util.Assistant;
import de.dakror.virtualhub.util.MacOSXHandler;
import de.dakror.virtualhub.util.ThumbnailAssistant;

/**
 * @author Dakror
 */
public class FileButton extends JButton
{
	private static final long serialVersionUID = 1L;
	public static final String[] names = { "Kein Etikett", "Rot", "Orange", "Gelb", "Gr\u00fcn", "Blau", "Violett", "Grau", };
	public static final Color[] colors = { new Color(0, 0, 0, 0), Color.red, Color.orange, Color.yellow, Color.green, Color.blue, Color.magenta, Color.gray };
	
	public static Polygon eticetPolygon;
	static
	{
		eticetPolygon = new Polygon();
		int size = 25;
		
		eticetPolygon.addPoint(0, 0);
		eticetPolygon.addPoint(size, 0);
		eticetPolygon.addPoint(0, size);
	}
	
	
	File file;
	JLabel preview;
	
	
	Color eticet = new Color(0, 0, 0, 0);
	
	
	public FileButton(File file)
	{
		this.file = file;
		
		// setContentAreaFilled(false);
		setFocusPainted(false);
		setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		setPreferredSize(new Dimension(250, 250));
		
		preview = new JLabel();
		preview.setPreferredSize(CFG.PREVIEWSIZE);
		preview.setHorizontalAlignment(JLabel.CENTER);
		preview.setVerticalAlignment(JLabel.CENTER);
		
		Icon sysIcon = ThumbnailAssistant.getFileSystemIcon(file);
		
		if (sysIcon instanceof LazyImageIcon) sysIcon = ((LazyImageIcon) sysIcon).getIcon();
		
		if (JTattooUtilities.isMac()) sysIcon = MacOSXHandler.getIcon(sysIcon);
		
		preview.setIcon(new ImageIcon(ThumbnailAssistant.scaleImage(((ImageIcon) sysIcon).getImage(), CFG.PREVIEWSIZE.width / 2f, CFG.PREVIEWSIZE.height / 2f)));
		add(preview);
		
		JLabel textLabel = new JLabel("<html><body style='text-align:center;'><br>" + Assistant.shortenFileName(file, 35, 2) + "</body></html>");
		add(textLabel);
		
		addMouseMotionListener(new MouseMotionAdapter()
		{
			@Override
			public void mouseDragged(MouseEvent e)
			{
				if (SwingUtilities.isLeftMouseButton(e))
				{
					int x = FileButton.this.getLocationOnScreen().x - Client.currentClient.frame.getContentPane().getLocationOnScreen().x + e.getX();
					int y = FileButton.this.getLocationOnScreen().y - Client.currentClient.frame.getContentPane().getLocationOnScreen().y + e.getY();
					
					if (Client.currentClient.frame.catalogWrap.contains(x, y))
					{
						Client.currentClient.frame.copy = e.isControlDown();
						Client.currentClient.frame.dragged = FileButton.this;
						Client.currentClient.frame.mouse = new Point(x, y);
						Client.currentClient.frame.catalog.repaint();
					}
					else
					{
						Client.currentClient.frame.setCursor(Cursor.getDefaultCursor());
					}
				}
			}
		});
		addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseReleased(MouseEvent e)
			{
				if (Client.currentClient.frame.dragged != null && Client.currentClient.frame.dragged.file.equals(FileButton.this.file) && e.getButton() == 1)
				{
					Client.currentClient.frame.dragged = null;
					Client.currentClient.frame.setCursor(Cursor.getDefaultCursor());
				}
			}
		});
		
		final JPopupMenu eticet = new JPopupMenu();
		for (int i = 0; i < names.length; i++)
		{
			JMenuItem jmi = new JMenuItem(new AbstractAction(names[i], generateEticetIcon(colors[i]))
			{
				
				private static final long serialVersionUID = 1L;
				
				@Override
				public void actionPerformed(ActionEvent e)
				{
					JMenuItem jmi = (JMenuItem) e.getSource();
					FileButton.this.eticet = colors[Arrays.asList(names).indexOf(jmi.getText())];
				}
			});
			eticet.add(jmi);
		}
		
		
		addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseReleased(MouseEvent e)
			{
				if (new Area(eticetPolygon).contains(e.getPoint())) eticet.show(e.getComponent(), e.getX(), e.getY());
				
			}
		});
		
		new Thread()
		{
			@Override
			public void run()
			{
				setPreview();
			};
		}.start();
	}
	
	public Icon generateEticetIcon(Color c)
	{
		try
		{
			BufferedImage bi = new BufferedImage(48, 48, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = (Graphics2D) bi.getGraphics();
			g.setColor(c);
			
			int arc = 16;
			
			g.setClip(new RoundRectangle2D.Double(0, 0, 48, 48, arc, arc));
			g.fillRect(0, 0, 48, 48);
			g.drawImage(ImageIO.read(getClass().getResource("/img/Gradient.png")), 0, 0, null);
			
			return new ImageIcon(bi.getScaledInstance(16, 16, Image.SCALE_SMOOTH));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public void setPreview()
	{
		String e = Assistant.getFileExtension(file);
		
		if (e.equals("jpg") || e.equals("jpeg") || e.equals("png") || e.equals("gif") || e.equals("bmp"))
		{
			try
			{
				preview.setIcon(new ImageIcon(ThumbnailAssistant.scaleImage(ImageIO.read(file))));
			}
			catch (IOException e1)
			{
				e1.printStackTrace();
			}
		}
		else if (e.equals("tif") || e.equals("tiff")) preview.setIcon(new ImageIcon(ThumbnailAssistant.scaleImage(ThumbnailAssistant.readTIF(file))));
		else if (e.equals("psd")) preview.setIcon(new ImageIcon(ThumbnailAssistant.scaleImage(ThumbnailAssistant.readPSD(file))));
		else if (e.equals("pdf")) preview.setIcon(new ImageIcon(ThumbnailAssistant.scaleImage(ThumbnailAssistant.readPDF(file))));
		
	}
	
	@Override
	public void paint(Graphics g)
	{
		super.paint(g);
		
		if (eticet == null) return;
		
		Graphics2D g2d = (Graphics2D) g;
		Color oldColor = g2d.getColor();
		
		g2d.setColor(eticet);
		
		g2d.fillPolygon(eticetPolygon);
		
		// very laf specific
		g2d.setColor(getModel().isRollover() ? AbstractLookAndFeel.getTheme().getRolloverColorDark() : ColorHelper.brighter(AbstractLookAndFeel.getTheme().getFrameColor(), 50));
		
		g2d.drawPolygon(eticetPolygon);
		
		g2d.setColor(oldColor);
	}
}
