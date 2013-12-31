package de.dakror.virtualhub.util;

import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.filechooser.FileSystemView;

import sun.awt.shell.ShellFolder;

import com.jtattoo.plaf.JTattooUtilities;
import com.jtattoo.plaf.acryl.AcrylLookAndFeel;
import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;

import de.dakror.virtualhub.settings.CFG;

/**
 * @author Dakror
 */
public class ThumbnailAssistant
{
	public static Image scaleImage(Image i)
	{
		return scaleImage(i, CFG.PREVIEWSIZE.width, CFG.PREVIEWSIZE.height);
	}
	
	public static Image scaleImage(Image i, double tw, double th)
	{
		try
		{
			double sw = i.getWidth(null);
			double sh = i.getHeight(null);
			double rw = 0;
			double rh = 0;
			double tr = tw / th;
			double sr = sw / sh;
			if (sr >= tr)
			{
				rw = tw;
				rh = rw / sr;
			}
			else
			{
				rh = th;
				rw = rh * sr;
			}
			Image result = i.getScaledInstance((int) Math.round(rw), (int) Math.round(rh), Image.SCALE_SMOOTH);
			i.flush();
			return result;
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	public static Icon getFileSystemIcon(File f)
	{
		if (JTattooUtilities.isMac())
		{
			try
			{
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				Icon icon = new JFileChooser().getIcon(f);
				UIManager.setLookAndFeel(new AcrylLookAndFeel());
				
				return icon;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				
				return null;
			}
		}
		try
		{
			ShellFolder shellFolder = ShellFolder.getShellFolder(f);
			Icon icon = new ImageIcon(shellFolder.getIcon(true));
			return icon;
		}
		catch (Exception e)
		{
			return FileSystemView.getFileSystemView().getSystemIcon(f);
		}
	}
	
	public static Image readPDF(File f)
	{
		try
		{
			RandomAccessFile raf = new RandomAccessFile(f, "r");
			FileChannel channel = raf.getChannel();
			ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
			PDFFile pdf = new PDFFile(buf);
			PDFPage page = pdf.getPage(0);
			Rectangle rect = new Rectangle(0, 0, (int) page.getBBox().getWidth(), (int) page.getBBox().getHeight());
			Image image = page.getImage(rect.width, rect.height, rect, null, true, true);
			raf.close();
			return image;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public static Image getThumbnail(File f)
	{
		try
		{
			File tmpFile = new File(f.getParentFile().getPath() + "/" + (!JTattooUtilities.isWindows() ? "." : "") + f.getName() + ".tmp");
			if (tmpFile.exists()) return ImageIO.read(tmpFile);
			else
			{
				BufferedImage thumbnail;
				if (Assistant.getFileExtension(f).equals("pdf")) thumbnail = Assistant.toBufferedImage(readPDF(f));
				else thumbnail = ImageMagickAssistant.getThumbnail(f);
				if (thumbnail == null) return null;
				
				createCacheFile(f, thumbnail);
				
				return thumbnail;
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public static void createCacheFile(File f, BufferedImage thumbnail) throws IOException
	{
		File file = new File(f.getParentFile().getPath() + "/" + (!JTattooUtilities.isWindows() ? "." : "") + f.getName() + ".tmp");
		file.createNewFile();
		file.deleteOnExit();
		ImageIO.write(thumbnail, "PNG", file);
		if (JTattooUtilities.isWindows()) Runtime.getRuntime().exec("attrib +H \"" + file.getPath() + "\"");
	}
}
