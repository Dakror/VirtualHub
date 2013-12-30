package de.dakror.virtualhub.client.file;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;

import com.jtattoo.plaf.AbstractLookAndFeel;
import com.jtattoo.plaf.ColorHelper;
import com.jtattoo.plaf.JTattooUtilities;
import com.jtattoo.plaf.LazyImageIcon;

import de.dakror.virtualhub.client.Client;
import de.dakror.virtualhub.data.Eticet;
import de.dakror.virtualhub.data.Tags;
import de.dakror.virtualhub.net.packet.Packet2Eticet;
import de.dakror.virtualhub.net.packet.Packet3Tags;
import de.dakror.virtualhub.settings.CFG;
import de.dakror.virtualhub.util.Assistant;
import de.dakror.virtualhub.util.FileSelection;
import de.dakror.virtualhub.util.MacOSXHandler;
import de.dakror.virtualhub.util.ThumbnailAssistant;

/**
 * @author Dakror
 */
public class FileButton extends JToggleButton implements DragSourceListener, DragGestureListener, DropTargetListener
{
	private static final long serialVersionUID = 1L;
	
	public static Polygon eticetPolygon;
	static
	{
		eticetPolygon = new Polygon();
		int size = 25;
		
		eticetPolygon.addPoint(0, 0);
		eticetPolygon.addPoint(size, 0);
		eticetPolygon.addPoint(0, size);
	}
	
	public File file;
	public Tags tags;
	JLabel preview;
	
	private Eticet eticet = Eticet.NONE;
	
	DropTarget dropTarget = new DropTarget(this, this);
	DragSource dragSource = DragSource.getDefaultDragSource();
	
	public FileButton(File file)
	{
		this.file = file;
		
		dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
		
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
		
		try
		{
			preview.setIcon(new ImageIcon(ThumbnailAssistant.scaleImage(((ImageIcon) sysIcon).getImage(), CFG.PREVIEWSIZE.width / 2f, CFG.PREVIEWSIZE.height / 2f)));
		}
		catch (NullPointerException e)
		{}
		
		add(preview);
		
		JLabel textLabel = new JLabel("<html><body style='text-align:center;'><br>" + Assistant.shortenFileName(file, 35, 2) + "</body></html>");
		add(textLabel);
		
		final JPopupMenu eticet = new JPopupMenu();
		for (final Eticet e : Eticet.values())
		{
			if (e == Eticet.NULL) continue;
			
			JMenuItem jmi = new JMenuItem(new AbstractAction(e.getName(), generateEticetIcon(e.getColor()))
			{
				
				private static final long serialVersionUID = 1L;
				
				@Override
				public void actionPerformed(ActionEvent evt)
				{
					JMenuItem jmi = (JMenuItem) evt.getSource();
					FileButton.this.eticet = Eticet.getByName(jmi.getText());
					try
					{
						Client.currentClient.sendPacket(new Packet2Eticet(FileButton.this.file, Client.currentClient.getCatalog().getName(), FileButton.this.eticet));
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
					Client.currentClient.frame.loadSubTree((EticetableTreeNode) Client.currentClient.frame.catalog.getSelectionPath().getLastPathComponent());
				}
			});
			eticet.add(jmi);
		}
		
		addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseReleased(MouseEvent e)
			{
				if (new Area(eticetPolygon).contains(e.getPoint()))
				{
					eticet.show(e.getComponent(), e.getX(), e.getY());
					setSelected(false);
					Client.currentClient.frame.setFileInfo(null);
				}
			}
		});
		
		new Thread()
		{
			@Override
			public void run()
			{
				setPriority(Thread.MAX_PRIORITY);
				try
				{
					setPreview();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			};
		}.start();
	}
	
	public void requestFileData()
	{
		try
		{
			Client.currentClient.sendPacket(new Packet2Eticet(file, Client.currentClient.getCatalog().getName(), Eticet.NULL));
			Client.currentClient.sendPacket(new Packet3Tags(file, Client.currentClient.getCatalog().getName(), null));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
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
			return null;
		}
	}
	
