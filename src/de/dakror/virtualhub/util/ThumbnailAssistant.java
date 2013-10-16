package de.dakror.virtualhub.util;

import java.awt.Image;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import javax.media.jai.PlanarImage;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.filechooser.FileSystemView;

import sun.awt.shell.ShellFolder;

import com.jtattoo.plaf.JTattooUtilities;
import com.jtattoo.plaf.acryl.AcrylLookAndFeel;
import com.sun.media.jai.codec.ByteArraySeekableStream;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.SeekableStream;

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
			return i.getScaledInstance((int) Math.round(rw), (int) Math.round(rh), Image.SCALE_SMOOTH);
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
			e.printStackTrace();
			return FileSystemView.getFileSystemView().getSystemIcon(f);
		}
	}
	
	public static Image readTIF(File f)
	{
		try
		{
			FileInputStream in = new FileInputStream(f);
			FileChannel channel = in.getChannel();
			ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());
			channel.read(buffer);
			SeekableStream stream = new ByteArraySeekableStream(buffer.array());
			Image image = PlanarImage.wrapRenderedImage(ImageCodec.createImageDecoder(ImageCodec.getDecoderNames(stream)[0], stream, null).decodeAsRenderedImage()).getAsBufferedImage();
			stream.close();
			in.close();
			return image;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