	public void setPreview() throws Exception
	{
		Image image = ThumbnailAssistant.getThumbnail(file);
		if (image != null) preview.setIcon(new ImageIcon(image));
	}
	
	public void setEticet(Eticet e)
	{
		eticet = e;
	}
	
	@Override
	public void paint(Graphics g)
	{
		super.paint(g);
		
		if (eticet == null) return;
		
		Graphics2D g2d = (Graphics2D) g;
		Color oldColor = g2d.getColor();
		
		g2d.setColor(eticet.getColor());
		
		g2d.fillPolygon(eticetPolygon);
		
		// very laf specific
		g2d.setColor(getModel().isRollover() ? AbstractLookAndFeel.getTheme().getRolloverColorDark() : ColorHelper.brighter(AbstractLookAndFeel.getTheme().getFrameColor(), 50));
		
		g2d.drawPolygon(eticetPolygon);
		
		g2d.setColor(oldColor);
	}
	
	@Override
	public void dragEnter(DragSourceDragEvent dsde)
	{}
	
	@Override
	public void dragOver(DragSourceDragEvent dsde)
	{}
	
	@Override
	public void dropActionChanged(DragSourceDragEvent dsde)
	{}
	
	@Override
	public void dragExit(DragSourceEvent dse)
	{}
	
	@Override
	public void dragDropEnd(DragSourceDropEvent dsde)
	{}
	
	@Override
	public void dragGestureRecognized(DragGestureEvent dge)
	{
		File[] selectedFiles = Client.currentClient.frame.getSelectedFiles();
		FileSelection transferable = new FileSelection(selectedFiles.length > 0 ? selectedFiles : new File[] { file });
		
		dge.startDrag(null, transferable, this);
	}
	
	@Override
	public void dragEnter(DropTargetDragEvent dtde)
	{
		getModel().setRollover(true);
	}
	
	@Override
	public void dragOver(DropTargetDragEvent dtde)
	{}
	
	@Override
	public void dropActionChanged(DropTargetDragEvent dtde)
	{}
	
	@Override
	public void dragExit(DropTargetEvent dte)
	{
		getModel().setRollover(false);
	}
	
	@Override
	public void drop(DropTargetDropEvent dtde)
	{
		if (!dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
		{
			Transferable tr = dtde.getTransferable();
			
			if (Client.currentClient.frame.getSelectedFileButtons().length > 0)
			{
				for (FileButton fb : Client.currentClient.frame.getSelectedFileButtons())
				{
					try
					{
						String tag = tr.getTransferData(DataFlavor.stringFlavor).toString();
						if (fb.tags.add(tag)) Client.currentClient.sendPacket(new Packet3Tags(fb.file, Client.currentClient.getCatalog().getName(), fb.tags));
						else
						{
							if (JOptionPane.showConfirmDialog(Client.currentClient.frame, fb.file.getName() + " ist mit diesem Schlüsselwort bereits verknüpft.\nM√∂chten Sie es von diese" + (fb.file.isDirectory() ? "m Verzeichnis" : "r Datei") + " entfernen?", "Schlüsselwort bereits verknüpft", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
							{
								if (fb.tags.remove(tag)) Client.currentClient.sendPacket(new Packet3Tags(fb.file, Client.currentClient.getCatalog().getName(), fb.tags));
							}
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
			else
			{
				try
				{
					String tag = tr.getTransferData(DataFlavor.stringFlavor).toString();
					if (tags.add(tag)) Client.currentClient.sendPacket(new Packet3Tags(file, Client.currentClient.getCatalog().getName(), tags));
					else
					{
						if (JOptionPane.showConfirmDialog(Client.currentClient.frame, file.getName() + " ist mit diesem Schlüsselwort bereits verknüpft.\nM√∂chten Sie es von diese" + (file.isDirectory() ? "m Verzeichnis" : "r Datei") + " entfernen?", "Schlüsselwort bereits verknüpft", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
						{
							if (tags.remove(tag)) Client.currentClient.sendPacket(new Packet3Tags(file, Client.currentClient.getCatalog().getName(), tags));
						}
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
}
